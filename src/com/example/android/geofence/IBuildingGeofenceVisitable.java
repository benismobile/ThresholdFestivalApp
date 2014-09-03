package com.example.android.geofence ;

public interface IBuildingGeofenceVisitable{

  public void accept(IBuildingGeofenceVisitor gfvisitor) ;
  public String toJSONString() ;
  
}

