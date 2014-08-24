package com.example.android.geofence ;

import org.json.JSONObject ;
import java.text.ParseException ;

public class Convo implements IGeofenceVisitable {

  private  String name ;
  private  GeofenceAudio geofenceAudio ;
  private JSONObject convoJSON ;
 
  public Convo(JSONObject convoJSONObj)
  {


     try
     {
     	 Convo convo = ConvoJSONParser.parseConvo(convoJSONObj) ;
         this.name = convo.getName() ;
         this.geofenceAudio = convo.getGeofenceAudio() ;
         this.convoJSON = convoJSONObj;
     }catch(ParseException e)
       {
           // Cannot do anything - due to this being horrible shortcut
	   //  TODO need to implement toJSONString properly rather than use this shortcut
       }
     

  }
  
  public Convo(String name, GeofenceAudio geofenceAudio) 
  {
    this.name = name ;
    this.geofenceAudio = geofenceAudio ;


  }

  public String getName()
  {
    return this.name ;
  }

  public GeofenceAudio getGeofenceAudio()
  {

    return this.geofenceAudio ;
  }

  @Override
  public void accept(IGeofenceVisitor gfVisitor)
  {
     gfVisitor.visit(this) ;

  }

  @Override
  public String toString()
  {
     return "name: "  + this.name + "\n" + geofenceAudio ;

  }

  @Override
  public String toJSONString()
  {
    if(this.convoJSON != null) return this.convoJSON.toString() ;
    return "{}" ;
 
  }
}
