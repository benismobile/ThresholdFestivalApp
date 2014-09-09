/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.example.android.framemarkers;

import java.nio.Buffer;
import android.util.Log ;
import android.content.res.AssetManager ;
import com.example.android.framemarkers.utils.MeshObject;
import com.obj.WavefrontObject ;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.util.ArrayList ;
import com.obj.Group ;
import java.util.Iterator ;
import com.obj.Vertex ;
import com.obj.TextureCoordinate ;

public class TestObject extends MeshObject
{
    
    Buffer mVertBuff;
    Buffer mTexCoordBuff;
    Buffer mNormBuff;
    Buffer mIndBuff;
    
    double[] flattenedVertices ;   
    double[] flattenedNormals ;   
    double[] flattenedTexCoords ;   
    short[]  convertedIndices ;
    
    public TestObject(AssetManager assets)
    {
        InputStream inputStream = null ;
        try
        {
           inputStream = assets.open("FrameMarkers/bird8.obj", AssetManager.ACCESS_BUFFER);
           String[] assetsList = assets.list("FrameMarkers") ;
           String[] locales = assets.getLocales() ;
           Log.d("TestObject", "assetsList:" + assetsList.length) ;
           Log.d("TestObject", "assetsList item 0:" + assetsList[0]) ;
           Log.d("TestObject", "inputStream:" + inputStream) ;
          
           Log.d("TestObject", "locales:" + locales.length ) ;
           Log.d("TestObject", "locales index 0:" + locales[0] ) ;
           
           
        }catch(java.io.IOException ioe) { Log.d("TestObject", "could not get metadata for object: " + ioe ) ; }
        // String assetStr = assetsList[0] ; 
         WavefrontObject wfobj = new WavefrontObject(assets, "FrameMarkers/bird8.obj") ;
        ArrayList<Group> groups = wfobj.getGroups() ;
        for(Iterator<Group> i = groups.iterator() ; i.hasNext();)
            {
                Group group = i.next() ;
                Log.d("TestObject", "Process Group with index count: " + group.indexCount) ;

                ArrayList<Vertex> vertices = group.vertices ;
                Log.d("TestObject", " vertices:" + vertices.size() ) ;

                ArrayList<Vertex> normals = group.normals ;
                Log.d("TestObject", " normals:" +  normals.size() ) ;

                ArrayList<TextureCoordinate> texcoords = group.texcoords ;
                Log.d("TestObject", " texcoords:" + texcoords.size() ) ;


                ArrayList<Integer> indices = group.indices ;
                Log.d("TestObject", " indices:" +  indices.size() ) ;
                int counter = 0 ; 
                flattenedVertices = new double[vertices.size() * 3] ;   

                for(Iterator<Vertex> j = vertices.iterator() ; j.hasNext();)
                   {
                       Vertex vertex = j.next() ;
                       flattenedVertices[counter] = vertex.getX() ;
                       counter++ ;
                       flattenedVertices[counter] = vertex.getY() ;
                       counter++ ;
                       flattenedVertices[counter] = vertex.getZ() ;
                       counter++ ;
                   }
                counter = 0 ;
                flattenedNormals = new double[normals.size() * 3] ;   

                for(Iterator<Vertex> j = normals.iterator() ; j.hasNext();)
                   {
                       Vertex normal = j.next() ;
                       flattenedNormals[counter] = normal.getX() ;
                       counter++ ;
                       flattenedNormals[counter] = normal.getY() ;
                       counter++ ;
                       flattenedNormals[counter] = normal.getZ() ;
                       counter++ ;
                   }
                 counter = 0 ;
                 flattenedTexCoords = new double[texcoords.size() * 3] ;
   
                 for(Iterator<TextureCoordinate> l = texcoords.iterator() ; l.hasNext();)
                   {
                        TextureCoordinate tex = l.next() ;
                       flattenedTexCoords[counter] = tex.getU()  ;
                       counter++ ;
                       flattenedTexCoords[counter] = tex.getV()  ;
                       counter++ ;
                       flattenedTexCoords[counter] = tex.getW()  ;
                       counter++ ;

                   }
                 counter = 0 ;
                 convertedIndices = new short[indices.size() ] ;

                  for(Iterator<Integer> m = indices.iterator() ; m.hasNext();)
                   {
                        convertedIndices[counter] =  m.next().shortValue() ;                   
                        counter++ ;
                   }

             }
        mVertBuff = fillBuffer(flattenedVertices);
        mTexCoordBuff = fillBuffer(flattenedTexCoords);
        mNormBuff = fillBuffer(flattenedNormals);
        mIndBuff = fillBuffer(convertedIndices);
    }
    
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff ;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }
    

    @Override
    public int getNumObjectVertex()
    {
        return flattenedVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return convertedIndices.length;
    }
}
