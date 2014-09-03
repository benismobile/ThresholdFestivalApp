package com.example.android.geofence ;

public interface IBuildingGeofenceVisitor{

   public void visit(GeofenceNullVisitable nullVisited) ;
   public void visit(Building building) ;
   public void visit(GeofenceBuilding geofenceBuilding);

}
