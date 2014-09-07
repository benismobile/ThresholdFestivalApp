/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.example.android.framemarkers;

import java.nio.Buffer;
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
        InputStream inputStream = assets.open("Framemarkers/bird8.obj", AssetManager.ACCESS_BUFFER);
        WavefrontObject wfobj = new WavefrontObject(inputStream) ;
        mVertBuff = fillBuffer(letterVertices);
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
