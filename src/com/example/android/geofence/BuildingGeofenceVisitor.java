package com.example.android.geofence ;

import com.example.android.location.R ;
import com.example.android.location.WebViewActivity ;
import android.util.Log ;
import android.app.NotificationManager ;
import android.app.Notification ;
import android.app.Activity ;
import android.content.Intent ;
import android.app.PendingIntent ;
import android.content.Context ;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.io.InputStream ;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.HttpResponse ;
import org.apache.http.HttpEntity;
import org.apache.http.params.BasicHttpParams ;
import android.net.ConnectivityManager ;
import android.net.NetworkInfo ;
import android.os.AsyncTask ;
import java.io.IOException ;




public class BuildingGeofenceVisitor implements IBuildingGeofenceVisitor 
{

  private final Building building ;
  protected WebViewActivity activity;
  private final String transition ;

  public BuildingGeofenceVisitor(Building building, WebViewActivity activity, String transition)
  {
     this.building = building ;
     this.activity = activity ;
     if(! "EXIT".equals(transition))
     {
        this.transition = "ENTER" ; 
     }
     else
     {
        this.transition = "EXIT" ;
     }
     

  }
  
  @Override
  public void visit(GeofenceNullVisitable nullVisited)
  {


  }


  @Override
  public void visit(Building building)
  {
     Log.d(GeofenceUtils.APPTAG, "Visiting Building:" + building.getName()) ;
     GeofenceBuilding gfBuilding = building.getGeofenceBuilding() ;
     gfBuilding.accept(this) ;

  }
  @Override
  public void visit(GeofenceBuilding gfBuilding)
  {
     Log.d(GeofenceUtils.APPTAG, "Visiting GeofenceBuilding:" + gfBuilding) ;
     int userId = this.activity.getUniqueUserId() ;
     int buildingId =  gfBuilding.getId() ;
     int infected = this.activity.getInfected() ;

     new PatientZeroTask().execute("http://ec2-54-195-107-35.eu-west-1.compute.amazonaws.com:8081/infected?building="+buildingId +"&sid="+ userId  +"&infected="+infected + "&transition=" + this.transition);

     // TODO add async tast in broadcast receiver to get buildings list
     // TODO click back when convo active - back to map
     // set infected status of WebViewActivity
  }
  private class PatientZeroTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... uri) {

                return httpGet(uri[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d(GeofenceUtils.APPTAG, "infected response:" + result ) ;
            if(result!=null)
            {
		char infectedFlag = result.charAt(0) ;
                if(infectedFlag == '1')
                {
		   activity.setInfected(1) ;
                
                PendingIntent pi = PendingIntent.getActivity(activity, 0,
	   new Intent(activity, com.example.android.location.WebViewActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = new Notification();
                notification.tickerText = "Forgotten Futures Infection Alert" ; 
                notification.icon = R.drawable.ic_notification ;
                notification.defaults |= Notification.DEFAULT_VIBRATE ;
                notification.defaults |= Notification.DEFAULT_SOUND   ;
                notification.setLatestEventInfo(activity, "You caught Robot Flu",
			                "Forgotten Futures"  , pi);
                NotificationManager mNotificationManager =
                 (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        
                 mNotificationManager.notify(1579, notification);
       
                }
                else
                {
		   activity.setInfected(0) ;	
                }
            }
       }
}

    
  protected String httpGet(String uri)
   {
        DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet httpget = new HttpGet(uri);
        // Depends on your web service
        httpget.setHeader("Content-type", "text/plain");

        InputStream inputStream = null;
        String result = null;
        try
        {
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                        sb.append(line + "\n");
                }
                return sb.toString();


        } catch (Exception e) {
           Log.e(GeofenceUtils.APPTAG, "Could not read JSON data from remote source: " + e) ;
           return null ;
        }
        finally {
          try{if(inputStream != null)inputStream.close();}catch(Exception squish){ return null;}
        }

   }

}
