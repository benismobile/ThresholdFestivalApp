/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.example.android.framemarkers;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent ;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import com.example.android.framemarkers.sampleapp.SampleApplicationControl;
import com.example.android.framemarkers.sampleapp.SampleApplicationException;
import com.example.android.framemarkers.sampleapp.SampleApplicationSession;
import com.example.android.framemarkers.utils.LoadingDialogHandler;
import com.example.android.framemarkers.utils.SampleApplicationGLView;
import com.example.android.framemarkers.utils.Texture;
// import com.qualcomm.vuforia.samples.VuforiaSamples.R;
import com.example.android.location.R;
import com.example.android.framemarkers.sampleappmenu.SampleAppMenu;
import com.example.android.framemarkers.sampleappmenu.SampleAppMenuGroup;
import com.example.android.framemarkers.sampleappmenu.SampleAppMenuInterface;
import com.example.android.location.BackgroundAudioService ;
import android.content.ServiceConnection ;
import android.content.ComponentName;
import android.os.Binder ;
import android.os.IBinder ;
import android.media.MediaPlayer ;

// The main activity for the FrameMarkers sample. 
public class FrameMarkers extends Activity implements SampleApplicationControl,
  SampleAppMenuInterface, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
{
    private static final String LOGTAG = "FrameMarkers";
    
    SampleApplicationSession vuforiaAppSession;
    
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private FrameMarkerRenderer mRenderer;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    
    private RelativeLayout mUILayout;
    
    private Marker dataSet[];
    
    private GestureDetector mGestureDetector;
    
    private SampleAppMenu mSampleAppMenu;
    
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mIsFrontCameraActive = false;
    private int detected = -1 ;
 
    private View mFlashOptionView;
    private BackgroundAudioService backgroundAudioService ;
    private boolean mIsBound = false ;


    private ServiceConnection mBackgroundAudioServiceConnection = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        backgroundAudioService = ((BackgroundAudioService.LocalBinder) service).getService();
        mIsBound = true ;
        // Tell the user about this for our demo.
        Log.d( LOGTAG, "Connected to BackgroundAudioService") ;

    }

      public void onServiceDisconnected(ComponentName className) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        // Because it is running in our same process, we should never
        // see this happen.
        backgroundAudioService = null;
        mIsBound = false ;
        Log.e(LOGTAG, "Disconnected from BackgroundAudioService ERROR") ;
    }

   };

    
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
        this);
    
    boolean mIsDroidDevice = false;
    
    
    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        vuforiaAppSession = new SampleApplicationSession(this);
        Log.d(LOGTAG, "onCreate startLoadingAnimation");
        
        startLoadingAnimation();
        
        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        Log.d(LOGTAG, "onCreate initAR");
        mGestureDetector = new GestureDetector(this, new GestureListener());
        
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();
        
        Log.d(LOGTAG, "loadTextures");
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");
        Intent startAudioIntent = new Intent(this, com.example.android.location.BackgroundAudioService.class);
        bindService(startAudioIntent, mBackgroundAudioServiceConnection, this.BIND_AUTO_CREATE);


    }
    
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    
    public void setDetected(int detected)
    {
        this.detected = detected ;
    }    
    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
    
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/letter_Q.png",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/letter_C.png",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/letter_A.png",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/blue.png",
            getAssets()));
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/letter_R.png",
            getAssets()));
       
    }
    
    
    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

        showToastLong("Find bird and then touch screen to capture it") ;
        
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
    }
    
    
    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }
    
    
    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Unload texture:
        mTextures.clear();
        mTextures = null;
        
        System.gc();
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
            return true;
        if(detected >= 0 ) 
        { 

            // String track = geofenceAudio.getTrackFromId(detected) ;
            if(this.backgroundAudioService != null) 
            {
              // TODO implement getTrackFromId
               this.backgroundAudioService.playForeground("notificationbird", this, this) ;


               Intent intent = new Intent(this, com.example.android.location.WebViewActivity.class);
 	       intent.putExtra("bird",""+detected );

               detected = -1 ;
               startActivity(intent);

            }


	    else
	    {
 	      Log.d(LOGTAG, "can't play track no background audio service" ) ;
	    }
        } 
       
        return mGestureDetector.onTouchEvent(event);
    }
    
    
    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
    }
    
    
    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new FrameMarkerRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
    }
    
    
    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        // Initialize the marker tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker trackerBase = trackerManager.initTracker(MarkerTracker
            .getClassType());
        MarkerTracker markerTracker = (MarkerTracker) (trackerBase);
        
        if (markerTracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        
        return result;
        
    }
    
    
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
            .getTracker(MarkerTracker.getClassType());
        if (markerTracker == null)
            return false;
        
        dataSet = new Marker[4];
        
        dataSet[0] = markerTracker.createFrameMarker(0, "MarkerQ", new Vec2F(
            50, 50));
        if (dataSet[0] == null)
        {
            Log.e(LOGTAG, "Failed to create frame marker Q.");
            return false;
        }
        
        dataSet[1] = markerTracker.createFrameMarker(1, "MarkerC", new Vec2F(
            50, 50));
        if (dataSet[1] == null)
        {
            Log.e(LOGTAG, "Failed to create frame marker C.");
            return false;
        }
        
        dataSet[2] = markerTracker.createFrameMarker(2, "MarkerA", new Vec2F(
            50, 50));
        if (dataSet[2] == null)
        {
            Log.e(LOGTAG, "Failed to create frame marker A.");
            return false;
        }
        
        dataSet[3] = markerTracker.createFrameMarker(3, "MarkerR", new Vec2F(
            50, 50));
        if (dataSet[3] == null)
        {
            Log.e(LOGTAG, "Failed to create frame marker R.");
            return false;
        }
        
        Log.i(LOGTAG, "Successfully initialized MarkerTracker.");
        
        return true;
    }
    
    
    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
            .getTracker(MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.start();
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
            .getTracker(MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.stop();
        
        return result;
    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(MarkerTracker.getClassType());
        
        return result;
    }
    
    
    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();
            
            mRenderer.mIsActive = true;
            
            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            
            // Hides the Loading Dialog
            loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            
            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
            
            boolean result = CameraDevice.getInstance().setFocusMode(
                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
            
            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
            
            Log.d(LOGTAG, "new SampleAppMenu");
            mSampleAppMenu = new SampleAppMenu(this, this, "Frame Markers",
                mGlView, mUILayout, null);
            setSampleAppMenuSettings();
            Log.d(LOGTAG, "SampleAppMenu settings");
            
        } else
        {
            Log.e(LOGTAG, exception.getString());
            finish();
        }
    }
    
    
    @Override
    public void onQCARUpdate(State state)
    {
    }
    
    final public static int CMD_BACK = -1;
    final public static int CMD_AUTOFOCUS = 1;
    final public static int CMD_FLASH = 2;
    final public static int CMD_CAMERA_FRONT = 3;
    final public static int CMD_CAMERA_REAR = 4;
    
    
    // This method sets the menu's settings
    private void setSampleAppMenuSettings()
    {
        SampleAppMenuGroup group;
        
        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);
        
        group = mSampleAppMenu.addGroup("", true);
        group.addSelectionItem(getString(R.string.menu_contAutofocus),
            CMD_AUTOFOCUS, mContAutofocus);
        mFlashOptionView = group.addSelectionItem(
            getString(R.string.menu_flash), CMD_FLASH, false);
        
        CameraInfo ci = new CameraInfo();
        boolean deviceHasFrontCamera = false;
        boolean deviceHasBackCamera = false;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++)
        {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
                deviceHasFrontCamera = true;
            else if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
                deviceHasBackCamera = true;
        }
        
        if (deviceHasBackCamera && deviceHasFrontCamera)
        {
            group = mSampleAppMenu.addGroup(getString(R.string.menu_camera),
                true);
            group.addRadioItem(getString(R.string.menu_camera_front),
                CMD_CAMERA_FRONT, false);
            group.addRadioItem(getString(R.string.menu_camera_back),
                CMD_CAMERA_REAR, true);
        }
        
        mSampleAppMenu.attachMenu();
    }
    
    
    @Override
    public boolean menuProcess(int command)
    {
        boolean result = true;
        
        switch (command)
        {
            case CMD_BACK:
                finish();
                break;
            
            case CMD_FLASH:
                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);
                
                if (result)
                {
                    mFlash = !mFlash;
                } else
                {
                    showToast(getString(mFlash ? R.string.menu_flash_error_off
                        : R.string.menu_flash_error_on));
                    Log.e(LOGTAG,
                        getString(mFlash ? R.string.menu_flash_error_off
                            : R.string.menu_flash_error_on));
                }
                break;
            
            case CMD_AUTOFOCUS:
                
                if (mContAutofocus)
                {
                    result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
                    
                    if (result)
                    {
                        mContAutofocus = false;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_off));
                        Log.e(LOGTAG,
                            getString(R.string.menu_contAutofocus_error_off));
                    }
                } else
                {
                    result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
                    
                    if (result)
                    {
                        mContAutofocus = true;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_on));
                        Log.e(LOGTAG,
                            getString(R.string.menu_contAutofocus_error_on));
                    }
                }
                
                break;
            
            case CMD_CAMERA_FRONT:
            case CMD_CAMERA_REAR:
                
                // Turn off the flash
                if (mFlashOptionView != null && mFlash)
                {
                    // OnCheckedChangeListener is called upon changing the checked state
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        ((Switch) mFlashOptionView).setChecked(false);
                    } else
                    {
                        ((CheckBox) mFlashOptionView).setChecked(false);
                    }
                }
                
                doStopTrackers();
                CameraDevice.getInstance().stop();
                CameraDevice.getInstance().deinit();
                try
                {
                    vuforiaAppSession
                        .startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA.CAMERA_FRONT
                            : CameraDevice.CAMERA.CAMERA_BACK);
                } catch (SampleApplicationException e)
                {
                    showToast(e.getString());
                    Log.e(LOGTAG, e.getString());
                    result = false;
                }
                doStartTrackers();
                mIsFrontCameraActive = (command == CMD_CAMERA_FRONT);
                break;
        
        }
        
        return result;
    }
    
    
    public void showToastLong(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
    public void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    
    boolean isFrontCameraActive()
    {
        return mIsFrontCameraActive;
    }

    public void onCompletion(MediaPlayer mp)
    {
        Log.d(LOGTAG, "MediaPlayer.OnCompletionListener.onComplete called") ;
        if(mp!=null)
        {
           if(this.backgroundAudioService != null)
           {
               this.backgroundAudioService.foregroundStopped(); 
           }
           else
           {
              Log.d(LOGTAG,"cannot foreground stop no background audio service " ) ;
           }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
         mp.reset() ;
         Log.e(LOGTAG, "Media error state code: " + what ) ;
         return true ;
    }


}
