package com.example.android.geofence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.example.android.geofence.ConvoJSONParser ;
import java.text.ParseException ;

import junit.framework.TestCase;

public class ConvoJSONParserTest extends TestCase {

	
	 private String dialogStr ;
	 private final String filePath = "/home/bbutchar/apps/forgotten_futures/test/res/dialog.json";

	
	@Override
	protected void setUp()
	{
		 try
	     {
	       BufferedReader br = new BufferedReader(new FileReader(filePath)) ;
	        String nextLine ;
	        StringBuilder sb = new StringBuilder() ;

	        while((nextLine = br.readLine()) != null)
	        {
	           sb.append(nextLine) ;

	        }

	           this.dialogStr = sb.toString() ;
                 
	    }catch(IOException e)
	     {
                e.printStackTrace() ;
	        fail("Could not read from file:" +  e.getMessage());

	     }   

		
		
	}
	
	public void testParseDialog()
	{
           try
           {
              Dialog dialog = ConvoJSONParser.parseDialog(dialogStr) ; 
              assertNotNull(dialog) ;
	      assertNotNull(dialog.getOptions()) ;
              assertTrue("options  empty" , dialog.getOptions().length > 0 ) ;
              Option[] options = dialog.getOptions() ;
          
              for(int i = 0 ; i < options.length ; i++ )
              {
                  Option option = options[i] ;
                  assertNotNull(option.getOption()) ; 
                  assertNotNull(option.getAudio()) ;
                  Audio audio = option.getAudio() ;
 		  assertTrue("audio id not set", audio.getId() > 0 ) ;
                  assertNotNull("audio track not set", audio.getTrack() ) ;

              }  
              
           }catch(ParseException e)
            {      
		fail("error parsing dialogStr:" + e) ;
            }

          
	}
	
	public void testParseConvo()
	{
		// fail("not implemented") ;
	}
	
}


