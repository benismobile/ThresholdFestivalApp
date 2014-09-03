/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Iterator ;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 * For a production app, use a content provider that's synced to the
 * web or loads geofence data based on current location.
 */
public class SimpleGeofenceStore {

    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;
    private  HashSet<String> geofenceIds ;
    private Editor editor ;

    // The name of the resulting SharedPreferences
    private static final String SHARED_PREFERENCE_NAME =
                    com.example.android.location.WebViewActivity.class.getSimpleName();

    // Create the SharedPreferences storage with private access only
    public SimpleGeofenceStore(Context context) {
        mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
	this.geofenceIds = new HashSet<String>() ;
	this.editor = this.mPrefs.edit() ;
    }


    public HashSet<String> getGeofenceIds()
    {
       // make a defensive copy of geofenceIds HashSet
       HashSet<String> idsCopy = new HashSet<String>() ;

       Set persistedIds = null ; 
 
       try
       {

           persistedIds = this.mPrefs.getStringSet("currentGeofenceIds", idsCopy) ;
       }catch(NoSuchMethodError err)
          {
	      // getStringSet only for API 11 and above - use tokenised version instead until Gingerbread finally dies
              String tokenizedIds = this.mPrefs.getString("currentGeofenceIds", "") ;
              String[] splitIds =  tokenizedIds.split(":") ; 	
              for(int i = 0 ; i < splitIds.length ; i++)
	      {
		 idsCopy.add(splitIds[i]) ;

              }
              return idsCopy ;
          }

       if(persistedIds == null || persistedIds.size() < 1) 
       {
          Log.d(GeofenceUtils.APPTAG, "SimpleGeofenceStore: getGeofenceIds() could no persisted ids");      
          return idsCopy ;
       }
       


       Log.d(GeofenceUtils.APPTAG, "SimpleGeofenceStore: getGeofenceIds() copying persisted ids");      
       for(Iterator<String> i = persistedIds.iterator() ; i.hasNext();)
       {
          String id = i.next() ;
          Log.d(GeofenceUtils.APPTAG, "SimpleGeofenceStore: getGeofenceIds() copying persisted id:" + id);      
	  idsCopy.add(id) ;
       }

        return idsCopy ;
    }

    public void clearGeofenceIds()
    {
       editor.remove("currentGeofenceIds") ;
       editor.commit() ;
    }

