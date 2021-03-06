package com.example.android.location ;

import android.app.Service ;
import android.media.MediaPlayer ;
import android.media.MediaPlayer.OnCompletionListener ;
import android.content.Intent ;
import android.app.PendingIntent ;
import android.util.Log ;
import android.os.Binder ;
import android.os.IBinder ;
import android.widget.Toast ;
import android.app.NotificationManager ;
import android.app.Notification ;
import android.util.Log ;

import com.example.android.location.R ;
import com.example.android.geofence.GeofenceUtils ;

import java.lang.CharSequence ;
import java.util.HashMap ;
import java.util.Iterator ;


public class BackgroundAudioService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    
    public static final String ACTION_PLAY = "com.example.android.ACTION_PLAY" ;
    public static final String EXTRA_TRACK = "com.example.android.TRACK" ;
    private MediaPlayer mMediaPlayer = null;
    private NotificationManager mNM;
    private Notification notification ;
    public final IBinder mBinder = new LocalBinder();
    private String track ;
    private MediaPlayer currentForegroundPlayer ; 
    private HashMap<String, MediaPlayer> playing = new HashMap<String, MediaPlayer>() ;
    private boolean foregroundPlaying ;

    @Override 
    public void onCreate()
    {
	Log.d(GeofenceUtils.APPTAG, "BackgroundAudioService:onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // track = intent.getStringExtra(EXTRA_TRACK) ; 
	// TODO switch on track // check Extra allow us to specify resource id directly?
        Log.d(GeofenceUtils.APPTAG, "BackgroundAudioService:onStartCommand " + intent) ;

       // mMediaPlayer = MediaPlayer.create(this, R.raw.backgroundrobotfactory) ; 
	if (intent.getAction().equals(ACTION_PLAY)) {
	//    mMediaPlayer.start() ;
        }
	
	showNotification();
	return START_NOT_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        mp.reset() ;
	
	Log.e(GeofenceUtils.APPTAG, "Media in error state") ;
	return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
	mp.release() ;
	mp = null ;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
 
        // stopForeground(true) ; 
        
	if(mMediaPlayer!=null)
	{
		mMediaPlayer.stop() ;
		mMediaPlayer.release() ;
	}

	if(playing != null)
	{
           playing.clear();
	}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        
        Log.d(GeofenceUtils.APPTAG, "BackgroundAudioService:showNotification") ;	
	track = "Robot Factory" ;

         PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), com.example.android.location.WebViewActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification();
        notification.tickerText = "Playing: " + track ;
        notification.icon = R.drawable.ic_action_play ;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), "forgotton futures",
                "Playing: " + track, pi);
        startForeground(1571, notification);

    }

    public void play(String track, boolean looping, float volume)
    {
        
        MediaPlayer  aMediaPlayer = MediaPlayer.create(this, getTrackId(track)) ; 
        // TODO add onCompleteListener so can remove from Playing 
	// TODO work out how to add track info to media player so we can remove from playing 
	if(! aMediaPlayer.isPlaying())
	{
	   aMediaPlayer.setLooping(looping);
	   aMediaPlayer.setVolume(volume, volume) ;
	   aMediaPlayer.start() ;
	   playing.put(track, aMediaPlayer) ;
	}

    }

    public void play(String track, boolean looping)
    {
        
        MediaPlayer  aMediaPlayer = MediaPlayer.create(this, getTrackId(track)) ; 
        aMediaPlayer.setOnCompletionListener(this) ;
	
	if(! aMediaPlayer.isPlaying())
	{
	   aMediaPlayer.setLooping(looping);
	   aMediaPlayer.start() ;
	   playing.put(track, aMediaPlayer) ;
	}

    }
   
    public void playForeground(String track, MediaPlayer.OnCompletionListener completionListener, MediaPlayer.OnErrorListener errorListener)
    {

        MediaPlayer  aMediaPlayer = MediaPlayer.create(this, getTrackId(track)) ; 
        aMediaPlayer.setOnCompletionListener(completionListener) ;
	aMediaPlayer.setOnErrorListener(errorListener); 

	if(! aMediaPlayer.isPlaying())
	{
	   aMediaPlayer.start() ;
	   this.currentForegroundPlayer = aMediaPlayer ; // keep a reference to stop over eager Garbage Collection
           this.foregroundPlaying = true ;
           if(playing != null && playing.size() > 0)
           {
              for(Iterator<MediaPlayer> i = playing.values().iterator() ; i.hasNext(); )
              {
                MediaPlayer player = i.next() ;
                if(player!=null) 
                {
                    player.setVolume(0.3f, 0.3f) ;
                    Log.d(GeofenceUtils.APPTAG, "ducked player:" + player) ;
                }
              }
           }
	}

    }



    

    public void play(String track)
    {
        
        MediaPlayer  aMediaPlayer = MediaPlayer.create(this, getTrackId(track)) ; 
        aMediaPlayer.setOnCompletionListener(this) ;

	if(! aMediaPlayer.isPlaying())
	{
	  
	   aMediaPlayer.start() ;
	   playing.put(track, aMediaPlayer) ;
	}

    }

    public void changeVolume(String track, float volume)
    {
  
         
         MediaPlayer trackPlayer = playing.get(track) ;
	 
	 if(trackPlayer != null && trackPlayer.isPlaying() )
	 {

             if(!foregroundPlaying)
             {
	        trackPlayer.setVolume(volume, volume) ;
             }
             else
             {
                trackPlayer.setVolume(0.3f, 0.3f) ;
                Log.d(GeofenceUtils.APPTAG, "BackgroundAudioPlayer: change volume ducking background track to 0.3f" ) ; 
             }
	 }

    }


    public void foregroundStopped()
    {
         if( this.currentForegroundPlayer != null) this.currentForegroundPlayer.release() ;
         // this.currentForegroundPlayer = null ; 
         this.foregroundPlaying = false ;
    }


    public void stop(String track)
    {    
          if(playing.containsKey(track))
	  {
          	MediaPlayer aMediaPlayer = playing.get(track) ;
	  	if(aMediaPlayer!=null)
                {
                   aMediaPlayer.stop() ;
	  	   aMediaPlayer.release() ;
                }
	  	playing.remove(track) ;
	  	aMediaPlayer = null ;
	  }

    }

    public class LocalBinder extends Binder {
        public BackgroundAudioService getService() 
	{
            return BackgroundAudioService.this;
        }
    }
    
    private int getTrackId(String track)
    {
       if("convo1ablock1".equals(track))
       {
	  return R.raw.convo1ablock1 ;
       }
       else if("convo1ablock2".equals(track))
       {
	  return R.raw.convo1ablock2 ;
       }
       else if("convo1ablock3".equals(track))
       {
	  return R.raw.convo1ablock3 ;
       }
       else if("convo1ablock4".equals(track))
       {
	  return R.raw.convo1ablock4 ;
       }
       else if("convo1ablock5".equals(track))
       {
	  return R.raw.convo1ablock5 ;
       }
       else if("convo1bblock6".equals(track))
       {
	  return R.raw.convo1bblock6 ;
       }
       else if("convo1bblock7".equals(track))
       {
	  return R.raw.convo1bblock7 ;
       }
       else if("convo1bblock8".equals(track))
       {
	  return R.raw.convo1bblock8 ;
       }
       else if("convo1cblocks9".equals(track))
       {
          return -1 ;
	  // return R.raw.convo1cblocks9 ;
       }
       else if("notificationbird".equals(track))
       {
	  return R.raw.notificationbird ;
       }
       else if(track != null &&  track.startsWith("notificationdetect"))
       {
	  return R.raw.notificationdetect ;
       }

       return 0 ;
    }

}


