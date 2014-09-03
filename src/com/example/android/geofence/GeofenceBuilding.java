package com.example.android.geofence ;

import org.json.JSONObject ;
import org.json.JSONException ;
import org.json.JSONArray ;


public class GeofenceBuilding implements IBuildingGeofenceVisitable
{

   private int id ;
   private double latitude ;
   private double longitude ;
   private float radius ;
   private long  duration ;
   private int transitions ;

   private GeofenceBuilding(int id)
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


   public void accept(IBuildingGeofenceVisitor gfVisitor)
   {
      gfVisitor.visit(this) ;
   }

   @Override 
   public String toString()
   {
      String requiredFields =  "id:" + this.id  + " latitude:" + this.latitude + " longitude:" + longitude + " radius:" + radius + " duration: " + duration + " transitions: " + transitions ;

        return requiredFields ;
   }

   @Override
   public String toJSONString()
   {
       StringBuilder sb = new StringBuilder() ;
       JSONObject gfBuildingObj = new JSONObject() ;
       try
       {
         gfBuildingObj.put("id", this.getId()) ;
         gfBuildingObj.put("lat", this.getLatitude()) ;
         gfBuildingObj.put("lon", this.getLongitude()) ;
         gfBuildingObj.put("radius", this.getRadius()) ;
         gfBuildingObj.put("duration", this.getDuration()) ;
         gfBuildingObj.put("transitions", this.getTransitions()) ;

       }catch(JSONException e)
       {
	   return "{" + "\"err\":" + "\"" + e.getMessage() + "\"" + "}" ;
       }

       sb.append(gfBuildingObj.toString() ) ;

       return sb.toString() ;

   }

   public static class Builder
   {
     
     private GeofenceBuilding geofenceBuilding ;

     public Builder(int id)
     {
       this.geofenceBuilding = new GeofenceBuilding(id) ;
       //TODO set defaults
     }
    

     public GeofenceBuilding build()
     {
        // TODO validate build parameters
     
	return geofenceBuilding ;
     }

     public GeofenceBuilding.Builder setCircularRegion(double latitude, double longitude, float radius)
     {
        this.geofenceBuilding.latitude = latitude ;
	this.geofenceBuilding.longitude = longitude ;
	this.geofenceBuilding.radius = radius ;
	return this ;
     }

     public GeofenceBuilding.Builder setExpirationDuration(long durationMillis)
     {
        this.geofenceBuilding.duration = durationMillis ;
        return this ;
     }

     public GeofenceBuilding.Builder setTransitionTypes(int transitions)
     {

        this.geofenceBuilding.transitions = transitions ;
	return this ;
     }


   }

}
