import javax.media.opengl.*; 
import java.nio.*;
//import com.sun.opengl.util.texture.*;
import com.sun.opengl.util.texture.Texture;

class XTexture
{
  XTexture()
  {
    _tex = null;
    isLoaded = false;
  }

  XTexture( String fName )
  {
    fileName = fName;
    _tex = null;
    _buffer = null;
    _img = null;
    
    isLoaded = false;
    
    load( fName );
  }
  
  void bind()
  {
    //_tex.bind();
    vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, _id );
  }

  void enable()
  {
    vgl.enableTexture( true );
    bind();
    //_tex.enable();
  }

  void disable()
  {
    //_tex.disable();
    vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );
    vgl.enableTexture( false );
  }

  void setWrap()
  {
    _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
    _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
  }

  void setClamp()
  {
    _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
    _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
  }

  void setClampToEdge()
  {
    _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
    _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );
  }

  void createGL( int w, int h )
  {
     _width = w;
     _height = h;
     
     int[] id = { 0 };

    // Creating texture.
    vgl._gl.glGenTextures( 1, id, 0 );
    _id = id[0];
      println( "texture created: " + _id );
    
    vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, _id );
    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );

    vgl._gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, _width, _height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, null );
//    gl._glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, GL.GL_RGBA, _width, _height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null );
  }
  
  void create( int w, int h )
  {
      _width = w;
      _height = h;
      _buffer = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );//_PRE );
      _tex = TextureIO.newTexture( _buffer, false );
      _id = _tex.getTextureObject();
      println( "texture created: " + _id );

//      _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_R, GL.GL_REPEAT );
      _tex.setTexParameterf( GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
      _tex.setTexParameterf( GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
      _tex.setTexParameterf( GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );//_MIPMAP_LINEAR );
      _tex.setTexParameterf( GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );//_MIPMAP_LINEAR );
  }

  void load( String fName )//, boolean mipmap )  
  {   
    fileName = fName;
        
    try
    {
      //println( "LOAD TEXTURE START" );

      _tex = TextureIO.newTexture( new File(dataPath(fileName)), true );  //mipmap );
      //println( "AFTER TEX" );

      _id = _tex.getTextureObject();
      //println( "ID: " + _id );

      _width = _tex.getImageWidth();
      _height = _tex.getImageHeight();

      //_tex.setTexParameterf( GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT ); 
      //_tex.setTexParameterf( GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );  
      _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP ); 
      _tex.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );  
      _tex.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
      _tex.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );//_MIPMAP_LINEAR );

      println("loading texture: " + fileName + " with id= " + _id );
      
      isLoaded = true;
    }
    catch( IOException e )
    {
      println( "*** texture error: " + e );
    }
    
    //println( "LOAD TEXTURE END" );
    
    //_id = _tex.getTarget();
    //println( "Texture: '" + fileName + "' with id: '" + _id + "'" );
    //println( "Estimated Memory Size: " + _tex.getEstimatedMemorySize() / 1024 + " KBytes" );

    //return tex;
  }

  void loadPImage( String fName )
  {
    fileName = fName;

    _img = loadImage( fName );
    if( _img == null )
    {
      println( "couldnt load texture: " + fileName );
      return;
    }      

    _width = _img.width;
    _height = _img.height;
        
    int[] texId = new int[1];

    //println( "gen tex" );
    vgl._gl.glGenTextures( 1, texId, 0 );
    _id = texId[0];
    //println( "tex id: " + _id );
    
    //println( "bind" );
    vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, _id );

    //println( "pixelstore" );
    vgl._gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE );

    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT ); 
    vgl._gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );  

    //println( "texparameter" );
    vgl._gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
    vgl._gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );

    //println( "teximage2d" );
//    vgl._gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, 4, _width, _height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(_img.pixels) ); 
    vgl._glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, 4, _img.width, _img.height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(_img.pixels) ); 
//    vgl._glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, 4, _img.width, _img.height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(_img.pixels) ); 

    //_img = null;

    isLoaded = true;
    println("loading texture: " + fileName + " with id= " + _id );
  }

  void update()
  {
//    if( _img != null );
//      _img.updatePixels();

//    vgl._gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, _width );
//    vgl._gl.glPixelStorei( GL.GL_UNPACK_SWAP_BYTES, 1 );
//    vgl._gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 4 );

    for( int j=0; j<_height; j++ )
    {
      for( int i=0; i<_width; i++ )
      {
        imgBuffer[i+j*_width] = (_img.pixels[i+j*_width]);
      }
    }

    vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, _id );
    vgl._gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
    vgl._gl.glTexSubImage2D( GL.GL_TEXTURE_2D, 0, 0, 0, _width, _height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(imgBuffer) ); //IntBuffer.wrap(_img.pixels) );
//    vgl._gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, 4, _width, _height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap(imgBuffer) ); //IntBuffer.wrap(_img.pixels) );

//    vgl._gl.glPixelStorei( GL.GL_UNPACK_SWAP_BYTES, 0 );

    vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );

    println("updated, id= " + _id );
  }
  
  void delete()
  {
    int[] texId = { _id };
    try {
    vgl._gl.glDeleteTextures( 1, texId, 0 );
    } catch( GLException e )
    { 
      println( e );
    }
    
    _img = null;
    
    _id = 0;

    isLoaded = false;
  }
  
  int getTarget()
  {
    return _tex.getTarget();
  }

  int getId()
  {
    return _id;
  }

  int getWidth()
  {
    return _width;
  }

  int getHeight()
  {
    return _height;
  }


  //
  // Members
  //
  String fileName;
  Texture _tex;
  int    _id;

  int _width, _height;

  PImage _img;
  int[] imgBuffer;

  BufferedImage  _buffer;
  
  boolean isLoaded;
};

