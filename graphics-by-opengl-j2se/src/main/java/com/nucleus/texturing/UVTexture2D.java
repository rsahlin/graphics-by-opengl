package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;

/**
 * A texture that has an array of UV coordinates so that it can hold data for a number of sprite frames.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class UVTexture2D extends Texture2D {

    @SerializedName("UVFrames")
    String UVFrames;

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
        this.UVFrames = source.UVFrames;
    }

    /**
     * Returns the name of the frame definitions, ie the reference to UVAtlas
     * 
     * @return Reference to UVAtlas
     */
    public String getUVFramesName() {
        return UVFrames;
    }

}
