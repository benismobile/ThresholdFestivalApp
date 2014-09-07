package com.example.android.location ;

import android.app.Activity ;
import android.os.Bundle ;
import android.content.Intent ;
import android.widget.EditText ;
import android.view.ViewConfiguration ;

import android.view.View;
import android.webkit.WebView ;
import android.annotation.SuppressLint ;
import android.webkit.WebSettings ;
import android.os.* ;
import android.support.v7.app.ActionBarActivity ;
import android.support.v7.widget.SearchView;
import android.view.Menu ;
import android.util.Log ;
import android.view.MenuItem ;
import android.view.MenuInflater ;
import android.widget.Toast ;
import android.text.TextUtils ;
import android.app.NotificationManager ;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.location.Location;
import android.content.SharedPreferences;
import android.content.Context;
import android.media.SoundPool;
import android.media.AudioManager;
import android.media.SoundPool.OnLoadCompleteListener;
import android.media.MediaPlayer ;
import android.media.MediaPlayer.OnPreparedListener;
import android.content.ComponentName;
import android.text.format.DateUtils;
import com.example.android.geofence.GeofenceUtils.REMOVE_TYPE;
import com.example.android.geofence.GeofenceUtils.REQUEST_TYPE;
import com.example.android.geofence.GeofenceRemover;
import com.example.android.geofence.GeofenceRequester;
import com.example.android.geofence.SimpleGeofenceStore;
import com.example.android.geofence.SimpleGeofence;
import com.example.android.geofence.GeofenceUtils;
import com.example.android.location.BackgroundAudioService ;
import android.content.ServiceConnection ;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.List;
import java.io.InputStream ;
import java.lang.StringBuilder ;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.DialogInterface ;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time ;
import com.example.android.geofence.GeofenceDialogFragment ;
import com.example.android.geofence.Dialog ;
import com.example.android.geofence.Audio ;
import com.example.android.geofence.Option ;
import com.example.android.geofence.Convo ;
import com.example.android.geofence.GeofenceAudio ;
import com.example.android.geofence.GeofenceBuilding ;
import com.example.android.geofence.Building ;
import com.example.android.geofence.ConvoJSONParser ;
import com.example.android.geofence.BuildingJSONParser ;
import com.example.android.geofence.ConvoGeofenceVisitor ;
import com.example.android.geofence.BuildingGeofenceVisitor ;
import com.example.android.geofence.IGeofenceVisitable ;
import com.example.android.geofence.IGeofenceVisitor ;
import com.example.android.geofence.ConvoDialogOnClickListener ;
import android.app.FragmentManager ;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.HttpResponse ;
import org.apache.http.HttpEntity;
import org.apache.http.params.BasicHttpParams ;
import org.json.JSONObject ;
import org.json.JSONArray ;
import org.json.JSONException ;
import android.net.ConnectivityManager ;
import android.net.NetworkInfo ;
import android.os.AsyncTask ;
import java.io.IOException ;
import java.text.ParseException ;
import java.util.Iterator ;
import android.webkit.WebViewClient ;

public class WebViewActivity extends ActionBarActivity
implements 
   LocationListener,
   GooglePlayServicesClient.ConnectionCallbacks,
   GooglePlayServicesClient.OnConnectionFailedListener,
   MediaPlayer.OnPreparedListener, IGeofenceVisitable, GeofenceDialogFragment.ConvoDialogListener
   


