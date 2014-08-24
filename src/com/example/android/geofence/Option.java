package com.example.android.geofence ;

import org.json.JSONObject ;
import org.json.JSONException ;

public class Option implements IGeofenceVisitable{

   private final String option ;
   private final Audio audio ;

   public Option(String option, Audio audio)
   {
      this.option = option ;
      this.audio = audio ;

   }

   @Override
   public void accept(IGeofenceVisitor visitor)
   {
       visitor.visit(this) ;
   }
 
   public String getOption(){

      return this.option ;
   }

   public Audio getAudio(){
   
      return this.audio ;
   }


   @Override
   public String toString()
   {
     return this.option + this.audio ;
   }


   @Override
   public String toJSONString()
   {
     JSONObject optionObj = new JSONObject() ;
     try
     {
       optionObj.put("option", this.option) ;
     }catch(JSONException e)
        {
           return "{" + "\"err\":"+ "\"" + e.getMessage() + "\""  + "}" ;

        }
     StringBuilder sb = new StringBuilder() ;
     sb.append(optionObj.toString()) ;
     
     sb.append(this.audio.toJSONString()) ;
     return sb.toString() ;

   }

}
