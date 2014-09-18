package com.example.android.location ;

import android.content.Intent ;
import android.content.BroadcastReceiver;
import android.text.TextUtils ;
import android.content.Context;
import android.widget.Toast ;
import android.util.Log ;
import com.example.android.geofence.GeofenceUtils;


public class GeofenceBroadcastReceiver extends BroadcastReceiver  {


       private final String LOGTAG = "GeofenceBroadcastReceiver" ; 
       /*
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {
          
             try
             {
             	WebViewActivity webViewActivity = (WebViewActivity) context ;
             } catch(java.lang.ClassCastException cce) 
               {
		  Log.e(LOGTAG, "Could not cast context to WebViewActivity: " + cce.getMessage() ) ;
                  handleGeofenceError(context, intent) ;
               }


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
                Log.e(LOGTAG, "Invalid action" ) ;
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context of originating component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {


               Log.d(LOGTAG, "handleGeofenceStatus: " + intent );

        }

        /**
         * Report geofence transitions to the UI
         * @param context A Context of originating component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent)
        {
           Log.d(LOGTAG, "handleGeofenceTransition: " + intent.getStringExtra("TRANSITION_TYPE") );
           Log.d(LOGTAG, "handleGeofenceTransition: " + intent.getStringArrayExtra("GEOFENCE_IDS") );

           String transitionType = intent.getStringExtra("TRANSITION_TYPE") ;
           String[] triggerGeofenceIds = intent.getStringArrayExtra("GEOFENCE_IDS") ;

           for(int i = 0 ; i < triggerGeofenceIds.length ; i++)
           {
              String geofenceId = triggerGeofenceIds[i] ;
              Log.d(LOGTAG, "handleGeofenceTransition: " + geofenceId ) ;

              if(geofenceId.startsWith("CONVO")) // TODO not good solution need to get a geofence convo type somehow - maybe an intent extra? 
              {
                   Log.d(LOGTAG, "handleGeofenceTransition: processing convo " + geofenceId) ;
                   handleConversationTransition(geofenceId, transitionType, context) ;
              }
/*
              else if(geofenceId.startsWith("BUILDING")) // TODO not good solution need to get a geofence convo type somehow 
              {
                   Log.d(LOGTAG, "handleGeofenceTransition: processing building " + geofenceId) ;
                   handleBuildingTransition(geofenceId, transitionType, context) ;
              }
              else
              {
                  Log.d(LOGTAG, "handleGeofenceTransition: processing background audio" + geofenceId ) ;
                  handleBackgroundAudioTransition(geofenceId, transitionType) ;
              }
*/

           }

         }


         private void handleBuildingTransition(String geofenceId, String transitionType, Context context)
         { 

            Log.d(LOGTAG, "handleBuildingTransition" ) ;
            /*
            if("Entered".equals(transitionType) || "Exited".equals(transitionType))
            {

                   String transitionFlag = "Entered".equals(transitionType) ? "ENTER" : "EXIT";  ;
                   Building building = mBuildings.get(geofenceId) ;

                   if(building != null)
                   {
                      Toast.makeText(context, transitionType + " building:" + building.getName(), Toast.LENGTH_SHORT).show();
                      BuildingGeofenceVisitor buildingVisitor = new BuildingGeofenceVisitor(building,  context, transitionFlag ) ;
                      building.accept(buildingVisitor) ;

                   }
            }
           */
         }

         private void handleConversationTransition(String geofenceId, String transitionType, Context context)
         {

            Log.d(LOGTAG, "handleConversationTransition" ) ;
            /*
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

            */

         }

 private void handleBackgroundAudioTransition(String geofenceId, String transitionType)
         {
            Log.d(LOGTAG, "handleBackgroundAudioTransition" ) ;
         /*
             SimpleGeofence sgf = mGeofencePrefs.getGeofence(geofenceId);
            boolean looping = sgf.getLooping() ;  // TODO subclass SimpleGeofence  

            float volume = 0.01f ;

            if(mLocationClient != null && mLocationClient.isConnected())
            {
               Location location = mLocationClient.getLastLocation() ;
               volume = getVolumeFromDistanceBetween(location, sgf) ;
               Log.d(LOGTAG, "GeofenceSampleReceiver.handleGeofenceTransition VOLUME: " + volume);
            }


            if("Entered".equals(transitionType))
            {
               Log.d(LOGTAG, "GeofenceSampleReceiver.handleGeofenceTransition ENTERED: " + geofenceId ) ;
               if(mIsBound)
               {
                  if(mBackgroundAudioService!=null)
                  {
                     mBackgroundAudioService.play(geofenceId, looping, volume) ;
                     Log.d(LOGTAG, "playing audio: " + geofenceId + " with looping:" + looping);
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
*/
         }


        /**
         * Report addition or removal errors to the UI, using a Toast
         * @param context of originating component
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(LOGTAG, msg);
            // Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }


    
}
                           


