/*
  Load a scene from an .obj file as simple as it gets.
  
  
    model is the free Sponza Atrium by Marko Dabrovic:
    http://hdri.cgtechniques.com/~sponza/
    Downloads: http://hdri.cgtechniques.com/~sponza/files/     
*/

import javax.media.opengl.*; 
import processing.opengl.*;
import objimp.*;

float aspectRatio = 1;

ObjImpScene scene2;


void setup()
{
  size( 800, 600, OPENGL );

  aspectRatio = width / (float)height;

  hint( ENABLE_OPENGL_4X_SMOOTH );
  smooth();

  frameRate( 60 );

    try
    {
        scene2 = new ObjImpScene( this );
        scene2.load( dataPath("sponza.obj"), 5 );
//  scene2.load( dataPath("spheres.obj"), .5 );
    } catch( Exception e )
    {
        println( e );
        System.exit( 0 );
    }
}



void draw()
{
  float time = millis() * 0.001;

  background( 0.4*255 );
  
  perspective( PI*.25, aspectRatio, 1, 6000 );
  camera( 0, 3, 0, -2*cos(time*.5), sin(time)*2+3, -2*sin(time*.5), 0, 1, 0 );
  
  // setup light. to do correct lighting you need to set it before any world transformations
  GL _gl = ((PGraphicsOpenGL)g).beginGL();
  setupLight( _gl, new float[]{0, 15, 0}, 1 );
  ((PGraphicsOpenGL)g).endGL(); 

  scale( 1, -1, 1 );  // make y axis points up

  
//  translate( 0, -10, 0 );
  rotateY( radians(mouseX) );
  
  scene2.draw();  
}




// val is 0 or 1. 0 = directional light, 1 = point light
void setupLight( GL g, float[] pos, float val )
{
  float[] light_emissive = { 0.0f, 0.0f, 0.0f, 1 };
  float[] light_ambient = { 0.0f, 0.0f, 0.0f, 1 };
  float[] light_diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
  float[] light_specular = { 1.0f, 1.0f, 1.0f, 1.0f };  
  float[] light_position = { pos[0], pos[1], pos[2], val };  

  g.glLightfv ( GL.GL_LIGHT1, GL.GL_AMBIENT, light_ambient, 0 );
  g.glLightfv ( GL.GL_LIGHT1, GL.GL_DIFFUSE, light_diffuse, 0 );
  g.glLightfv ( GL.GL_LIGHT1, GL.GL_SPECULAR, light_specular, 0 );
  g.glLightfv ( GL.GL_LIGHT1, GL.GL_POSITION, light_position, 0 );  
  g.glEnable( GL.GL_LIGHT1 );
  g.glEnable( GL.GL_LIGHTING );
  
  g.glEnable( GL.GL_COLOR_MATERIAL );
}  




void stop()
{
  super.stop();
}
