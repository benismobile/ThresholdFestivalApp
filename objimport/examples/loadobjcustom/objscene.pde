/*
  Basic object file scene manager/loader.
  This are the structures that are used to import and draw a scene from an .obj file.
  

  obj importer by Fabien Sanglard ( http://fabiensanglard.net/ )
  changed and compiled for processing&jogl by Victor Martins ( www.pixelnerve.com/v )


 This Java OBJ Loader support:
    * Groups (scene management)
    * Vertex, Normal, Texture coordinates
    * MTL (material) references.

This Java OBJ Loader DOES NOT support:
    * Relative vertex references.
    * Anything other than GL_TRIANGLE and GL_QUAD polygons
*/


class OBJFace
{
  OBJFace() 
  {
    a = b = c = 0;
  }

  OBJFace( int a, int b, int c ) 
  {
    this.a = a;
    this.b = b;
    this.c = c;
    this.matId = -1;
  }

  OBJFace( int a, int b, int c, int matId ) 
  {
    this.a = a;
    this.b = b;
    this.c = c;
    this.matId = matId;
  }

  int a, b, c;  // vertex
  int na, nb, nc;  // normal
  int ta, tb, tc;  // texcoord

  int matId;
}

class OBJMaterial
{
  OBJMaterial()
  {
    texId = -1;
  }
  
  Vector3 ambient;
  Vector3 diffuse;
  Vector3 specular;
  int texId;
}

class OBJMesh
{
  OBJMesh()
  {
    vertexList = new ArrayList();
    texcoordList = new ArrayList();
    normalList = new ArrayList();
    faceList = new ArrayList();
    material = new OBJMaterial();
  }
  
  void setName( String n )
  {
    name = n;
  }
  
  void addVertex( Vector3 v )
  {
    vertexList.add( v );
  }

  void addTexCoord( Vector3 v )
  {
    texcoordList.add( v );
  }

  void addNormal( Vector3 v )
  {
    normalList.add( v );
  }

  // Add faces
  void addFace( OBJFace f )
  {
    faceList.add( f );
  }
  void addFace( int a, int b, int c )
  {
    faceList.add( new OBJFace(a, b, c) );
  }

  
  String name;
  OBJMaterial material;  
  ArrayList vertexList;
  ArrayList texcoordList;
  ArrayList normalList;
  ArrayList faceList;
}

class OBJScene
{
  OBJScene()
  {
    _vertexList = new ArrayList();
    _normalList = new ArrayList();
    _texcoordList = new ArrayList();

    _meshList = new ArrayList();
  }

  void load( String name )
  {
    load( name, 1, 1, 1 );
  }

  void load( String name, float s )
  {
    load( name, s, s, s );
  }
  
  void load( String name, float sx, float sy, float sz )
  {
    WavefrontObject obj = new WavefrontObject( name, sx, sy, sz );  
  
    ArrayList groups = obj.getGroups();
    for( int gi=0; gi<groups.size(); gi++ )
    {
      Group g = (Group)groups.get( gi );
      Material gm = g.getMaterial();

      OBJMesh mesh = new OBJMesh();
      mesh.setName( g.getName() );

      mesh.material.ambient = new Vector3( gm.getKa().getX(), gm.getKa().getY(), gm.getKa().getZ() );
      mesh.material.diffuse = new Vector3( gm.getKd().getX(), gm.getKd().getY(), gm.getKd().getZ() );
      mesh.material.specular = new Vector3( gm.getKs().getX(), gm.getKs().getY(), gm.getKs().getZ() );
      
      if( gm.texName != null && gm.texName.length() > 0 )
      {
        XTexture tex = new XTexture( gm.texName );
        tex.setWrap();
        mesh.material.texId = tex.getId();
      }

      for( int fi=0; fi<g.getFaces().size(); fi++ )
      {
        Face f = g.getFaces().get( fi );
        int[] idx = f.vertIndices;
        int[] nidx = f.normIndices;
        int[] tidx = f.texIndices;

        OBJFace face = new OBJFace();
        face.a = idx[0];
        face.b = idx[1];
        face.c = idx[2];
        face.na = nidx[0];
        face.nb = nidx[1];
        face.nc = nidx[2];
        face.ta = tidx[0];
        face.tb = tidx[1];
        face.tc = tidx[2];
        
        mesh.addFace( face );
      }

      for( int vi=0; vi<obj.getVertices().size(); vi++ )
      {
        Vertex v = (Vertex)obj.getVertices().get( vi );
        _vertexList.add( new Vector3(v.getX(), v.getY(), v.getZ()) );
      }

      for( int vi=0; vi<obj.getNormals().size(); vi++ )
      {
        Vertex v = (Vertex)obj.getNormals().get( vi );
        _normalList.add( new Vector3(v.getX(), v.getY(), v.getZ()) );
      }

      for( int vi=0; vi<obj.getTextures().size(); vi++ )
      {
        TextureCoordinate tc = (TextureCoordinate)obj.getTextures().get( vi );
        _texcoordList.add( new Vector3( tc.getU(), tc.getV(), tc.getW()) );
      }

/*      for( int vi=0; vi<g.indices.size(); vi++ )
      {
        int tc = (int)g.indices.get( vi );
        mesh.addVertex( new Vector3( tc.getX(), tc.getY(), tc.getZ()) );
      }

      for( int vi=0; vi<g.vertices.size(); vi++ )
      {
        Vertex tc = (Vertex)g.vertices.get( vi );
        mesh.addVertex( new Vector3( tc.getX(), tc.getY(), tc.getZ()) );
      }

      for( int vi=0; vi<g.normals.size(); vi++ )
      {
        Vertex tc = (Vertex)g.normals.get( vi );
        mesh.addNormal( new Vector3( tc.getX(), tc.getY(), tc.getZ()) );
      }

      for( int vi=0; vi<g.texcoords.size(); vi++ )
      {
        TextureCoordinate tc = (TextureCoordinate)g.texcoords.get( vi );
        mesh.addTexCoord( new Vector3( tc.getU(), tc.getV(), tc.getW()) );
      }*/

      // Finally add mesh to scene
      addMesh( mesh );
    }
  }
  