{
   private boolean mIsInFront ;
   private int mActiveMap = 0 ;
   private boolean mAchievementsShowing = false ;
   private LocationClient mLocationClient;
   private LocationRequest mLocationRequest;
   boolean mUpdatesRequested = false;
   WebView webview ;
   SharedPreferences mPrefs;  // storage for location update status
   SharedPreferences.Editor mEditor;
 
   // Persistent storage for geofences
   private SimpleGeofenceStore mGeofencePrefs;

   private static final long GEOFENCE_EXPIRATION_IN_HOURS = 1;
   private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

   // Store the current request
   private REQUEST_TYPE mRequestType;

   // Store the current type of removal
   private REMOVE_TYPE mRemoveType;


   // Store a list of geofences to add
   List<Geofence> mCurrentGeofences;
   ArrayList<String> mCurrentGeofenceIds ;

   // Add geofences handler
   private GeofenceRequester mGeofenceRequester;
   // Remove geofences handler
   private GeofenceRemover mGeofenceRemover;

   private DecimalFormat mLatLngFormat;
   private DecimalFormat mRadiusFormat;

   
   private GeofenceSampleReceiver mBroadcastReceiver;

   // An intent filter for the broadcast receiver
   private IntentFilter mIntentFilter;

   // Store the list of geofences to remove
   private List<String> mGeofenceIdsToRemove;
   private int uuidHash ;
   private int infected ;
   private String uuid ; 
   private SoundPool mSoundPool ;
   private HashMap mSoundMap ;
   private HashMap<String, Convo> mConvos ;
   private HashMap<String, Building> mBuildings ;
   private HashSet mSoundLoadedMap ;
   private MediaPlayer mPlayer ;  
   private MediaPlayer mPlayer2 ;  
   private boolean mBackgroundAudioServiceRunning = false ;
   private BackgroundAudioService mBackgroundAudioService ;
   private boolean mIsBound = false ;
   private Convo mActiveConvo = null  ;
   private boolean mActiveConvoInProgress = false ;
   private Dialog mActiveDialog = null ;
   private boolean mActiveDialogShowing = false ;
   private GeofenceDialogFragment mActiveDialogFragment = null ;
//   private boolean dialogActive = false ;
   private WebViewActivity mActivity = this;
  

   private ServiceConnection mBackgroundAudioServiceConnection = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        mBackgroundAudioService = ((BackgroundAudioService.LocalBinder) service).getService();
        mIsBound = true ;
        // Tell the user about this for our demo.
        Log.d( GeofenceUtils.APPTAG, "Connected to BackgroundAudioService") ;
        
    }

      public void onServiceDisconnected(ComponentName className) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        // Because it is running in our same process, we should never
        // see this happen.
        mBackgroundAudioService = null;
	mIsBound = false ;
	Log.e(GeofenceUtils.APPTAG, "Disconnected from BackgroundAudioService ERROR") ;
    }

   };



   @Override
   public String toJSONString()
   {
     if(mActiveConvo!=null)
      return mActiveConvo.toJSONString() ;

     return "{}" ;

   }

   @Override
   public void accept(IGeofenceVisitor v) 
   {
      Log.d(GeofenceUtils.APPTAG, "WebViewActivity: accept called mIsInFront:" + mIsInFront ) ;
      mActiveDialog = v.getActiveDialog() ; // set ActiveDialog so we can display it in onResume

      if(mIsInFront)
      {
         
         Log.d(GeofenceUtils.APPTAG, "WebViewActivity: accept visitor: show Dialog:" + mActiveDialog ) ;
         GeofenceDialogFragment dialog =  GeofenceDialogFragment.newInstance(mActiveDialog) ; 
         dialog.show(getSupportFragmentManager(), "GeofenceEventFragment") ;
         mActiveDialogFragment = dialog ;
	 mActiveDialogShowing = true ;
	 mEditor.putBoolean("mActiveDialogShowing", true ) ;
	 mEditor.commit() ;
	 
      }
      else
      {
          Log.d(GeofenceUtils.APPTAG, "persisting mActiveDialog to mPrefs" ) ;          
          mEditor.putString("mActiveDialog", mActiveDialog.toJSONString()) ;
          mEditor.commit() ;
      }

   }

   @Override
   public void onDialogClick(int selected)
   {
      Log.d(GeofenceUtils.APPTAG, "onDialogClick selected:" + selected) ;
      Option[] options = mActiveDialog.getOptions() ;
      Option selectedOption = options[selected] ;
     
     ConvoGeofenceVisitor geofenceVisitor = new ConvoGeofenceVisitor(mActiveConvo, mBackgroundAudioService, this ) ;
     // TODO remove comment geofenceVisitor.visit(selectedOption) ;
     selectedOption.accept(geofenceVisitor) ;
     mActiveDialogFragment.dismiss() ;
     mActiveDialogFragment = null ;
     mActiveDialog = null ;
     mActiveDialogShowing = false ;

     mEditor.remove("mActiveDialog") ;
     mEditor.putBoolean("mActiveDialogShowing", false) ;
     mEditor.commit();

   }

   public int getUniqueUserId()
   {

        return mPrefs.getInt("uuidHash", -1) ;
   }

   public int getInfected()
   {
       return mPrefs.getInt("infected", 0) ;

   }
  
   public void setInfected(int infected) 
   {
	this.infected = infected ;
        mEditor.putInt("infected", infected) ;
        mEditor.commit() ;	
   } 

   @SuppressLint("NewApi")
   @Override
   protected void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);

     // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

     webview = new WebView(this);
     setContentView(webview);
     WebSettings webSettings = webview.getSettings();
     webSettings.setJavaScriptEnabled(true);
     webSettings.setAllowContentAccess(true) ;
     webSettings.setBlockNetworkImage (false) ;
     webSettings.setUseWideViewPort(true);
     webSettings.setLoadsImagesAutomatically (true) ;
    // WHATEVER YOU DO: DONT USE setAllowFileAccess* ON GINGERBREAB - Causes nasty crach
    // BUT needed to get the local gpx loading to work
     webview.addJavascriptInterface(new WebAppInterface(this),"Android");
     webview.loadUrl("file:///android_asset/html/threshold2.html");

    mLocationClient = new LocationClient(this, this, this);
    mLocationRequest = LocationRequest.create();
    mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

    // Open Shared Preferences
    mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

   // Get an editor
   mEditor = mPrefs.edit();
   mUpdatesRequested = true ;
   mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
   mEditor.commit();

   // is uuid already generated?

   this.uuidHash = mPrefs.getInt("uuidHash", -1) ;
   if(this.uuidHash== -1)
   {    this.uuidHash = java.util.UUID.randomUUID().hashCode() ;
   	// mEditor.putString("uuid", this.uuid) ;
   	mEditor.putInt("uuidHash", this.uuidHash) ;
   	mEditor.commit() ;
  }

   // Create a new broadcast receiver to receive updates from the listeners and service
     mBroadcastReceiver = new GeofenceSampleReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
        // Action for broadcast Intents containing ENTER and EXIT TRANSITIONS
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_TRANSITION);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate a new geofence storage area
        mGeofencePrefs = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);
   
        mSoundMap = new HashMap<Integer, Integer>();
        mSoundLoadedMap = new HashSet<Integer>();
        
	mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                    int status) {
	       Log.d(GeofenceUtils.APPTAG, "onLoadCompleteListener sampleId " + sampleId) ;
               mSoundLoadedMap.add(new Integer(sampleId)) ;
            }
        });
       
        mConvos = new HashMap<String, Convo>() ;
        mBuildings = new HashMap<String, Building>() ;
      
        
     	Intent startAudioIntent = new Intent(this, com.example.android.location.BackgroundAudioService.class);
        bindService(startAudioIntent, mBackgroundAudioServiceConnection, Context.BIND_AUTO_CREATE);
        restoreInstanceState(savedInstanceState) ;
  
      // getOverflowMenu() ;

   } // ends onCreate


   private void getOverflowMenu() {

     try {
        ViewConfiguration config = ViewConfiguration.get(this);
        java.lang.reflect.Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
        if(menuKeyField != null) {
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

   @Override
   public void onPrepared(MediaPlayer player)
   {
	player.start() ;

   }


   @Override
   protected void onDestroy() {

    Log.d(GeofenceUtils.APPTAG, "onDestroy() called" ) ;
    super.onDestroy();
     if (mIsBound) {
        // Detach our existing connection.
        unbindService(mBackgroundAudioServiceConnection);
        mIsBound = false;
    }

    stopUpdates();


   /* 
    HashSet<String> gfIds = mGeofencePrefs.getGeofenceIds() ;
    if(gfIds != null )
    {
      for(Iterator<String> i = gfIds.iterator() ; i.hasNext();)
      {
         String id = i.next() ;
	 
         SimpleGeofence gf = mGeofencePrefs.getGeofence(id) ;
         Log.d(GeofenceUtils.APPTAG, "onDestroy(): removing geofences " + gf.getId() ) ;
	 mGeofencePrefs.clearGeofence(gf.getId()) ;
	 mCurrentGeofences.remove(gf.toGeofence()) ;

      }
       mGeofencePrefs.clearGeofenceIds() ;

    }
    */

   }

   @Override
   public void onStop()
   {
    Log.d(GeofenceUtils.APPTAG, "onStop() called" ) ;

    mEditor.putInt("infected", this.infected) ;
    mEditor.commit() ; 
    super.onStop() ;


  }
  
    @Override
    public void onPause() {

        super.onPause();
	Log.d(GeofenceUtils.APPTAG, "onPause() called") ;

	mIsInFront = false ;
        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();
        mEditor.putInt("infected", this.infected) ;
        mEditor.commit();

        // TODO unregister broadcast receiver?
    }


    @Override
    public void onStart() {

        super.onStart();

        
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {

      

      if(mCurrentGeofences != null && mCurrentGeofences.size() > 0)
      {	
         ArrayList<String> currentGeofenceIds = new ArrayList<String>() ;
     
	for(int i = 0 ; i < mCurrentGeofences.size() ; i++)
	{
	   Geofence gf = mCurrentGeofences.get(i) ;
           Log.d(GeofenceUtils.APPTAG, "onSavedInstanceState saving current geofence: " + gf.getRequestId() ) ;
	   currentGeofenceIds.add(gf.getRequestId() ) ;
	
	}
      
	savedInstanceState.putStringArrayList("mCurrentGeofenceIds", mCurrentGeofenceIds) ;
      }
      
      

      ArrayList<String> convoStringArray = new ArrayList<String>() ;

      if (mConvos != null && mConvos.size() > 0)
      {
         for( Iterator<Convo> i = mConvos.values().iterator() ; i.hasNext();)
	 {
	    Convo convo = i.next() ;
            Log.d(GeofenceUtils.APPTAG, "onSavedInstanceState saving convo: " + convo.getName() ) ;
            convoStringArray.add(convo.toJSONString()) ;
	 }
	 savedInstanceState.putStringArrayList("mConvos", convoStringArray) ;

      }
   
      ArrayList<String> buildingStringArray = new ArrayList<String>() ;

      if (mBuildings != null && mBuildings.size() > 0)
      {
         for( Iterator<Building> i = mBuildings.values().iterator() ; i.hasNext();)
	 {
	    Building building = i.next() ;
            Log.d(GeofenceUtils.APPTAG, "onSavedInstanceState saving building: " + building.getName() ) ;
            buildingStringArray.add(building.toJSONString()) ;
	 }
	 savedInstanceState.putStringArrayList("mBuildings", buildingStringArray) ;
// TODO also store in mPrefs? Perhaps not necessary as read from file source anyway. 
      }

      if(mActiveConvo != null)
      {
         savedInstanceState.putString("mActiveConvo", mActiveConvo.toJSONString() ) ;
         savedInstanceState.putBoolean("mActivaConvoInProgress", mActiveConvoInProgress) ;
      }

      if(mActiveDialog != null)
      {
         savedInstanceState.putString("mActiveDialog", mActiveDialog.toJSONString() ) ;

         mEditor.putString("mActiveDialog", mActiveDialog.toJSONString() );
	 mEditor.putBoolean("mActiveDialogShowing", mActiveDialogShowing ) ;
         mEditor.commit();
         Log.d(GeofenceUtils.APPTAG, "onSavedInstanceState saving mActiveDialog: " ) ;
      }
      
     mEditor.putInt("infected", this.infected) ;
     mEditor.commit() ; 
     super.onSaveInstanceState(savedInstanceState);
   }


   public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
	// restoreInstanceState(savedInstanceState) ;
   } 

   private void restoreInstanceState(Bundle savedInstanceState)
   {
    // Restore state members from saved instance
        if(savedInstanceState == null )
	{
           Log.d(GeofenceUtils.APPTAG, "restoreInstanceState: savedInstanceState is null") ;
	   return ;

	}
     
     
     /*
        ArrayList<String> currentGeofenceIds = savedInstanceState.getStringArrayList("mCurrentGeofenceIds");
	if(currentGeofenceIds != null && currentGeofenceIds.size() > 0 && mCurrentGeofences == null )
	{
	   String[] gfids = new String[currentGeofenceIds.size()] ;

	   currentGeofenceIds.toArray(gfids) ;

	   for(int i = 0 ; i < gfids.length ; i++)
	   {
	     String gfID = gfids[i] ;
             Log.d(GeofenceUtils.APPTAG, "restoreInstanceState gf: " + gfID ) ;
	     
             SimpleGeofence gf = mGeofencePrefs.getGeofence(gfID);
             mCurrentGeofences.add(gf.toGeofence());

	   }
       }

       */
/*
       // TODO get mConvos from getJSONString and parse from JSON Str using getStingArray
        ArrayList<String> convoStringArray = savedInstanceState.getStringArrayList("mConvos");
        if(convoStringArray != null && convoStringArray.size() > 0  )
	{
            for(Iterator<String> i = convoStringArray.iterator() ; i.hasNext();)
	    {
                String convoStr = i.next() ;
		try
		{
			Convo convo = ConvoJSONParser.parseConvo(convoStr) ;
                	mConvos.put(convo.getName(), convo) ;
                        Log.d(GeofenceUtils.APPTAG, "restoreInstanceState convo: " + convo.getName() ) ;
			
		}catch(ParseException e)
		 {
			Log.e(GeofenceUtils.APPTAG, "restoreInstanceState: Could not recover convo state for convoStringArray " + convoStr ) ;
		 }

	    }

	}

        String mActiveConvoStr = savedInstanceState.getString("mActiveConvo") ;

	try
	{
                 	
           Log.d(GeofenceUtils.APPTAG, "restoreInstanceState mActiveConvoStr: " + mActiveConvoStr ) ;
           mActiveConvo = ConvoJSONParser.parseConvo(mActiveConvoStr) ;
	   mActiveConvoInProgress = savedInstanceState.getBoolean("mActiveConvoInProgress") ;

	}catch(ParseException e)
	{
           Log.w(GeofenceUtils.APPTAG, "restoreInstanceState: Could not recover mActiveConvo from string" + mActiveConvoStr ) ;
	}

        // restore mActiveDialog
	String mActiveDialogStr = savedInstanceState.getString("mActiveDialog") ;
	try
	{
	   Log.d(GeofenceUtils.APPTAG, "restoreInstanceState: mActiveDialog: " + mActiveDialogStr ) ;
	   mActiveDialog = ConvoJSONParser.parseDialog(mActiveDialogStr) ;

	}catch(ParseException e)
	{
           Log.w(GeofenceUtils.APPTAG, "restoreInstanceState: Could not parse mActiveDialog from string" + mActiveConvoStr ) ;
	}
*/
   }

   @Override
   public void onResume()
   {

      Log.d(GeofenceUtils.APPTAG, "onResume has been called") ;

      super.onResume();
      mIsInFront = true ;

      // If the app already has a setting for getting location updates, get it
      if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) 
      {
         Log.d(GeofenceUtils.APPTAG, "onResume: KEY_UPDATES_REQUESTED") ;
         mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
      }
      // Otherwise, turn off location updates until requested
      else 
      {
         Log.d(GeofenceUtils.APPTAG, "onResume: KEY_UPDATES_NOT_REQUESTED") ;
         mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
         mEditor.commit();
      }

      if(mPrefs.contains("infected"))
      {
	  this.infected = mPrefs.getInt("infected", 0) ;
      }


      LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
 
      // TODO should we test if started already?
      if(mActiveConvo != null && !mActiveConvoInProgress)
      {
         Toast.makeText(this, "onResume :ACTIVE Convo:" + mActiveConvo.getName(), Toast.LENGTH_SHORT).show();
         ConvoGeofenceVisitor geofenceVisitor = new ConvoGeofenceVisitor(mActiveConvo, mBackgroundAudioService, mActivity ) ;
         geofenceVisitor.visitConvo() ;
	 mActiveConvoInProgress = true ;
      }

      // try to restore mActiveDialog
      String mActiveDialogStr = mPrefs.getString("mActiveDialog", "NULLString") ;
      mActiveDialogShowing = mPrefs.getBoolean("mActiveDialogShowing", false) ;

      if(! "NULLString".equals(mActiveDialogStr))
      {
         try
         {
            Log.d(GeofenceUtils.APPTAG, "onResume: restore mActiveDialog: " + mActiveDialogStr ) ;
	    mActiveDialog = ConvoJSONParser.parseDialog(mActiveDialogStr) ;

         }catch(ParseException e)
	   {
              Log.w(GeofenceUtils.APPTAG, "onResume: Could not parse mActiveDialog from string" + mActiveDialogStr ) ;
	   }
      }
      else
      {
            Log.d(GeofenceUtils.APPTAG, "onResume: nothing to restore for mActiveDialog: " + mActiveDialogStr ) ;
      }


      if(mActiveDialog != null)
      {
                  
         Log.d(GeofenceUtils.APPTAG, "onResume: show Dialog:" + mActiveDialog ) ;
         GeofenceDialogFragment dialog =  GeofenceDialogFragment.newInstance(mActiveDialog) ; 
         if(! mActiveDialogShowing)
	 {
	    dialog.show(getSupportFragmentManager(), "GeofenceEventFragment") ;
            mActiveDialogFragment = dialog ;
	 }

      }   

      Time now = new Time() ;
      now.setToNow() ;
      long nowMillis = now.toMillis(false) ;
   

      // the list of current geofences is not empty - check if any expired
      if(mCurrentGeofences != null && mCurrentGeofences.size() > 0 )
      {
    	  Log.d(GeofenceUtils.APPTAG, "onResume: mCurrentGeofences.size() > 0") ; 

            HashSet<String> gfIds = mGeofencePrefs.getGeofenceIds() ;
            if(gfIds != null )
 	    {
	      for(Iterator<String> i = gfIds.iterator() ; i.hasNext();)
	      {
	         String id = i.next() ;
	         SimpleGeofence gf = mGeofencePrefs.getGeofence(id) ;
	         Log.d(GeofenceUtils.APPTAG, "onResume: geofence list not empty:checking gf" + gf.getId() + "  expiretime: " + gf.getExpirationTime() + " nowMillis:" + nowMillis + " expired: " + (gf.getExpirationTime() < nowMillis)) ;

	        if(gf.getExpirationTime() < nowMillis)
	        {
	          Log.d(GeofenceUtils.APPTAG, "OnResume Removed expired geofence " + gf.getId() ) ;
		  mGeofencePrefs.clearGeofence(gf.getId()) ;
		  mCurrentGeofences.remove(gf.toGeofence()) ;
	        }

	      }
	    }
	    else
	    {
                Log.e(GeofenceUtils.APPTAG, "onResume: mCurrentGeofences.size() > 0 BUT no geofenceIds in mGeofencePrefs!") ;

	    }
      }

      // list of current geofences is empty attempt to restore from shared prefs  
      if(mCurrentGeofences != null && mCurrentGeofences.size() == 0 )
      {
    	     Log.d(GeofenceUtils.APPTAG, "onResume mCurrentGeofences.size() == 0") ; 
             HashSet<String> gfIds = mGeofencePrefs.getGeofenceIds() ;

             if(gfIds != null && gfIds.size() > 0 )
	     {
	        for(Iterator<String> i = gfIds.iterator() ; i.hasNext();)
	        {
	           String id = i.next() ;
		   Log.d(GeofenceUtils.APPTAG, "onResume restore geofence " + id + " from mGeofencePrefs") ; 
		   SimpleGeofence gf = mGeofencePrefs.getGeofence(id) ;

	           if(gf == null || gf.getExpirationTime() < nowMillis)
		   {
		      
		      Log.d(GeofenceUtils.APPTAG, "onResume geofence " + id + " had expired (or is null) so clear from mGeofencePrefs") ; 
	              mGeofencePrefs.clearGeofence(id) ;

		   }
		   else
		   {
		      Log.d(GeofenceUtils.APPTAG, "onResume geofence " + id + " not expired so add to mCurrentGeofences") ; 
		      mCurrentGeofences.add(gf.toGeofence()) ;

		   }

	        }
	     } 
	     else
	     {
                Log.d(GeofenceUtils.APPTAG, "onResume: gfIds empty") ;

	     }
      }

       if(mCurrentGeofences != null && mCurrentGeofences.size() == 0)
       {
    	  Log.d(GeofenceUtils.APPTAG, "onResume mCurrentGeofences.size() STILL == 0 OBTAINING GEOFENCES" ) ; 
          mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;
     

          ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
	  if (networkInfo != null && networkInfo.isConnected()) 
	  {

	
//         	 new DownloadBackgroundAudioJSONTask().execute("https://dl.dropboxusercontent.com/u/58768795/ForgottonFutures/backgroundsdev.json");
  //       	 new DownloadConversationsAudioJSONTask().execute("https://dl.dropboxusercontent.com/u/58768795/ForgottonFutures/conversations.json");
    //     	 new DownloadBuildingsJSONTask().execute("https://dl.dropboxusercontent.com/u/58768795/ForgottonFutures/buildings.json");

                   
         	 new DownloadConversationsAudioJSONTask().execute("https://dl.dropboxusercontent.com/u/58768795/ForgottonFutures/conversationskai.json");
//           new DownloadBackgroundAudioJSONTask().execute("https://dl.dropboxusercontent.com/u/26331961/kai_backgrounds.json");

   //        new DownloadConversationsAudioJSONTask().execute("https://dl.dropboxusercontent.com/u/26331961/conversations.json");

          } 
	  else 
	  {
             Toast.makeText(this, "No network connection available for game logic",  Toast.LENGTH_SHORT).show();
	     Log.e(GeofenceUtils.APPTAG, "No network connection available for game logic.");

          }

       }

       if(mCurrentGeofences == null)
       {
          Log.e(GeofenceUtils.APPTAG, "onResume: mCurrentGeofences == null " ) ;

       }
    
       NotificationManager notificationManager = (NotificationManager)getSystemService(this.NOTIFICATION_SERVICE);
       notificationManager.cancelAll();


       Intent callingIntent = this.getIntent() ;
       String bird = callingIntent.getStringExtra("bird");
       Log.d(GeofenceUtils.APPTAG, "get string extra bird" + bird + " from intent " + callingIntent) ;
       if(bird != null && !"-1".equals(bird))
       {
          Log.d(GeofenceUtils.APPTAG, "recovered bird" ) ;
          callingIntent.putExtra("bird","-1" );	
          int countBirds = mPrefs.getInt("countbirds", 0 ) ;
          countBirds++ ;

          Log.d(GeofenceUtils.APPTAG, "countBirds:" + countBirds ) ;
          mEditor.putInt("countbirds", countBirds) ;
          mEditor.commit() ;
          onBirdCaptured() ; 
       }

       mLocationClient.connect();
}




  public void playSound(int sound, float fSpeed, int repeat) 
  {
  
      Log.d(GeofenceUtils.APPTAG, "playSound:" + sound);
      AudioManager mgr = (AudioManager)getSystemService(AUDIO_SERVICE);
      float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
      float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      float volume = streamVolumeCurrent / streamVolumeMax; 
      
      Log.d(GeofenceUtils.APPTAG, "playSound: " + sound + " volume: " + volume);
      mSoundPool.play(sound, volume, volume, 1, repeat, fSpeed);
  }



@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.webview_activity_actions, menu);
    return super.onCreateOptionsMenu(menu);
}


