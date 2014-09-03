package com.example.android.geofence ;

import org.json.JSONObject ;
import org.json.JSONArray ;
import org.json.JSONException ;

public class Dialog implements IGeofenceVisitable{

   private final Option[] options ;

   public Dialog(Option[] options)
   {
     this.options = options ;
   }

   public Option[] getOptions(){

      return this.options ;
   }

   public void accept(IGeofenceVisitor geofenceVisitor)
   {
      geofenceVisitor.visit(this) ;
   }

   @Override
   public String toString()
   {
      if(options==null) return null ;
      
      StringBuilder sb = new StringBuilder() ;

      for(int i = 0 ; i < this.options.length ; i++ )
      {
         Option option = this.options[i] ;
	 String optionStr = option.getOption();
	 sb.append(optionStr) ;

      }
      return sb.toString() ;
    
   }

   public String toJSONString()
   {
        
      StringBuilder sb = new StringBuilder() ;
      JSONObject dialog = new JSONObject() ;
      JSONObject options = new JSONObject() ;
      try
      {
         JSONArray optionsAryObj = new JSONArray() ;
         
         for(int i = 0 ; i < this.options.length ; i++ )
         {
            Option option = this.options[i] ;
	    String optionStr = option.toJSONString() ;
            JSONObject optionObj = new JSONObject(optionStr) ;
            optionsAryObj.put(optionObj) ;
  	 }
         options.put("options", optionsAryObj) ;
	 dialog.put("dialog", options) ;
         sb.append(dialog.toString()) ;         

      }catch(JSONException e)
       {
          return "\"err\":" + e.getMessage() ;
       }

      return sb.toString() ;
    
   }



}
