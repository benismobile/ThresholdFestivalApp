/*
  Load OBJ files into processing.
  
  In this example we keep the data structures outside the library. This gives more control to the user to change whatever they need/want.
  If you prefer the easier and regular way look into "loadobj" example
*/

import processing.opengl.*;
import com.obj.*;

VGL vgl;

float aspectRatio = 1;

PrintWriter out;
OutputStream os;

OBJScene scene;

void setup()
{
  size( 800, 600, OPENGL );

  vgl = new VGL( this );

  aspectRatio = width / (float)height;

  hint( ENABLE_OPENGL_4X_SMOOTH );
  smooth();

  frameRate( 60 );

//  out = createWriter( "objout.txt" );


  scene = new OBJScene();
  scene.load( dataPath("spheres.obj"), .5 );
  //scene.debug();
  
  
//  out.close();
}


/*void writeString( String s )
{
  out.println( s );
  out.flush();
}*/

void draw()
{
  float time = millis() * 0.001;


  vgl.background( 0.4 );
   
  vgl.perspective( 45, aspectRatio, 1, 1000 );
  vgl.camera( 0, 0, 50, //sin(time*.5)*300, -100, cos(time*.5)*300,
             0, 0, 0,
             0, 1, 0 );

  vgl._gl.glEnable(GL.GL_LIGHTING);
//  gl._gl.glEnable(GL.GL_COLOR_MATERIAL);
//  gl._gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT,new float[]{.0f,.0f,.0f, 1.f }, 0);
  vgl._gl.glColorMaterial( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE );
  vgl._gl.glLightfv(GL.GL_LIGHT1,GL.GL_AMBIENT,new float[]{0,0,0, 1.f }, 0);
  vgl._gl.glLightfv(GL.GL_LIGHT1,GL.GL_DIFFUSE,new float[]{ 1, 1, 1, 1.f }, 0);
  vgl._gl.glLightfv(GL.GL_LIGHT1,GL.GL_SPECULAR,new float[]{ .7f,.7f,.7f, 1.f  }, 0);
  vgl._gl.glLightfv(GL.GL_LIGHT1,GL.GL_POSITION,new float[]{ -0, 800, 1000, 1.0f }, 0);
  vgl._gl.glEnable( GL.GL_LIGHT1 );
  vgl._gl.glLightModeli( GL.GL_LIGHT_MODEL_COLOR_CONTROL, GL.GL_SEPARATE_SPECULAR_COLOR );

  vgl.rotateY( mouseX );
  
  scene.draw();
}


void stop()
{
  super.stop();
}
