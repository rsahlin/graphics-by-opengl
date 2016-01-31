package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.BaseReference;

/**
 * Quad UV data for frames in a texture/image, this is used for Sprites, ie a Quad instead of triangle.
 * This class can be serialized using GSON
 * UV data is defined as starting point, normally upper left but this depends on vertex ordering, followed by width and
 * height.
 * 
 * @author Richard Sahlin
 *
 */
public class UVAtlas extends BaseReference {

    /**
     * Number of components in one frame
     * Using U, V, width and height
     */
    public final static int COMPONENTS = 4;
    /**
     * Index of U component
     */
    public final static int U = 0;
    /**
     * Index of V component
     */
    public final static int V = 1;
    /**
     * Index of width component
     */
    public final static int WIDTH = 2;
    /**
     * Index of height component
     */
    public final static int HEIGHT = 3;

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
        return UVData.length / (COMPONENTS);
    }

    /**
     * Returns the array holding all UV data
     * 
     * @return
     */
    public float[] getUVData() {
        return UVData;
    }

    /**
     * Stores the UV data for a sprite (quad) frame at the specified index, this will put the
     * UV start pointa + width/height at the specified destination index.
     * 
     * @param frame Frame number to fetch UV data for.
     * @param destination Destination array
     * @param destIndex Index in destination where UV + width/height is stored
     */
    public void getUVData(int frame, float[] destination, int destIndex) {
        int sourceIndex = frame * COMPONENTS;
        System.arraycopy(UVData, sourceIndex, destination, destIndex, COMPONENTS);
    }

    /**
     * Stores one frame of UV data for a sprite (quad) frame at the specified index, this will
     * store 4 UV coordinates that can be used to set texture coordinates from.
     * 
     * @param frame Frame number to fetch UV data for.
     * @param destination Destination array
     * @param destIndex Index in destination where UV coordinates for 4 vertices are stored.
     * @throws ArrayIndexOutOfBoundsException If destination does not have room for 4 UV coordinates at destIndex
     */
    public void getUVFrame(int frame, float[] destination, int destIndex) {
        int sourceIndex = frame * COMPONENTS;
        int index = 0;
        float u = UVData[sourceIndex + U];
        float v = UVData[sourceIndex + V];
        float w = UVData[sourceIndex + WIDTH];
        float h = UVData[sourceIndex + HEIGHT];
        destination[index++] = u;
        destination[index++] = v;
        destination[index++] = u + w;
        destination[index++] = v;
        destination[index++] = u + w;
        destination[index++] = v - h;
        destination[index++] = u;
        destination[index++] = v - h;
    }
}
