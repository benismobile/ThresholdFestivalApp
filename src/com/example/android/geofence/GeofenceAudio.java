package com.example.android.geofence ;

import org.json.JSONObject ;
import org.json.JSONException ;
import org.json.JSONArray ;



public class GeofenceAudio implements IGeofenceVisitable
{

   private int id ;
   private double latitude ;
   private double longitude ;
   private float radius ;
   private long  duration ;
   private int transitions ;
   private String track ;  
   private OnComplete onComplete ;

   private GeofenceAudio(int id)
   {
      this.id = id ;
   }

   public int getId()
   {
      return this.id ;
   }

   public double getLatitude()
   {
      return this.latitude ;
   }

   public double getLongitude()
   {
      return this.longitude ;
   }

   public float getRadius()
   {
      return this.radius ;
   }

   public long getDuration()
   {
      return this.duration ;
   }

   public int getTransitions()
   {
      return this.transitions ;
   }

   public String getTrack()
   {
      return this.track ;
   }

   public OnComplete getOnComplete()
   {
      return this.onComplete ;
   }

   public boolean hasOnComplete()
   {
      if(this.onComplete != null ) 
      { 
         return true ;
      }
      else
      {
         return false ;
      }	

   }

   @Override
   public void accept(IGeofenceVisitor gfVisitor)
   {
      gfVisitor.visit(this) ;
   }

   @Override 
   public String toString()
   {
      String requiredFields =  "id:" + this.id  + " latitude:" + this.latitude + " longitude:" + longitude + " radius:" + radius + " duration: " + duration + " transitions: " + transitions + " track: " + track ;

     if(this.onComplete != null)
     {
        return requiredFields + onComplete ;
     }
     else
     {
        return requiredFields ;
     }

   }

   @Override
   public String toJSONString()
   {
       StringBuilder sb = new StringBuilder() ;
       JSONObject gfAudioObj = new JSONObject() ;
       try
       {
         gfAudioObj.put("id", this.getId()) ;
         gfAudioObj.put("lat", this.getLatitude()) ;
         gfAudioObj.put("lon", this.getLongitude()) ;
         gfAudioObj.put("radius", this.getRadius()) ;
         gfAudioObj.put("duration", this.getDuration()) ;
         gfAudioObj.put("transitions", this.getTransitions()) ;
	 gfAudioObj.put("track", this.getTrack()) ;

       }catch(JSONException e)
       {
	   return "{" + "\"err\":" + "\"" + e.getMessage() + "\"" + "}" ;
       }

       sb.append(gfAudioObj.toString() ) ;
       if(hasOnComplete())
       {
          sb.append(this.getOnComplete().toJSONString()) ;
       }

       return sb.toString() ;

   }

   public static class Builder
   {
     
     private GeofenceAudio geofenceAudio ;

     public Builder(int id)
     {
       this.geofenceAudio = new GeofenceAudio(id) ;
       //TODO set defaults
     }
    

     public GeofenceAudio build()
     {
        // TODO validate build parameters
     
	return geofenceAudio ;
     }

     public GeofenceAudio.Builder setCircularRegion(double latitude, double longitude, float radius)
     {
        this.geofenceAudio.latitude = latitude ;
	this.geofenceAudio.longitude = longitude ;
	this.geofenceAudio.radius = radius ;
	return this ;
     }

     public GeofenceAudio.Builder setExpirationDuration(long durationMillis)
     {
        this.geofenceAudio.duration = durationMillis ;
        return this ;
     }

     public GeofenceAudio.Builder setTransitionTypes(int transitions)
     {

        this.geofenceAudio.transitions = transitions ;
	return this ;
     }

     public GeofenceAudio.Builder setTrack(String track)
     {
        this.geofenceAudio.track = track ;
	return this ;

     }
  
     public GeofenceAudio.Builder setOnComplete(OnComplete onComplete)
     {
        this.geofenceAudio.onComplete = onComplete ;
	return this;

     }

   }

}
