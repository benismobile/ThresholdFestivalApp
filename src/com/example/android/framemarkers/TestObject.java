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

public class TestObject extends MeshObject
{
    // Data for drawing the 3D plane as overlay
    private static final double letterVertices[] = { 
             };
    
    private static final double letterNormals[] = {


            };
    
    private static final double letterTexcoords[] = 
 {
 };
    
    private static final short letterIndices[] = { 
       };
    
    Buffer mVertBuff;
    Buffer mTexCoordBuff;
    Buffer mNormBuff;
    Buffer mIndBuff;
    
    
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

        mVertBuff = fillBuffer();
        mTexCoordBuff = fillBuffer(letterTexcoords);
        mNormBuff = fillBuffer(letterNormals);
        mIndBuff = fillBuffer(letterIndices);
    }
    
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = null ; // getVerticesFromFile();
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
    
    private double[] getVerticesFromFile()
    {

            return null ;

    }

    @Override
    public int getNumObjectVertex()
    {
        return letterVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return letterIndices.length;
    }
}
