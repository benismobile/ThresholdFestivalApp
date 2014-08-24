package com.example.android.geofence ;

import android.app.AlertDialog ;
import android.app.Activity ;
import android.app.DialogFragment ;
import android.os.Bundle ;
import android.content.DialogInterface ;
import android.util.Log ;
import com.example.android.location.R ;

public class GeofenceDialogFragment extends DialogFragment {
   
   public interface ConvoDialogListener 
   {
      public void onDialogClick(int selected);
   }

    ConvoDialogListener mListener;

    public static GeofenceDialogFragment newInstance(Dialog dialog)
    {
       GeofenceDialogFragment frag = new GeofenceDialogFragment() ;
       Bundle args = new Bundle() ;



       if(dialog != null && dialog.getOptions() != null)
       {
          Option[] options = dialog.getOptions() ;
          CharSequence[] optionsStrings = new CharSequence[options.length];
          for(int i = 0 ; i < options.length ; i++ )
	  {
          	optionsStrings[i] =  options[i].getOption() ;
		// optionsStrings[i] = optionsStrings[i].toString().replace("'", "\\'");
	  }
          args.putCharSequenceArray("options", optionsStrings) ;
       }
       frag.setArguments(args) ;
       return frag ;
    }
    
 

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

	Log.d(GeofenceUtils.APPTAG, "onCreateDialog") ;
        CharSequence[] optionArray = getArguments().getCharSequenceArray("options") ;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Forgotten Futrues") 
	       .setSingleChoiceItems (optionArray, -1, new DialogInterface.OnClickListener(){
	           @Override
		   public void onClick(DialogInterface dialog, int which)
		   {
		      mListener.onDialogClick(which) ;
                      Log.d(GeofenceUtils.APPTAG, "onClick " + which ) ;
		   }
	       
	       }) ;
                 
               
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ConvoDialogListener so we can send events to the host
            mListener = (ConvoDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ConvoDialogListener");
        }
    }



}