@Override
public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
        case R.id.get_location:
            getLocation();
            return true;
	case R.id.framemarkers:
	    framemarkers();
            return true ;
        case R.id.switch_map:
	    switchMap() ;
            return true ;
        case R.id.toggle_achievements:
            toggleAchievements() ;
            return true ;
        default:
            return super.onOptionsItemSelected(item);
    }
}

public void toggleAchievements()
{
   if(mAchievementsShowing && mActiveMap == 1)
   {
      webview.loadUrl("javascript:removeLegend();");
      mAchievementsShowing = false ;
      return ;
   }
   
   if(!mAchievementsShowing && mActiveMap == 1)
   {
      webview.loadUrl("javascript:showLegend();");
      mAchievementsShowing = true ;
      int countBirds = mPrefs.getInt("countbirds", 0 ) ;
      Log.d(GeofenceUtils.APPTAG, "toggleAchievements countBirds:" + countBirds) ;
      for(int i = 0 ; i < countBirds ; i++ )
      {
         webview.loadUrl("javascript:addBird();");
      }
      if(getInfected() > 0 ) 
      {
         webview.loadUrl("javascript:addDisease('" + getUniqueUserId() + "');");
      }
      return ;
   }

   if(!mAchievementsShowing && mActiveMap == 0)
   {
           webview.setWebViewClient(new WebViewClient() {

           public void onPageFinished(WebView view, String url) {
                  view.loadUrl("javascript:showLegend();");
                  mAchievementsShowing = true ;
                  int countBirds = mPrefs.getInt("countbirds", 0 ) ;
                  Log.d(GeofenceUtils.APPTAG, "onBirdCaptured.WebViewClient countBirds:" + countBirds) ;
                  for(int i = 0 ; i < countBirds ; i++ )
                  {
                      view.loadUrl("javascript:addBird();");
                  }
                  if(getInfected() > 0 ) 
                  {
                    webview.loadUrl("javascript:addDisease('" + getUniqueUserId() + "');");
                  }
      return ;

            }
           });
      switchMap() ;
      return ;
   }

   if(mAchievementsShowing && mActiveMap == 0)
   {
      return ;
   }
}


