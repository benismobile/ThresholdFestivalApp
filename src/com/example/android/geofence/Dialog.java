package com.example.android.geofence ;


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
      sb.append("[") ;

      for(int i = 0 ; i < this.options.length ; i++ )
      {
         Option option = this.options[i] ;
         
	 String optionStr = option.toJSONString() ;
	 sb.append(optionStr) ;

         if (i < options.length -1 ) 
	 {
	    sb.append(",") ;
	 } 

      }
      sb.append("]") ;
      return sb.toString() ;
    
   }



}
