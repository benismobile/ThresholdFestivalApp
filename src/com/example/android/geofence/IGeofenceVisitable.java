package com.example.android.geofence ;

public interface IGeofenceVisitable{

  public void accept(IGeofenceVisitor gfvisitor) ;
  public String toJSONString() ;
  
}