public void onBirdCaptured()
{
     Toast.makeText(this,"Congratulations you captured a bird for Robot Bob",Toast.LENGTH_SHORT).show();
      
      if(mActiveMap == 0 )
      { 
           webview.setWebViewClient(new WebViewClient() {

           public void onPageFinished(WebView view, String url) {
                  view.loadUrl("javascript:showLegend();");
                  mAchievementsShowing = true ;
                  int countBirds = mPrefs.getInt("countbirds", 0 ) ;
                  Log.d(GeofenceUtils.APPTAG, "onBirdCaptured.WebViewClient countBirds:" + countBirds) ;
                  for(int i = 0 ; i < countBirds ; i++ )
                  {
                      view.loadUrl("javascript:addBird();");
                  }
                  // TODO if count bird == 3 play 
            }
           });
           switchMap() ;
           return ;
      }
      webview.loadUrl("javascript:showLegend();");
      mAchievementsShowing = true ;
      int countBirds = mPrefs.getInt("countbirds", 0 ) ;
      Log.d(GeofenceUtils.APPTAG, "onBirdCaptured countBirds:" + countBirds) ;
      for(int i = 0 ; i < countBirds ; i++ )
      {
         webview.loadUrl("javascript:addBird();");
      }

}

public void switchMap()
{
     if(mActiveMap == 0)
     {
        webview.loadUrl("file:///android_asset/html/threshold3.html");
        Toast.makeText(this,"Game Map",Toast.LENGTH_SHORT).show();
        mActiveMap = 1 ;
     }
     else if(mActiveMap == 1)
     {
        webview.loadUrl("file:///android_asset/html/threshold2.html");
        Toast.makeText(this,"Venue Map",Toast.LENGTH_SHORT).show();
        mActiveMap = 0 ; 
     }

}

