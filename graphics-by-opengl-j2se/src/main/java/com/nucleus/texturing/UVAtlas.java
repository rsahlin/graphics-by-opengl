package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.BaseReference;

/**
 * Quad UV data for frames in a texture/image, this is used for Sprites, ie a Quad instead of triangle.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class UVAtlas extends BaseReference {

    /**
     * Using U and V
     */
    private final static int COMPONENTS = 2;

    @SerializedName("UVData")
    private float[] UVData;

    /**
     * Returns all Quad UV data, or null if not set.
     * 
     * @return
     */
    public float[] getFrames() {
        return UVData;
    }

    /**
     * Returns the number of frames that are defined
     * 
     * @return Number of frames of UV data.
     */
    public int getFrameCount() {
        return UVData.length / (4 * 2);
    }

    /**
     * Returns the array holding all UV frames, each frame is made up of 4 UV pairs, one for each corner of the frame.
     * 
     * @return
     */
    public float[] getUVFrames() {
        return UVData;
    }

    /**
     * Stores the UV coordinates for a sprite (quad) frame at the specified index
     * 
     * @param frame Frame number to fetch UV data for.
     * @param destination Destination array
     * @param destIndex Index in destination where UV data is stored
     */
    public void getUVData(int frame, float[] destination, int destIndex) {
        int sourceIndex = frame * 4 * COMPONENTS;
        System.arraycopy(UVData, sourceIndex, destination, destIndex, 4 * COMPONENTS);
    }
}
