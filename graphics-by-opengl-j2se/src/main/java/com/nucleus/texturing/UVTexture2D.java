package com.nucleus.texturing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.gson.PostDeserializable;

/**
 * A texture that has an array of UV coordinates + width/height,so that it can hold data for a number of sprite frames.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class UVTexture2D extends Texture2D implements PostDeserializable {

    @SerializedName("UVAtlas")
    UVAtlas UVAtlas;

    /**
     * Currently copied from UVAtlas after deserialization
     * TODO Implement using binary loader.
     */
    transient FloatBuffer uvData;

    public UVTexture2D() {
        super();
    }

    protected UVTexture2D(UVTexture2D source) {
        set(source);
    }

    /**
     * Copies the data from the source into this
     * 
     * @param source
     */
    protected void set(UVTexture2D source) {
        super.set(source);
        this.UVAtlas = source.UVAtlas;
    }

    /**
     * Returns the frame definitions
     * 
     * @return UVAtlas
     */
    public UVAtlas getUVAtlas() {
        return UVAtlas;
    }

    @Override
    public int getFrameCount() {
        return UVAtlas.getFrameCount();
    }

    @Override
    public void postDeserialize() {
        if (UVAtlas != null) {
            float[] data = UVAtlas.getUVData();
            uvData = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            uvData.put(data);
        }
    }

}