public void getLocation()
{

    webview.loadUrl("javascript:getLocation();");

}

public void framemarkers()
{
 
  Log.d(GeofenceUtils.APPTAG, "Framemarkers called" ) ;
  // Toast.makeText(this,"Framemarkers called",Toast.LENGTH_SHORT).show();
  Intent intent = new Intent(this, com.example.android.framemarkers.FrameMarkers.class);

  startActivity(intent);

}

  private void startUpdates() {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

  private void stopUpdates() {
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }




    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
          }

      private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
      }



    @Override
    public void onLocationChanged(Location location) {

      // Display the current location in the UI
       String latlon = LocationUtils.getLatLngJSON(this, location);

     // call javascript method to update location
     webview.loadUrl("javascript:onLocationUpdateP('" + latlon +"');");
    
     
     if(mIsBound)
     {

        if(mBackgroundAudioService!=null)
	{ 
		
	    if(mCurrentGeofences != null && mCurrentGeofences.size() > 0)
	    {
	       for(int i = 0 ; i < mCurrentGeofences.size() ; i++)
	       {
	          Geofence gf = mCurrentGeofences.get(i) ;
	          String trackID = gf.getRequestId() ;
		  if(trackID.startsWith("CONVO")) return ;

		  //  get SimpleGeofence object and lon/lat 
                  SimpleGeofence sgf = mGeofencePrefs.getGeofence(trackID);
		  if(sgf == null)
		  {
		     Log.e(GeofenceUtils.APPTAG, "Could not retrieve geofence from mGeofencePrefs id: " + trackID ) ;      
		     return ; 

		  }

		  boolean varyVolume = sgf.getVaryVolume() ;


                  float volume = getVolumeFromDistanceBetween(location, sgf) ;

       		  if(mBackgroundAudioService!=null)
		  {
		     if(varyVolume)
		     { // note changeVolume will duck if forground playing
	               mBackgroundAudioService.changeVolume(trackID, volume) ;
		       Log.d(GeofenceUtils.APPTAG, "change or duck volume for track " + trackID + " to:" + volume);
		     }
                     else
                     {
                       volume = 0.9f ;
		       Log.d(GeofenceUtils.APPTAG, "not vary volume for track " + trackID + " at default volume:" + volume);
	               mBackgroundAudioService.changeVolume(trackID, volume) ;

                     }
	          }

	       }
	    }
		
	}
	
     }

  }

