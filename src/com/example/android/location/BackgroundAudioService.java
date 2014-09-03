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

        mMediaPlayer = MediaPlayer.create(this, R.raw.backgroundrobotfactory) ; 
	if (intent.getAction().equals(ACTION_PLAY)) {
	    mMediaPlayer.start() ;
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
       if("backgroundambientarea".equals(track) || "backgroundambientarea2".equals(track) || "backgroundambientarea3".equals(track))
       {
          return R.raw.backgroundambientarea ;
       }
       else if("backgroundchase".equals(track))
       {
	  return R.raw.backgroundchase ;
       }
       else if("backgroundcomsumersphere".equals(track) || "backgroundcomsumersphere2".equals(track) || "backgroundcomsumersphere3".equals(track) || "backgroundcomsumersphere4".equals(track) )
       {
	  return R.raw.backgroundcomsumersphere ;
       }
       else if("backgroundfunderdome".equals(track) || "backgroundfunderdome2".equals(track) || "backgroundfunderdome3".equals(track) || "backgroundfunderdome4".equals(track))
       {
	  return R.raw.backgroundfunderdome ;
       }
       else if("backgroundpwpintro".equals(track))
       {
	  return R.raw.backgroundpwpintro ;
       }
       else if("backgroundpwp".equals(track) || "backgroundpwp2".equals(track) || "backgroundpwp3".equals(track) || "backgroundpwp4".equals(track) )
       {
	  return R.raw.backgroundpwp ;
       }
       else if("backgroundrobotfactory".equals(track) || "backgroundrobotfactory2".equals(track) || "backgroundrobotfactory3".equals(track) )
       {
	  return R.raw.backgroundrobotfactory ;
       }
       else if("backgroundskylark".equals(track) || "backgroundskylark2".equals(track) || "backgroundskylark3".equals(track) )
       {
	  return R.raw.backgroundskylark ;
       }
       else if("backgroundspaceport".equals(track) || "backgroundspaceport2".equals(track) || "backgroundspaceport3".equals(track) )
       {
	  return R.raw.backgroundspaceport ;
       }
       else if("convo1ablock1".equals(track))
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
       else if("convo2ablock1".equals(track))
       {
	  return R.raw.convo2ablock1 ;
       }
       else if("convo2ablock2".equals(track))
       {
	  return R.raw.convo2ablock2 ;
       }
       else if("convo2ablock3".equals(track))
       {
	  return R.raw.convo2ablock3 ;
       }
       else if("convo2ablock4".equals(track))
       {
	  return R.raw.convo2ablock4 ;
       }
       else if("convo2ablock5".equals(track))
       {
	  return R.raw.convo2ablock5 ;
       }
       else if("convo2bblock6".equals(track))
       {
          return -1 ;
	  // return R.raw.convo2bblock6 ;
       }
       else if("convo2bblock7".equals(track))
       {
          return -1 ;
	  // return R.raw.convo2bblock7 ;
       }
       else if("convo2cblock8".equals(track))
       {
          return -1 ;
	  // return R.raw.convo2cblock8 ;
       }
       else if("convo2cblock9".equals(track))
       {
          return -1 ;
	  // return R.raw.convo2cblock9 ;
       }
       else if("convo2dblock10".equals(track))
       {
          return -1 ; 
	  // return R.raw.convo2dblock10 ;
       }
       else if("convo2dblock11".equals(track))
       {
          return -1 ;
	  // return R.raw.convo2dblock11 ;
       }
       else if("convo2dblock12".equals(track))
       {
          return -1 ;
	  // return R.raw.convo2dblock12 ;
       }
       else if("convo3ablock10".equals(track))
       {
	  return R.raw.convo3ablock10 ;
       }
       else if("convo3ablock1".equals(track))
       {
	  return R.raw.convo3ablock1 ;
       }
       else if("convo3ablock2".equals(track))
       {
	  return R.raw.convo3ablock2 ;
       }
       else if("convo3ablock3".equals(track))
       {
	  return R.raw.convo3ablock3 ;
       }
       else if("convo3ablock4".equals(track))
       {
	  return R.raw.convo3ablock4 ;
       }
       else if("convo3ablock5".equals(track))
       {
	  return R.raw.convo3ablock5 ;
       }
       else if("convo3ablock6".equals(track))
       {
	  return R.raw.convo3ablock6 ;
       }
       else if("convo3ablock7".equals(track))
       {
	  return R.raw.convo3ablock7 ;
       }
       else if("convo3ablock8".equals(track))
       {
	  return R.raw.convo3ablock8 ;
       }
       else if("convo3ablock9".equals(track))
       {
	  return R.raw.convo3ablock9 ;
       }
       else if("convo4ablock1".equals(track))
       {
          return -1 ;
	  // return R.raw.convo4ablock1 ;
       }
       else if("convo4ablock2".equals(track))
       {
          return -1;
	  // return R.raw.convo4ablock2 ;
       }
       else if("convo4ablock3".equals(track))
       { 
          return -1 ;
	  // return R.raw.convo4ablock3 ;
       }
       else if("convo4ablock4".equals(track))
       {
          return -1;
	  // return R.raw.convo4ablock4 ;
       }
       else if("convo4ablock5".equals(track))
       {
          return -1;
	  // return R.raw.convo4ablock5 ;
       }
       else if("convo4bblock10".equals(track))
       {
          return -1 ;
	  // return R.raw.convo4bblock10 ;
       }
       else if("convo4bblock6".equals(track))
       { 
          return -1 ;
	  // return R.raw.convo4bblock6 ;
       }
       else if("convo4bblock7".equals(track))
       {
            return -1 ;
	  // return R.raw.convo4bblock7 ;
       }
       else if("convo4bblock8".equals(track))
       {
          return -1 ;
	  // return R.raw.convo4bblock8 ;
       }
       else if("convo4bblock9".equals(track))
       {
          return -1 ;
	  // return R.raw.convo4bblock9 ;
       }
       else if("convo5ablock1".equals(track))
       {
	  return R.raw.convo5ablock1 ;
       }
       else if("convo5ablock2".equals(track))
       {
	  return R.raw.convo5ablock2 ;
       }
       else if("convo5ablock3".equals(track))
       {
	  return R.raw.convo5ablock3 ;
       }
       else if("convo5ablock4".equals(track))
       {
	  return R.raw.convo5ablock4 ;
       }
       else if("convo5ablock5".equals(track))
       {
	  return R.raw.convo5ablock5 ;
       }
       else if("convo5ablock6".equals(track))
       {
	  return R.raw.convo5ablock6 ;
       }
       else if("convo5ablock7".equals(track))
       {
	  return R.raw.convo5ablock7 ;
       }
       else if("convo5bblock10".equals(track))
       {
	  return -1 ;
          // return R.raw.convo5bblock10 ;
       }
       else if("convo5bblock8".equals(track))
       {
          return -1 ;
	  // return R.raw.convo5bblock8 ;
       }
       else if("convo5bblock9".equals(track))
       {
          return -1 ;
	  // return R.raw.convo5bblock9 ;
       }
       else if("convo6ablock1".equals(track))
       {
	  return R.raw.convo6ablock1 ;
       }
       else if("convo6bblock10".equals(track))
       {
	  return R.raw.convo6bblock10 ;
       }
       else if("convo6bblock11".equals(track))
       {
	  return R.raw.convo6bblock11 ;
       }
       else if("convo6bblock12".equals(track))
       {
	  return R.raw.convo6bblock12 ;
       }
       else if("convo6bblock13".equals(track))
       {
	  return R.raw.convo6bblock13 ;
       }
       else if("convo6bblock2".equals(track))
       {
	  return R.raw.convo6bblock2 ;
       }
       else if("convo6bblock3".equals(track))
       {
	  return R.raw.convo6bblock3 ;
       }
       else if("convo6bblock4".equals(track))
       {
	  return R.raw.convo6bblock4 ;
       }
       else if("convo6bblock5".equals(track))
       {
	  return R.raw.convo6bblock5 ;
       }
       else if("convo6bblock6".equals(track))
       {
	  return R.raw.convo6bblock6 ;
       }
       else if("convo6bblock7".equals(track))
       {
	  return R.raw.convo6bblock7 ;
       }
       else if("convo6bblock8".equals(track))
       {
	  return R.raw.convo6bblock8 ;
       }
       else if("convo6bblock9".equals(track))
       {
	  return R.raw.convo6bblock9 ;
       }
       else if("convo6cblock14".equals(track))
       {
	  return R.raw.convo6cblock14 ;
       }
       else if("convo6dblock15".equals(track))
       {
	  return R.raw.convo6dblock15 ;
       }
       else if("convo6eblock16".equals(track))
       {
	  return R.raw.convo6eblock16 ;
       }
       else if("convo6eblock17".equals(track))
       {
	  return R.raw.convo6eblock17 ;
       }
       else if("convo6eblock18".equals(track))
       {
	  return R.raw.convo6eblock18 ;
       }
       else if("convo6eblock19".equals(track))
       {
	  return R.raw.convo6eblock19 ;
       }
       else if("convo6fblock20".equals(track))
       {
	  return R.raw.convo6fblock20 ;
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


