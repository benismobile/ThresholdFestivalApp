package com.example.android.geofence ;

public interface IGeofenceVisitor{

   public void visit(Convo convo) ;
   public void visit(GeofenceAudio geofenceAudio);
   
   public void visit(Dialog dialog) ;
   public void visit(Option option) ;
   public void visit(Audio audio) ;
   public void visit(GeofenceNullVisitable nullVisited) ;
   public Dialog getActiveDialog() ;




}
