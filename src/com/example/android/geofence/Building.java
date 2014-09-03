package com.example.android.geofence ;

import org.json.JSONObject ;
import java.text.ParseException ;
import org.json.JSONObject ;
import org.json.JSONArray ;
import org.json.JSONException ;


public class Building implements IBuildingGeofenceVisitable {

  private  String name ;
  private  GeofenceBuilding geofenceBuilding ;
 
  public Building(String name, GeofenceBuilding geofenceBuilding) 
  {
    this.name = name ;
    this.geofenceBuilding = geofenceBuilding ;
  }

  public String getName()
  {
    return this.name ;
  }

  public GeofenceBuilding getGeofenceBuilding()
  {

    return this.geofenceBuilding ;
  }

  @Override
  public void accept(IBuildingGeofenceVisitor gfVisitor)
  {
     gfVisitor.visit(this) ;

  }

  @Override
  public String toString()
  {
     return "name: "  + this.name + "\n" + geofenceBuilding.toString() ;

  }

  @Override
  public String toJSONString()
  {

    StringBuilder sb = new StringBuilder() ;
    JSONObject buildingObj = new JSONObject() ;

    try
    {    
       buildingObj.put("name",this.name ) ;
       String geofenceBuildingStr = this.geofenceBuilding.toJSONString() ;
       JSONObject geofenceBuildingObj = new JSONObject(geofenceBuildingStr) ;
       buildingObj.put("geofence_building", geofenceBuildingObj) ;
       sb.append(buildingObj.toString() ) ;
    }catch(JSONException e)
     {
        return "\"err\":" + e.getMessage() ;
     }
    return sb.toString() ;

 
  }
}