private float getVolumeFromDistanceBetween(Location location, SimpleGeofence sgf)
{


  double gfLongitude = sgf.getLongitude() ;
  double gfLatitude = sgf.getLatitude() ;
  double latitude = location.getLatitude() ;
  double longitude = location.getLongitude() ;
  float[] distanceCalc = new float[2];
  float radius = sgf.getRadius() ;
  location.distanceBetween(latitude, longitude, gfLatitude, gfLongitude, distanceCalc) ;
 

  if(distanceCalc.length > 0 )
  {
     	 Log.d(GeofenceUtils.APPTAG, "onLocationChanged: distance to GF " + sgf.getId() + " is: " + distanceCalc[0] ) ;
        if(distanceCalc[0] < 25) return 0.9f ;
        if(distanceCalc[0] < 40) return 0.65f ;
        if(distanceCalc[0] < 75) return 0.5f ;
        if(distanceCalc[0] < 100) return 0.4f ; 

        float maxLog = (float) Math.log10(radius)  ;
	float logDist = (float) Math.log10((distanceCalc[0]  + 1))  ; // add 1 to ensure vol always > 0
        float volumeScalar = 1 - ( logDist / maxLog )  ;
        return volumeScalar ;
	                
  }
  return 0.35f ;

}



private boolean servicesConnected() {

// Check that Google Play services is available
        int resultCode =              GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode)
	   {

            // Continue
            return true;
	   }


	Toast.makeText(this, "Google Play Services NotAvailable",  Toast.LENGTH_SHORT).show();

	return false;

}

    @Override
    public void onConnected(Bundle dataBundle)
    {																								
    //    Toast.makeText(this, "WebViewActivity On Connected called ",Toast.LENGTH_SHORT).show();
          startUpdates() ;

     
       Log.d(GeofenceUtils.APPTAG, "WebViewActivity:onConnected " + mCurrentGeofences ) ;
    }

     private void addBuildingsGeofences(String buildingsJSONStr)
     {
        Log.d(GeofenceUtils.APPTAG, "adding buildings geofences" ) ;
        Building[] buildings ;
        try
        {
           buildings = BuildingJSONParser.parseBuildingsArray(buildingsJSONStr) ;
        }catch(ParseException e) 
         {
	   Log.e(GeofenceUtils.APPTAG, "Could not parse Buildings Array from String: " + buildingsJSONStr + " Caused by: " +  e.getMessage()) ;
           Toast.makeText(this, "Could not parse buildings.json file",  Toast.LENGTH_LONG).show();
           return ; 

         }
         ArrayList buildingGeofences = new ArrayList<Geofence>(); 
         for(int i = 0 ; i < buildings.length ; i++)
	 {
            Building building = buildings[i] ;
	    Log.d(GeofenceUtils.APPTAG, "processing building: " + building ) ;
            String buildingName = building.getName() ;
	    GeofenceBuilding gfBuilding = building.getGeofenceBuilding() ;
	    String buildingId = Integer.toString(gfBuilding.getId()) ;

            SimpleGeofence geofence = new SimpleGeofence(
            "BUILDING_" + buildingId ,
            gfBuilding.getLatitude(),
            gfBuilding.getLongitude(),  
            gfBuilding.getRadius(), 
            gfBuilding.getDuration(), // expiration time
            gfBuilding.getTransitions() );

            mBuildings.put("BUILDING_" + buildingId, building) ;
       	    mGeofencePrefs.setGeofence(buildingId, geofence);
            mCurrentGeofences.add(geofence.toGeofence());
            buildingGeofences.add(geofence.toGeofence()) ;

     }

       // Start the request. Fail if there's already a request in progress
        try {
               // add geofences
	       mGeofenceRequester.addGeofences(buildingGeofences);
	       Log.d(GeofenceUtils.APPTAG, "requesting adding of building geofence list items") ;

            } catch (UnsupportedOperationException e) 
	      {
                 // Notify user that previous request hasn't finished.
                 Toast.makeText(this, R.string.add_geofences_already_requested_error,  Toast.LENGTH_LONG).show();
              }
     } 


     private void addConversationGeofences(String conversationsJSONStr)
     {

        Log.d(GeofenceUtils.APPTAG, "adding Conversations geofences" ) ;

	Convo[] conversations ;

        try
	{
 		conversations = ConvoJSONParser.parseConvoArray(conversationsJSONStr) ;
		Log.d(GeofenceUtils.APPTAG, "parsed conversations string into " + conversations.length) ;

	}catch (ParseException e)
	 {
	   Log.e(GeofenceUtils.APPTAG, "Could not parse convoArray from String: " + conversationsJSONStr + " Caused by: " +  e.getMessage()) ;
           Toast.makeText(this, "Could not parse conversation file",  Toast.LENGTH_LONG).show();
           return ; 
	 }
         ArrayList convoGeofences = new ArrayList<Geofence>();
         for(int i = 0 ; i < conversations.length ; i++)
	 {
            Convo convo = conversations[i] ;
	    Log.d(GeofenceUtils.APPTAG, "processing Convo: " + convo ) ;
            String convoName = convo.getName() ;
	    GeofenceAudio gfAudio = convo.getGeofenceAudio() ;
	    

            SimpleGeofence geofence = new SimpleGeofence(
            "CONVO_" + convoName,
            gfAudio.getLatitude(),
            gfAudio.getLongitude(),  
            gfAudio.getRadius(), 
            gfAudio.getDuration(), // expiration time
            gfAudio.getTransitions() );

            mConvos.put("CONVO_" + convoName, convo) ;
       	    mGeofencePrefs.setGeofence(convoName, geofence);
            mCurrentGeofences.add(geofence.toGeofence());
            convoGeofences.add(geofence.toGeofence());
	 }			         	
				
       // Start the request. Fail if there's already a request in progress
        try {
               // add geofences
               // TODO add just convo geofences
	       mGeofenceRequester.addGeofences(convoGeofences);
	       Log.d(GeofenceUtils.APPTAG, "requesting adding of convo geofence list items") ;

            } catch (UnsupportedOperationException e) 
	      {
                 // Notify user that previous request hasn't finished.
                 Toast.makeText(this, R.string.add_geofences_already_requested_error,  Toast.LENGTH_LONG).show();
              }
      
       
    }
   
   
   
   
   
   
   private void addBackgroundGeofences(String backgroundJSONStr)
     {

	if(backgroundJSONStr == null) return ;
        JSONArray jArray = null ;
        Log.d(GeofenceUtils.APPTAG, "adding BackgroundGeofences")  ;

    	try {

        	JSONObject jBackgrounds = new JSONObject(backgroundJSONStr);	
	
        	jArray = jBackgrounds.getJSONArray("backgrounds");

    	}catch (JSONException e) 
	{
		Log.e(GeofenceUtils.APPTAG, "Error parsing background JSON Str"+ e) ;
		return ;

	}

        ArrayList backgroundGeofences = new ArrayList<Geofence>();
	for (int i=0; i < jArray.length(); i++)
	{
                JSONObject backgroundObject = null;
	        JSONObject geofenceAudioObject = null;

    		try {
        		backgroundObject = jArray.getJSONObject(i);
                   
			geofenceAudioObject = backgroundObject.getJSONObject("geofence_audio");  
        		// Pulling items from the array
			if(geofenceAudioObject!=null)
			{ // tag geofence_audio
        			int id = geofenceAudioObject.getInt("id");
        			double lat = geofenceAudioObject.getDouble("lat");
        			double lon = geofenceAudioObject.getDouble("lon");
        			float radius = (float)geofenceAudioObject.getDouble("radius");
        			long duration = geofenceAudioObject.getLong("duration");
                                JSONArray transitionsArray = geofenceAudioObject.getJSONArray("transitions") ;
				int transitions = 0;
				for(int j=0; j < transitionsArray.length() ; j++)
				{
					String transitionStr = transitionsArray.getString(j) ;
					int transition = 0 ;
				        if("ENTER".equals(transitionStr))
							transition = Geofence.GEOFENCE_TRANSITION_ENTER ;
					else if("EXIT".equals(transitionStr))
							transition = Geofence.GEOFENCE_TRANSITION_EXIT ;
				

					transitions = transitions | transition ;
				}

				String track = geofenceAudioObject.getString("track");
				boolean loop = geofenceAudioObject.getBoolean("loop") ;
                                boolean varyVolume = geofenceAudioObject.getBoolean("vary_volume") ;

                               
		 		Log.d(GeofenceUtils.APPTAG, "Parsed geofence audio object: id: " + id +	
				" lat: " + lat + " lon:" + lon + " radius:" + radius + 
				" duration:" + duration + " transitions: " + transitions + " track:" + track + 
				" loop:" + loop + " vary_volume:" + varyVolume) ;


	                        SimpleGeofence geofence = new SimpleGeofence(
                                 track,
                                 lat, // Latitude
            			 lon,  // Longitude
            			 radius, // radius
            			 // expiration time
            			 duration,
				 loop,
				 varyVolume,
            			 transitions);
                                // TODO set stored prefs values for track, loop, vary_volume
            			mGeofencePrefs.setGeofence(track, geofence);
       	    			mCurrentGeofences.add(geofence.toGeofence());
       	    			backgroundGeofences.add(geofence.toGeofence());
				
			}
			else
			{
				Log.e(GeofenceUtils.APPTAG, "geofenceAudio object is null") ;
			}
    		    }catch (JSONException e) {
        	   	   Log.e(GeofenceUtils.APPTAG, "Error parsing JSON geofence audio object " + geofenceAudioObject + e ) ;	
    			}
	}
           // Start the request. Fail if there's already a request in progress
           try {
               // Try to add geofences
                // TODO add just backgound geofences
               mGeofenceRequester.addGeofences(backgroundGeofences);
	       Log.d(GeofenceUtils.APPTAG, "requesting adding of background geofence list items") ;

               } catch (UnsupportedOperationException e) {
                 // Notify user that previous request hasn't finished.
                 Toast.makeText(this, R.string.add_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
                 }
      
       
    }

    @Override
    public void onDisconnected()
    {


    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

     Toast.makeText(this, "Connection Failed.",Toast.LENGTH_SHORT).show();


    }


   protected String getLocalJSON(String uri)
   {
	DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
	HttpGet httpget = new HttpGet(uri);
	// Depends on your web service
	httpget.setHeader("Content-type", "application/json");

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



   protected String getWebJSON(String uri)
   {
	DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
	HttpPost httppost = new HttpPost(uri);
	// Depends on your web service
	httppost.setHeader("Content-type", "application/json");

	InputStream inputStream = null;
	String result = null;
	try 
	{
    		HttpResponse response = httpclient.execute(httppost);           
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


   private class DownloadBuildingsJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... uri) {
              
                return getLocalJSON(uri[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            addBuildingsGeofences(result);
       }
    }

   private class DownloadConversationsAudioJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... uri) {
              
                return getLocalJSON(uri[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            addConversationGeofences(result);
       }
    }



   private class DownloadBackgroundAudioJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... uri) {
              
                return getLocalJSON(uri[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            addBackgroundGeofences(result);
       }
    }


  /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver  {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    ||
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {


		// Toast.makeText(context, "GeofenceSampleReceiver:handleGeofenceStatus:" + intent, Toast.LENGTH_SHORT).show() ;
               Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver:handleGeofenceStatus: " + intent );

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) 
	{

           Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver:handleGeofenceTransition: " + intent.getStringExtra("TRANSITION_TYPE") );
           Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver:handleGeofenceTransition: " + intent.getStringArrayExtra("GEOFENCE_IDS") );
	      
	   String transitionType = intent.getStringExtra("TRANSITION_TYPE") ;
	   String[] triggerGeofenceIds = intent.getStringArrayExtra("GEOFENCE_IDS") ;
              
	   for(int i = 0 ; i < triggerGeofenceIds.length ; i++)
           {
	      String geofenceId = triggerGeofenceIds[i] ;
	      Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition: " + geofenceId ) ;

              if(geofenceId.startsWith("CONVO")) // TODO not good solution need to get a geofence convo type somehow 
	      {      
		   Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition: processing convo " + geofenceId) ;
		   handleConversationTransition(geofenceId, transitionType, context) ; 
	      }
              else if(geofenceId.startsWith("BUILDING")) // TODO not good solution need to get a geofence convo type somehow 
	      {      
		   Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition: processing building " + geofenceId) ;
		   handleBuildingTransition(geofenceId, transitionType, context) ; 
	      }
	      else 
	      {
			   Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition: processing background audio" + geofenceId ) ;
			   handleBackgroundAudioTransition(geofenceId, transitionType) ;
	      }
            }

	 }


         private void handleBuildingTransition(String geofenceId, String transitionType, Context context)
	 {
	    if("Entered".equals(transitionType) || "Exited".equals(transitionType))
	    {

              String transitionFlag = "Entered".equals(transitionType) ? "ENTER" : "EXIT";  ;  
		   Building building = mBuildings.get(geofenceId) ;
		   
		   if(building != null)
		   {
                       Toast.makeText(context, transitionType + " building:" + building.getName(), Toast.LENGTH_SHORT).show();
		       BuildingGeofenceVisitor buildingVisitor = new BuildingGeofenceVisitor(building,  mActivity, transitionFlag ) ;
		      building.accept(buildingVisitor) ; 

		   }
	    }

	 }



         private void handleConversationTransition(String geofenceId, String transitionType, Context context)
	 {
	    if("Entered".equals(transitionType))
	    {
                   
		   mActiveConvo = mConvos.get(geofenceId) ;
		   
		   if(mActiveConvo != null)
		   {

                       Toast.makeText(context, "ACTIVE Convo:" + mActiveConvo.getName(), Toast.LENGTH_SHORT).show();
		       ConvoGeofenceVisitor geofenceVisitor = new ConvoGeofenceVisitor(mActiveConvo, mBackgroundAudioService, mActivity ) ;
		       geofenceVisitor.visitConvo() ;
		       mActiveConvoInProgress = true ;

		   }


	    }
            else if("Exited".equals(transitionType))
            {
		   mActiveConvo = null ; 
                   mActiveConvoInProgress = false ;
	    }

	 }


	 private void handleBackgroundAudioTransition(String geofenceId, String transitionType)
	 {
            SimpleGeofence sgf = mGeofencePrefs.getGeofence(geofenceId);
            boolean looping = sgf.getLooping() ;  // TODO subclass SimpleGeofence  

            float volume = 0.01f ;

	    if(mLocationClient != null && mLocationClient.isConnected())
	    {
               Location location = mLocationClient.getLastLocation() ;
	       volume = getVolumeFromDistanceBetween(location, sgf) ;
	       Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition VOLUME: " + volume);
            }


	    if("Entered".equals(transitionType))
	    {
               Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition ENTERED: " + geofenceId ) ;
     	       if(mIsBound)
     	       {
                  if(mBackgroundAudioService!=null)
		  {	
	             mBackgroundAudioService.play(geofenceId, looping, volume) ; 
	             Log.d(GeofenceUtils.APPTAG, "playing audio: " + geofenceId + " with looping:" + looping);
		  }
     	       }

	    }
            else if("Exited".equals(transitionType))
	    {
	       Log.d(GeofenceUtils.APPTAG, "GeofenceSampleReceiver.handleGeofenceTransition EXITED: " + geofenceId ) ;
     	       if(mIsBound)
     	       {
                  if(mBackgroundAudioService!=null)
		  {	
	             mBackgroundAudioService.stop(geofenceId) ;
	             Log.d(GeofenceUtils.APPTAG, "stop audio: " + geofenceId);
		  }
     	       }

            }
       	 }
       

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }

	

	
    }
}
