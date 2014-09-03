package com.example.android.geofence ;

import org.json.JSONObject ;
import org.json.JSONArray ;
import org.json.JSONException ;
import java.text.ParseException ;
import com.google.android.gms.location.Geofence;


public class ConvoJSONParser
{
 

   public static Convo[] parseConvoArray(String conversationsJSONStr) throws ParseException
   {
        JSONArray conversationsArray ;
	try
	{

           JSONObject conversationObject = new JSONObject(conversationsJSONStr) ;
           conversationsArray = conversationObject.getJSONArray("conversations") ;
        }catch(JSONException e)
	 {
            throw new ParseException("Error parsing conversations string into JSON Array: " + e.getMessage(), 0 ) ;

	 }

	Convo[] convos = new Convo[conversationsArray.length()] ;


        
	for(int i = 0 ; i < conversationsArray.length() ; i++ )
	{
	   try
	   {
	     convos[i] = parseConvo(conversationsArray.getJSONObject(i));
	   
	   }catch(JSONException e)
	    {
 		throw new ParseException("Error parsing conversations Array object: " + e.getMessage(), 0) ;
	    }
	}


	return convos ;


   }
   
   
   
   
   public static Convo parseConvo(String conversationObjectString) throws ParseException
   {
      
          try
	  {
             JSONObject convoObj = new JSONObject(conversationObjectString) ;
	     return parseConvo(convoObj) ;
	  }catch(JSONException e)
	   {
	      throw new ParseException("Unexpected Error parsing Conversation JSON String " + e.getMessage(), 0 ) ;
	   }

   }
   
   public static Convo parseConvo(JSONObject conversationObject) throws ParseException
   {
      if(conversationObject == null) nullInput("conversation object not intialised") ;

      try 
      {
            if(!conversationObject.has("name")) missingKey("name") ;
            if(!conversationObject.has("geofence_audio")) missingKey("geofence_audio") ;
	    
	    String name = conversationObject.getString("name") ;
            JSONObject geofenceAudioObj = conversationObject.getJSONObject("geofence_audio");
            GeofenceAudio geofenceAudio = parseGeofenceAudio(geofenceAudioObj) ;
	    return new Convo(name, geofenceAudio) ;

      
      }catch (JSONException e) 
       {
	   throw new ParseException("Unexpected Error parsing Conversation JSON object " + e.getMessage(), 0 ) ;
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

   private static boolean isValidGeofenceObject(JSONObject geofenceAudioObject)
   {
      return geofenceAudioObject.has("id") && 
             geofenceAudioObject.has("lat") &&
	     geofenceAudioObject.has("lon") &&
	     geofenceAudioObject.has("radius") &&
	     geofenceAudioObject.has("duration") &&
	     geofenceAudioObject.has("transitions") &&
	     geofenceAudioObject.has("track") ;

   }

   private static GeofenceAudio parseGeofenceAudio(JSONObject geofenceAudioObject) throws ParseException
   {

       if(geofenceAudioObject == null) nullInput(" GeofenceAudioObject" ) ;
       if(!isValidGeofenceObject(geofenceAudioObject)) throw new ParseException("Invalid geofenceAudioObject: " + geofenceAudioObject, 1) ; 
       
       try
       {
          int id = geofenceAudioObject.getInt("id");
          double lat = geofenceAudioObject.getDouble("lat");
          double lon = geofenceAudioObject.getDouble("lon");
          float radius = (float)geofenceAudioObject.getDouble("radius");
          long duration = geofenceAudioObject.getLong("duration");

          JSONArray transitionsArray = geofenceAudioObject.getJSONArray("transitions") ;
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
	
	  String track = geofenceAudioObject.getString("track");										

          GeofenceAudio.Builder gfBuilder = new GeofenceAudio.Builder(id) ;
	  gfBuilder.setCircularRegion(lat,lon,radius)
	  	   .setExpirationDuration(duration)
		   .setTransitionTypes(transitions)
		   .setTrack(track) ;
          

	  if(geofenceAudioObject.has("on_complete"))
	  {
	     JSONObject onComplete = geofenceAudioObject.getJSONObject("on_complete") ;
	     gfBuilder.setOnComplete(parseOnComplete(onComplete)) ;
	  }

	  return gfBuilder.build() ;

     }catch(JSONException e)
        {
	   throw new ParseException("Unexpected Error parsing geofence_audio JSON object " + e.getMessage(), 0 ) ;
        }
        
	

   }


   private static OnComplete parseOnComplete(JSONObject onCompleteObj) throws ParseException, JSONException
   {
       if(onCompleteObj.has("dialog"))
       {
	 Dialog dialog = parseDialog(onCompleteObj) ;
	 return new OnComplete(dialog) ;
       }
       else if(onCompleteObj.has("audio"))
       {
          Audio audio = parseAudio(onCompleteObj.getJSONObject("audio")) ;
	  return new OnComplete(audio) ;

       }
       else
       {
          throw new ParseException("Invalid onComplete object: " + onCompleteObj, 2) ;
       }

   }

   public static Dialog parseDialog(String dialogStr) throws ParseException
   {
       try
       {
	  JSONObject dialogObj = new JSONObject(dialogStr) ;
	  return parseDialog(dialogObj) ;
	

       }catch (JSONException e)
       {
          throw new ParseException("Invalid Dialof JSON String:" + e.getMessage()  ,1 ) ;

       }
      

   }


   private static Dialog parseDialog(JSONObject dialogObj) throws ParseException, JSONException
   {
      if(!dialogObj.has("dialog")) throw new ParseException("Invalid dialog object: no dialog key" + dialogObj, 1) ; 
    
     dialogObj = dialogObj.getJSONObject("dialog") ;  
     if(!dialogObj.has("options")) throw new ParseException("Invalid dialog object: no options key" + dialogObj, 1) ; 
      
         JSONArray optionsArray = dialogObj.getJSONArray("options") ;
         Option[] options = new Option[optionsArray.length()] ;

         for(int k = 0 ; k < optionsArray.length() ; k++ )
         {
            JSONObject optionObj = optionsArray.getJSONObject(k) ;
	    Option option = parseOption(optionObj) ;
	    options[k] = option ;
         }
      
      
      return new Dialog(options) ;
   }

   private static Audio parseAudio(JSONObject audioObj) throws ParseException, JSONException
   {
        
        if(!audioObj.has("id") || ! audioObj.has("track")) throw new ParseException("Invalid audio object: " + audioObj, 1) ;

        int audioTrackId = audioObj.getInt("id") ;
        String audioTrack = audioObj.getString("track") ;
	if(audioObj.has("on_complete"))
	{
          JSONObject audioTrackOnComplete = audioObj.getJSONObject("on_complete") ;
	  OnComplete onAudioComplete = parseOnComplete(audioTrackOnComplete) ;
	  return new Audio(audioTrackId, audioTrack, onAudioComplete) ;
        }
	else
	{
            return new Audio(audioTrackId, audioTrack) ;
	}
   }

   private static Option parseOption(JSONObject optionObj) throws ParseException, JSONException
   {
      if(!optionObj.has("option") || !optionObj.has("audio"))
      {
         throw new ParseException("Invalid Option object: " + optionObj, 1) ; 
      }

      String optionStr = optionObj.getString("option") ;
      JSONObject audioObj = optionObj.getJSONObject("audio") ;
      Audio audio = parseAudio(audioObj) ;
      return new Option(optionStr, audio) ;
   }



}
