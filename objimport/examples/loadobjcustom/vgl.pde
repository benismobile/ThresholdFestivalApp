import processing.opengl.*;
import java.nio.*; 
import java.util.*;
import javax.media.opengl.*; 
import javax.media.opengl.glu.*; 
import com.sun.opengl.util.texture.*;  


class VGL
{
  VGL( PApplet parent )
  {
    _parent = parent;
    
    _pgl = (PGraphicsOpenGL) g;
    _gl = _pgl.gl;
    _glu = (((PGraphicsOpenGL) g).glu);
  }

  void begin()
  {
    _gl = ((PGraphicsOpenGL)g).beginGL(); 
    _glu = (((PGraphicsOpenGL) g).glu);
  }

  void end()
  {
    ((PGraphicsOpenGL)g).endGL(); 
  }

  void background( float c )
  {
    _gl.glClearColor( c, c, c, 1 );
    _gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );// | GL.GL_ACCUM_BUFFER_BIT );
  }

  void background( float r, float g, float b, float a )
  {
    _gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
    _gl.glClearColor( r, g, b, a );
  }

  void ortho( float left, float right, float bottom, float top, float near, float far )
  {
    _gl.glMatrixMode( GL.GL_PROJECTION );
    _gl.glLoadIdentity();    
    _gl.glOrtho( left, right, bottom, top, near, far );
  }

  // create ortho mode.
  // ranges from [-1..1]
  void ortho()
  {
    _gl.glMatrixMode( GL.GL_PROJECTION );
    _gl.glLoadIdentity();    
    _gl.glOrtho( -1, 1, -1, 1, -1, 1 );
  }
  
  void perspective( float fovy, float aspect, float nearZ, float farZ )
  {
    _gl.glMatrixMode( GL.GL_PROJECTION );
    _gl.glLoadIdentity();
    _glu.gluPerspective( fovy, aspect, nearZ, farZ );  
  }

  void camera( float ex, float ey, float ez, float tx, float ty, float tz, float ux, float uy, float uz )
  {
    _gl.glMatrixMode( GL.GL_MODELVIEW );
    _gl.glLoadIdentity();
    _glu.gluLookAt( ex, ey, ez, tx, ty, tz, ux, uy, uz );
  }

  void setMatrixMode( int mode )
  {
    switch( mode )
    {
      case 0:
        _gl.glMatrixMode( GL.GL_PROJECTION );
        _gl.glLoadIdentity();
        break;
      case 1:
        _gl.glMatrixMode( GL.GL_MODELVIEW );
        _gl.glLoadIdentity();
        break;
      default:
        _gl.glMatrixMode( GL.GL_MODELVIEW );
        _gl.glLoadIdentity();
    }
  }
  
  Matrix getMatrix( int type )
  {
    Matrix m = new Matrix();
    m.identity();
    
    if( type == 0 )
    {
      _gl.glGetFloatv( GL.GL_PROJECTION_MATRIX, m.getFloatBuffer() );
    }
    else    
    {
      _gl.glGetFloatv( GL.GL_MODELVIEW_MATRIX, m.getFloatBuffer() );
    }

    return m;
  }
  
  void translate( float x, float y, float z )
  {
    _gl.glTranslatef( x, y, z );
  }  

  void translate( Vector3 v )
  {
    _gl.glTranslatef( v.x, v.y, v.z );
  }  


  void rotate( float a, float x, float y, float z )
  {
    _gl.glRotatef( a, x, y, z );
  }

  void rotate( float a, Vector3 v )
  {
    _gl.glRotatef( a, v.x, v.y, v.z );
  }

  void rotateX( float a )
  {
    _gl.glRotatef( a, 1, 0, 0 );
  }

  void rotateY( float a )
  {
    _gl.glRotatef( a, 0, 1, 0 );
  }

  void rotateZ( float a )
  {
    _gl.glRotatef( a, 0, 0, 1 );
  }

  void scale( float sx, float sy, float sz )
  {
    _gl.glScalef( sx, sy, sz );
  }

  void scale( float s )
  {
    _gl.glScalef( s, s, s );
  }

  /**********************************************************************
  // matrix stack methods
  ***********************************************************************/

  void loadMatrix( FloatBuffer m )
  {
    _gl.glLoadMatrixf( m );
  }

  void multMatrix( FloatBuffer m )
  {
    _gl.glMultMatrixf( m );
  }

  void multMatrix( Matrix m )
  {
    FloatBuffer fb = FloatBuffer.wrap( m._M );
    _gl.glMultMatrixf( fb );
  }

  void pushMatrix()
  {
    _gl.glPushMatrix();
  }

  void popMatrix()
  {
    _gl.glPopMatrix();
  }


  /**********************************************************************
  // blending methods
  ***********************************************************************/
  
  void enableBlend()
  {
    _gl.glEnable( GL.GL_BLEND );
  }

  void disableBlend()
  {
    _gl.glDisable( GL.GL_BLEND );
  }
  
  void setBlend( boolean f )
  {
    if( f ) _gl.glEnable( GL.GL_BLEND );
    else _gl.glDisable( GL.GL_BLEND );
  }

  void setAlphaBlend()
  {
    _gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );    
  }

  void setAdditiveBlend()
  {
    _gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
  }

  void setOneBlend()
  {
    _gl.glBlendFunc( GL.GL_ONE, GL.GL_ONE );
  }


  /**********************************************************************
  // render methods
  ***********************************************************************/

  void enableLighting( boolean f )
  {
    if( f ) _gl.glEnable( GL.GL_LIGHTING );
    else _gl.glDisable( GL.GL_LIGHTING );
  }
  
  void enableTexture( boolean f )
  {
    if( f ) _gl.glEnable( GL.GL_TEXTURE_2D );
    else _gl.glDisable( GL.GL_TEXTURE_2D );
  }

  void setDepthWrite( boolean f )
  {
    if( f ) _gl.glEnable( GL.GL_DEPTH_TEST );
    else _gl.glDisable( GL.GL_DEPTH_TEST );
  }

  void setDepthMask( boolean f )
  {
    _gl.glDepthMask( f );
  }

  /**********************************************************************
  // rendering methods
  **********************************************************************/

  void fill( float c )
  {
    _r = c;
    _g = c;
    _b = c;
    _a = 1;
  }

  void fill( float c, float a )
  {
    _r = c;
    _g = c;
    _b = c;
    _a = a;
  }

  void fill( float r, float g, float b, float a )
  {
    _r = r;
    _g = g;
    _b = b;
    _a = a;
  }

  void quad( float posx, float posy, float z, float s )
  {
    _gl.glTranslatef( posx, posy, z );
    _gl.glBegin( GL.GL_QUADS );
    _gl.glNormal3f( 0.0f, 0.0f, 1.0f );
    _gl.glColor4f( _r, _g, _b, _a );

    _gl.glTexCoord2f(0, 0);
    _gl.glVertex3f( -1*s,  1*s, z );

//    _gl.glColor4f( _r, _g, _b, _a );
    _gl.glTexCoord2f(1, 0);
    _gl.glVertex3f(  1*s,  1*s, z );

//    _gl.glColor4f( _r, _g, _b, _a );
    _gl.glTexCoord2f(1, 1);
    _gl.glVertex3f(  1*s, -1*s, z );

//    _gl.glColor4f( _r, _g, _b, _a );
    _gl.glTexCoord2f(0, 1);
    _gl.glVertex3f( -1*s, -1*s, z );
    _gl.glEnd();   
  }

  // renders a quad at origin.
  // useful when you use want to manually translate
  void quad( float s )
  {  
    quad( 0, 0, 0, s );
  }

  void rect( float posx, float posy, float z, float sx, float sy )
  {  
    _gl.glTranslatef( posx, posy, z );
    _gl.glBegin( GL.GL_QUADS );
    _gl.glNormal3f( 0.0f, 0.0f, 1.0f ); 

    _gl.glColor4f( _r, _g, _b, _a );    
    _gl.glTexCoord2f(0, 0); 
    _gl.glVertex3f( -1*sx, -1*sy, z );
    
    _gl.glColor4f( _r, _g, _b, _a );
    _gl.glTexCoord2f(1, 0); 
    _gl.glVertex3f(  1*sx, -1*sy, z );
    
    _gl.glColor4f( _r, _g, _b, _a );
    _gl.glTexCoord2f(1, 1); 
    _gl.glVertex3f(  1*sx,  1*sy, z );
    
    _gl.glColor4f( _r, _g, _b, _a );
    _gl.glTexCoord2f(0, 1); 
    _gl.glVertex3f( -1*sx,  1*sy, z );
    
    _gl.glEnd();   
  }

  void rect( float sx, float sy )
  {  
    this.rect( 0, 0, 0, sx, sy );
  }

  /**********************************************************************
  // Members
  **********************************************************************/
  PGraphicsOpenGL _pgl;
  GL _gl;
  GLU _glu;
  
  PApplet _parent;
  
  // global color for our render faces
  float _r, _g, _b, _a;
};
