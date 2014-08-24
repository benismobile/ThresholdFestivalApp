package com.example.android.geofence ;

public class GeofenceNullVisitable 
{
  private final String msg ;  // message for visitor to process

  public GeofenceNullVisitable(String msg)
  {
	this.msg = msg ;
  }

  public String getMessage()
  {
     return this.msg ;
  }


}
