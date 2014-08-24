package com.example.android.geofence ;

import android.content.DialogInterface ;

public class ConvoDialogOnClickListener implements DialogInterface.OnClickListener
{

   private final IGeofenceVisitor visitor ;
   private final Dialog dialog ;
   private final Option[] options ;
   


   public ConvoDialogOnClickListener(IGeofenceVisitor v, Dialog d)
   {
      this.visitor = v ;
      this.dialog = d ;
      this.options = d.getOptions() ;


   }

   @Override
   public void onClick(DialogInterface dialog, int which) 
   {
      Option selectedOption = this.options[which] ;

      if(selectedOption!=null)
      {
         this.visitor.visit(selectedOption) ;
      }
   }
						         


}
