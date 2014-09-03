package com.example.android.geofence;

import com.obj.WavefrontObject ;
import junit.framework.TestCase;
import utils.Logger ;
import java.util.ArrayList ;
import com.obj.Group ;
import java.util.Iterator ;
import com.obj.Vertex ;
import com.obj.TextureCoordinate ;

public class WavefrontObjParserTest extends TestCase {

	
	 private final String filename = "/home/bbutchar/tools/models/bird8.obj";
         private WavefrontObject wfobj ;
	
	@Override
	protected void setUp()
	{ 
          Logger logger = new Logger() ;
	  this.wfobj = new WavefrontObject(filename) ;	
	}
	
	public void testWaveFrontObj()
	{

            ArrayList<Group> groups = this.wfobj.getGroups() ;
            assertNotNull(groups) ;
            assertTrue(groups.size() > 0 ) ;

            for(Iterator<Group> i = groups.iterator() ; i.hasNext();)
            {
		Group group = i.next() ;
                System.out.println("Printing Group with index count: " + group.indexCount) ;
                ArrayList<Vertex> vertices = group.vertices ;
                assertNotNull(vertices) ;
                assertTrue(vertices.size() > 0 ) ;

                ArrayList<Vertex> normals = group.normals ;
                assertNotNull(normals) ; 
                assertTrue(normals.size() > 0 ) ;
		assertEquals(normals.size() , vertices.size() ) ;
                
                ArrayList<TextureCoordinate> texcoords = group.texcoords ;
                assertNotNull(texcoords) ;
                assertTrue(texcoords.size() > 0 ) ;
                

                ArrayList<Integer> indices = group.indices ;
                assertNotNull(indices) ;
                assertTrue(indices.size()  > 0 ) ;
 		System.out.println("num indices:" + indices.size() ) ;

                   int counter = 0 ;
                   for(Iterator<Vertex> j = vertices.iterator() ; j.hasNext();) 
 		   {
                       if(counter % 2 ==0 ) System.out.println() ;
                       StringBuilder sb = new StringBuilder() ;
		       Vertex vertex = j.next() ;
                       sb.append(vertex.getX()) ;
                       sb.append(",") ;
                       sb.append(vertex.getY()) ;
                       sb.append(",") ;
 		       sb.append(vertex.getZ()) ;
                       sb.append(",") ;
                       System.out.print(sb.toString()) ;
                       counter++ ;
                   }


             //      int counter = 0 ;

                   for(Iterator<Vertex> k = normals.iterator() ; k.hasNext();) 
       		   {
           //             if(counter % 2 ==0 ) System.out.println() ;
                       StringBuilder sb = new StringBuilder() ;
		      Vertex vertex = k.next() ; 	
                       sb.append(vertex.getX()) ;
                       sb.append(",") ;
                       sb.append(vertex.getY()) ;
                       sb.append(",") ;
 		       sb.append(vertex.getZ()) ;
                       sb.append(",") ;
       //                System.out.print(sb.toString()) ;
         //              counter++ ;
		   }
                  // counter = 0 ;
                   for(Iterator<TextureCoordinate> l = texcoords.iterator() ; l.hasNext();)
		   { 
//                        if(counter % 5 ==0 ) System.out.println() ;
                        StringBuilder sb = new StringBuilder() ;
	                TextureCoordinate tex = l.next() ;
                        sb.append(tex.getU()) ;
                        sb.append(",") ;
                        sb.append(tex.getV()) ;
                        sb.append(",") ;
 		        sb.append(tex.getW()) ;
                        sb.append(",") ;
                         
          //              System.out.print(sb.toString()) ;
                //        counter++ ;
                        
                   }
            //           System.out.println() ;
            /*       
                   for(Iterator<Integer> m = indices.iterator() ; m.hasNext();)
 		   {
			StringBuilder sb = new StringBuilder() ;
			sb.append(m.next()) ;			
                        sb.append(",") ;
                        System.out.print(sb.toString()) ;

		   }
                     System.out.println() ;
             */
                }
              
               
            
           

	}
	
	
}


