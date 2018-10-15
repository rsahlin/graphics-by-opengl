package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * sampler
 * Texture sampler properties for filtering and wrapping modes.
 * 
 * Related WebGL functions: texParameterf()
 * 
 * Properties
 * 
 * Type Description Required
 * magFilter integer Magnification filter. No
 * minFilter integer Minification filter. No
 * wrapS integer s wrapping mode. No, default: 10497
 * wrapT integer t wrapping mode. No, default: 10497
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class Sampler extends GLTFNamedValue {

    private static final String MAG_FILTER = "magFilter";
    private static final String MIN_FILTER = "minFilter";
    private static final String WRAP_S = "wrapS";
    private static final String WRAP_T = "wrapT";

    @SerializedName(MAG_FILTER)
    private int magFilter = GLES20.GL_NEAREST;
    @SerializedName(MIN_FILTER)
    private int minFilter = GLES20.GL_NEAREST;
    @SerializedName(WRAP_S)
    private int wrapS = GLES20.GL_REPEAT;
    @SerializedName(WRAP_T)
    private int wrapT = GLES20.GL_REPEAT;

    public int getMagFilter() {
        return magFilter;
    }

    public int getMinFilter() {
        return minFilter;
    }

    public int getWrapS() {
        return wrapS;
    }

    public int getWrapT() {
        return wrapT;
    }

}
