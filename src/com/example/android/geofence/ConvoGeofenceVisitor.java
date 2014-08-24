package com.example.android.geofence ;

import com.example.android.location.BackgroundAudioService ;
import com.example.android.location.R ;
import com.example.android.location.WebViewActivity ;
import android.media.MediaPlayer ;
import android.util.Log ;
import android.app.NotificationManager ;
import android.app.Notification ;
import android.app.Activity ;
import android.content.Intent ;
import android.app.PendingIntent ;
import android.content.Context ;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;




public class ConvoGeofenceVisitor implements IGeofenceVisitor, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener 
{

  private final Convo convo ;
  private final BackgroundAudioService backgroundAudioService ;
  private OnComplete onComplete ;
  private WebViewActivity.GeofenceSampleReceiver receiver ;
  private WebViewActivity activity;
  private Dialog activeDialog ;


  public ConvoGeofenceVisitor(Convo  convo, BackgroundAudioService backgroundAudioService, WebViewActivity activity)
  {
     this.convo = convo ;
     this.backgroundAudioService = backgroundAudioService ;
     this.activity = activity ;

  }


  @Override
  public Dialog getActiveDialog()
  {
    return this.activeDialog ;
  }

  public void visit(Convo convo)
  {
     Log.d(GeofenceUtils.APPTAG, "visiting Convo") ;
     GeofenceAudio gfAudio = convo.getGeofenceAudio() ;
     gfAudio.accept(this) ;
  }

  public void visit(Dialog dialog)
  {
      Log.d(GeofenceUtils.APPTAG, "visiting dialog" + dialog ) ;
        
      // this.activity.activateDialog(dialog) ;
      this.activeDialog = dialog ;

      PendingIntent pi = PendingIntent.getActivity(activity, 0,
	   new Intent(activity, com.example.android.location.WebViewActivity.class),
      PendingIntent.FLAG_UPDATE_CURRENT);
      Notification notification = new Notification();
      notification.tickerText = "Dialog" ; 
      notification.icon = R.drawable.ic_notification ;
      notification.defaults |= Notification.DEFAULT_VIBRATE ;
      notification.defaults |= Notification.DEFAULT_SOUND   ;
      notification.setLatestEventInfo(this.activity, "forgotton futures",
			                "Dialog"  , pi);
      NotificationManager mNotificationManager =
           (NotificationManager) this.activity.getSystemService(Context.NOTIFICATION_SERVICE);
        
      mNotificationManager.notify(1572, notification);
       
      this.activity.accept(this) ;
  }


  public void visit(GeofenceAudio geofenceAudio)
  {
   
    // TODO onComplete
    Log.d(GeofenceUtils.APPTAG, "visiting geofenceAudio" ) ;

    String track = geofenceAudio.getTrack() ;
    this.backgroundAudioService.playForeground(track, this, this) ;
    if(geofenceAudio.hasOnComplete())
    {
        this.onComplete = geofenceAudio.getOnComplete() ;

    }
    else
    {
        this.onComplete = null ;
    }

  }
  
  
  public void visit(Option option)
  {
     Log.d(GeofenceUtils.APPTAG, "visiting Option: " + option) ; 
     Audio audio = option.getAudio() ;
     audio.accept(this) ; 
  }

  public void visit(Audio audio)
  {
     
     Log.d(GeofenceUtils.APPTAG, "visiting Audio: " + audio) ; 
     String track = audio.getTrack() ;
     this.backgroundAudioService.playForeground(track, this, this) ;
     if(audio.hasOnComplete())
     {
        this.onComplete = audio.getOnComplete() ;

     }
     else
     {
        this.onComplete = null ;
     }


  }

  public void visit(GeofenceNullVisitable nullVisited)
  {


  }

  public void visitConvo()
  {
    // visit Convo
    convo.accept(this) ;


  }

  @Override
  public void onCompletion(MediaPlayer mp)
  {
	Log.d(GeofenceUtils.APPTAG, "MediaPlayer.OnCompletionListener.onComplete called") ;
        if(mp!=null) mp.release() ;
	mp = null ;
	if(this.onComplete != null)
	{
	   this.onComplete.accept(this) ;
	}
	// TODO check this logic
	this.onComplete = null ;

  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra)
  {
      mp.reset() ;
      Log.e(GeofenceUtils.APPTAG, "Media error state code: " + what ) ;
      return true ;

  }

  // broadcast receiver to receive Dialog Option results
  // get the current Dialog option

}