    /**
     * Returns a stored geofence by its id, or returns {@code null}
     * if it's not found.
     *
     * @param id The ID of a stored geofence
     * @return A geofence defined by its center and radius. See
     * {@link SimpleGeofence}
     */
    public SimpleGeofence getGeofence(String id) {

        /*
         * Get the latitude for the geofence identified by id, or GeofenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        double lat = mPrefs.getFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),
                GeofenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the longitude for the geofence identified by id, or
         * -999 if it doesn't exist
         */
        double lng = mPrefs.getFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),
                GeofenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the radius for the geofence identified by id, or GeofenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        float radius = mPrefs.getFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS),
                GeofenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the expiration duration for the geofence identified by
         * id, or GeofenceUtils.INVALID_VALUE if it doesn't exist
         */
        long expirationDuration = mPrefs.getLong(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
                GeofenceUtils.INVALID_LONG_VALUE);

        long expirationTime = mPrefs.getLong(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_TIME),
                0);


        /*
	* Get media looping flag from shared prefs
	*
	*/
	boolean looping = mPrefs.getBoolean(getGeofenceFieldKey(id, "MEDIA_LOOPING"), false) ;

        /*
	* Get media varyVolume flag from shared prefs
	*
	*/
	boolean varyVolume = mPrefs.getBoolean(getGeofenceFieldKey(id, "MEDIA_VARY_VOLUME"), false) ;


        /*
         * Get the transition type for the geofence identified by
         * id, or GeofenceUtils.INVALID_VALUE if it doesn't exist
         */
        int transitionType = mPrefs.getInt(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
                GeofenceUtils.INVALID_INT_VALUE);

        // If none of the values is incorrect, return the object
        if (
            lat != GeofenceUtils.INVALID_FLOAT_VALUE &&
            lng != GeofenceUtils.INVALID_FLOAT_VALUE &&
            radius != GeofenceUtils.INVALID_FLOAT_VALUE &&
            expirationDuration != GeofenceUtils.INVALID_LONG_VALUE &&
            transitionType != GeofenceUtils.INVALID_INT_VALUE) {

            // Return a true Geofence object
            // TODO add expire time into constructor
	    return new SimpleGeofence(id, lat, lng, radius, expirationDuration, expirationTime, looping, varyVolume, transitionType);
	    

        // Otherwise, return null.
        } else {
            return null;
        }
    }

    /**
     * Save a geofence.

     * @param geofence The {@link SimpleGeofence} containing the
     * values you want to save in SharedPreferences
     */
    public void setGeofence(String id, SimpleGeofence geofence) {

        /*
         * Get a SharedPreferences editor instance. Among other
         * things, SharedPreferences ensures that updates are atomic
         * and non-concurrent
         */
        // Editor editor = mPrefs.edit();

        // Write the Geofence values to SharedPreferences
        editor.putFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE),
                (float) geofence.getLatitude());

        editor.putFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE),
                (float) geofence.getLongitude());

        editor.putFloat(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS),
                geofence.getRadius());

        editor.putLong(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION),
                geofence.getExpirationDuration());
        
	editor.putLong(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_TIME),
                geofence.getExpirationTime());
        
	editor.putBoolean(getGeofenceFieldKey(id, "MEDIA_LOOPING"), geofence.getLooping()) ;

        editor.putBoolean(getGeofenceFieldKey(id, "MEDIA_VARY_VOLUME"), geofence.getVaryVolume()) ;


        editor.putInt(
                getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE),
                geofence.getTransitionType());

        this.geofenceIds.add(id) ;
        try
        {
           editor.putStringSet("currentGeofenceIds", this.geofenceIds) ;
        }catch(NoSuchMethodError err)
         {
            // putStringSet API 11 and above so tokenize the set instead
            String tokenizedIds =  tokenizeIds() ;
            editor.putString("currentGeofenceIds", tokenizedIds ) ;
         }
        // Commit the changes
        editor.commit();
    }

    public void clearGeofence(String id) {

        // Remove a flattened geofence object from storage by removing all of its keys
        // Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_EXPIRATION_TIME));
	editor.remove(getGeofenceFieldKey(id, "MEDIA_LOOPING"));
	editor.remove(getGeofenceFieldKey(id, "MEDIA_VARY_VOLUME"));
        editor.remove(getGeofenceFieldKey(id, GeofenceUtils.KEY_TRANSITION_TYPE));
	if(this.geofenceIds.contains(id))
	{
           this.geofenceIds.remove(id) ;
           try
           {
              editor.putStringSet("currentGeofenceIds", this.geofenceIds) ;
           }catch(NoSuchMethodError err)
            {
		String tokenizedIds = tokenizeIds() ;
                editor.putString("currentGeofenceIds", tokenizedIds) ;
            }
	}
        editor.commit();
    }


    private String tokenizeIds()
    {
           
            StringBuilder sb = new StringBuilder() ;
 	    for(Iterator<String> i = this.geofenceIds.iterator() ; i.hasNext(); )
            {
               String nextId = i.next() ;
               sb.append(nextId) ;
               if(i.hasNext()) 
                   {    sb.append(":") ; } 
            }

            String tokenizedIds =  sb.toString() ;
            return tokenizedIds ;
     }
    


    /**
     * Given a Geofence object's ID and the name of a field
     * (for example, GeofenceUtils.KEY_LATITUDE), return the key name of the
     * object's values in SharedPreferences.
     *
     * @param id The ID of a Geofence object
     * @param fieldName The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(String id, String fieldName) {

        return
                GeofenceUtils.KEY_PREFIX +
                id +
                "_" +
                fieldName;
    }
}
