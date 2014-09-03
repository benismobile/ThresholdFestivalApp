package com.example.android.geofence ;

import org.json.JSONObject ;
import org.json.JSONArray ;
import org.json.JSONException ;
import java.text.ParseException ;
import com.google.android.gms.location.Geofence;


public class BuildingJSONParser
{
 

   public static Building[] parseBuildingsArray(String buildingsJSONStr) throws ParseException
   {
        JSONArray buildingsArray ;
	try
	{

           JSONObject buildingsObject = new JSONObject(buildingsJSONStr) ;
           buildingsArray = buildingsObject.getJSONArray("buildings") ;
        }catch(JSONException e)
	 {
            throw new ParseException("Error parsing buildings string into JSON Array: " + e.getMessage(), 0 ) ;

	 }

	Building[] buildings = new Building[buildingsArray.length()] ;


        
	for(int i = 0 ; i < buildingsArray.length() ; i++ )
	{
	   try
	   {
	     buildings[i] = parseBuilding(buildingsArray.getJSONObject(i));
	   
	   }catch(JSONException e)
	    {
 		throw new ParseException("Error parsing buildings Array object: " + e.getMessage(), 0) ;
	    }
	}


	return buildings ;


   }
   
   
   
   
   public static Building parseBuilding(String buildingObjectString) throws ParseException
   {
      
          try
	  {
             JSONObject buildingObj = new JSONObject(buildingObjectString) ;
	     return parseBuilding(buildingObj) ;
	  }catch(JSONException e)
	   {
	      throw new ParseException("Unexpected Error parsing Building JSON String " + e.getMessage(), 0 ) ;
	   }

   }
   
   public static Building parseBuilding(JSONObject buildingObject) throws ParseException
   {
      if(buildingObject == null) nullInput("conversation object not intialised") ;

      try 
      {
            if(!buildingObject.has("name")) missingKey("name") ;
            if(!buildingObject.has("geofence_building")) missingKey("geofence_building") ;
	    
	    String name = buildingObject.getString("name") ;
            JSONObject geofenceBuildingObj = buildingObject.getJSONObject("geofence_building");
            GeofenceBuilding geofenceBuilding = parseGeofenceBuilding(geofenceBuildingObj) ;
	    return new Building(name, geofenceBuilding) ;

      
      }catch (JSONException e) 
       {
	   throw new ParseException("Unexpected Error parsing Building JSON object " + e.getMessage(), 0 ) ;
       }

   }

   private static void missingKey(String key) throws ParseException
   {
      throw new ParseException("object must have key \"" + key + "\"", 0 ) ;
   }

   private static void nullInput(String missingInput) throws ParseException
   {
      throw new ParseException("Missing input: " + missingInput, 0) ;
   }

   private static boolean isValidGeofenceBuildingObject(JSONObject geofenceBuildingObject)
   {
      return geofenceBuildingObject.has("id") && 
             geofenceBuildingObject.has("lat") &&
	     geofenceBuildingObject.has("lon") &&
	     geofenceBuildingObject.has("radius") &&
	     geofenceBuildingObject.has("duration") &&
	     geofenceBuildingObject.has("transitions") ;

   }

   private static GeofenceBuilding parseGeofenceBuilding(JSONObject geofenceBuildingObject) throws ParseException
   {

       if(geofenceBuildingObject == null) nullInput(" GeofenceBuildingObject" ) ;
       if(!isValidGeofenceBuildingObject(geofenceBuildingObject)) throw new ParseException("Invalid geofenceBuildingObject: " + geofenceBuildingObject, 1) ; 
       
       try
       {
          int id = geofenceBuildingObject.getInt("id");
          double lat = geofenceBuildingObject.getDouble("lat");
          double lon = geofenceBuildingObject.getDouble("lon");
          float radius = (float)geofenceBuildingObject.getDouble("radius");
          long duration = geofenceBuildingObject.getLong("duration");

          JSONArray transitionsArray = geofenceBuildingObject.getJSONArray("transitions") ;
          int transitions = 0;
          for(int j=0; j < transitionsArray.length() ; j++)
          {
             String transitionStr = transitionsArray.getString(j) ;
             int transition = 0 ;
	     if("ENTER".equals(transitionStr))
	     {
	        transition = Geofence.GEOFENCE_TRANSITION_ENTER ;
	     }
	     else if("EXIT".equals(transitionStr))
	     {							
	        transition = Geofence.GEOFENCE_TRANSITION_EXIT ;
             }					 
	     transitions = transitions | transition ;			
	  }
	

          GeofenceBuilding.Builder gfBuilder = new GeofenceBuilding.Builder(id) ;
	  gfBuilder.setCircularRegion(lat,lon,radius)
	  	   .setExpirationDuration(duration)
		   .setTransitionTypes(transitions) ;
          

	  return gfBuilder.build() ;

     }catch(JSONException e)
        {
	   throw new ParseException("Unexpected Error parsing geofence_building JSON object " + e.getMessage(), 0 ) ;
        }
        
	

   }


}
