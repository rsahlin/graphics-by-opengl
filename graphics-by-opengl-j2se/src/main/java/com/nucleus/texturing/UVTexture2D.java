package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.gson.PostDeserializable;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.FloatBlockBuffer;

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
    transient BlockBuffer uvData;

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

    /**
     * Returns the buffer containing the uv atlas
     * 
     * @return
     */
    public BlockBuffer getUVAtlasBuffer() {
        return uvData;
    }

    @Override
    public int getFrameCount() {
        return UVAtlas.getFrameCount();
    }

    @Override
    public void postDeserialize() {
        if (UVAtlas != null) {
            float[] data = UVAtlas.getUVData();
            uvData = new FloatBlockBuffer(data.length);
            uvData.put(data, 0, data.length);
        }
    }

}
