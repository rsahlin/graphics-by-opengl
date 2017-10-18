package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;

/**
 * A texture that has an array of UV coordinates + width/height,so that it can hold data for a number of sprite frames.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class UVTexture2D extends Texture2D {

    @SerializedName("UVAtlas")
    UVAtlas UVAtlas;

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

}