  void addMesh( OBJMesh mesh )
  {
    _meshList.add( mesh );
  }


  //
  // Simple draw function. alot of space for optimization. but thats not the point here
  //
  
  void draw()
  {
    Vector3 n1, n2, n3;
    Vector3 p1, p2, p3;
    Vector3 tc1, tc2, tc3;
    
    // Render all scene
    for( int i=0; i<_meshList.size(); i++ )
    {
      OBJMesh m = (OBJMesh)_meshList.get( i );

      // If current material has texture, bind it
      if( m.material.texId > 0 )
      {
        vgl._gl.glEnable( GL.GL_TEXTURE_2D );
        vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, m.material.texId );
      }
      else
      {
        vgl._gl.glDisable( GL.GL_TEXTURE_2D );
        vgl._gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );
      }
      
      vgl._gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[]{m.material.ambient.x, m.material.ambient.y, m.material.ambient.z, 1.0f}, 0 ); 
      vgl._gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[]{m.material.diffuse.x, m.material.diffuse.y, m.material.diffuse.z, 1.0f}, 0 ); 
      vgl._gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, new float[]{m.material.specular.x, m.material.specular.y, m.material.specular.z, 1.0f}, 0 ); 
      vgl._gl.glMateriali( GL.GL_FRONT_AND_BACK,GL.GL_SHININESS, 128 );
      
      // render triangles.. this is too basic. should be optimized
      vgl._gl.glBegin( GL.GL_TRIANGLES );
      vgl._gl.glColor4f( m.material.diffuse.x, m.material.diffuse.y, m.material.diffuse.z, 1 );      
      for( int fi=0; fi<m.faceList.size(); fi++ )
      {
        OBJFace f = (OBJFace)m.faceList.get( fi );

        p1 = (Vector3)_vertexList.get(f.a);
        p2 = (Vector3)_vertexList.get(f.b);
        p3 = (Vector3)_vertexList.get(f.c);
        n1 = (Vector3)_normalList.get(f.na);
        n2 = (Vector3)_normalList.get(f.nb);
        n3 = (Vector3)_normalList.get(f.nc);
        tc1 = (Vector3)_texcoordList.get(f.ta);
        tc2 = (Vector3)_texcoordList.get(f.tb);
        tc3 = (Vector3)_texcoordList.get(f.tc);

        vgl._gl.glNormal3f( n1.x, n1.y, n1.z );
        vgl._gl.glTexCoord2f( tc1.x, tc1.y );
        vgl._gl.glVertex3f( p1.x, p1.y, p1.z );    

        vgl._gl.glNormal3f( n2.x, n2.y, n2.z );
        vgl._gl.glTexCoord2f( tc2.x, tc2.y );
        vgl._gl.glVertex3f( p2.x, p2.y, p2.z );
        
        vgl._gl.glNormal3f( n3.x, n3.y, n3.z );
        vgl._gl.glTexCoord2f( tc3.x, tc3.y );
        vgl._gl.glVertex3f( p3.x, p3.y, p3.z );
      }
      vgl._gl.glEnd();
    }
  }
  
/*  void debug()
  {
    for( int i=0; i<_meshList.size(); i++ )
    {
      Mesh m = (Mesh)_meshList.get(i);
      
      writeString( "Mesh name: " + m.name );
      writeString( "texcoordsize: " + _texcoordList.size() );
      writeString( "normalsize: " + _normalList.size() );
      writeString( "vertexsize: " + _vertexList.size() );
      writeString( "facesize: " + m.faceList.size() );

      for( int vi=0; vi<m.vertexList.size(); vi++ )
      {
        Vector3 v = (Vector3)m.vertexList.get(vi);
        float x = v.x;
        float y = v.y;
        float z = v.z;
        //writeString( "Vertex  x: " + x + ", y: " + y + ", z: " + z );
      }
      
      for( int vi=0; vi<m.faceList.size(); vi++ )
      {
        OBJFace f = (OBJFace)m.faceList.get(vi);
        int a = f.a;
        int b = f.b;
        int c = f.c;
        //writeString( "Face  a: " + a + ", b: " + b + ", c: " + c );
      }
    }
  }*/

  ArrayList _vertexList;
  ArrayList _normalList;
  ArrayList _texcoordList;
  ArrayList _meshList;
  
  private WavefrontObject _obj;
}; 
