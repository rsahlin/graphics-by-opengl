package com.nucleus.texturing;

import com.nucleus.opengl.GLES20Wrapper.GLES20;

/**
 * Texture object and Sampler info, this class holds the texture object ID, texture parameters but should
 * NOT keep the texture data.
 * 
 * @author Richard Sahlin
 *
 */
public class Texture2D {

    public final static int TEXTURE_0 = 0;

    /**
     * Index into parameters where min filter value is
     */
    public final static int MIN_FILTER_INDEX = 0;
    /**
     * Index into parameters where mag filter value is
     */
    public final static int MAG_FILTER_INDEX = 1;
    /**
     * Index into parameters where wrap s value is
     */
    public final static int WRAP_S_INDEX = 2;
    /**
     * Index into parameters where wrap t value is
     */
    public final static int WRAP_T_INDEX = 3;

    /**
     * The texture name, this is a loose reference to the allocated texture name.
     * It is up to the caller to allocate and release texture names.
     */
    protected int name;

    /**
     * min filter, mag filter, wrap s and wrap t parameters
     */
    protected final int[] parameters = new int[] { GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_TEXTURE_WRAP_T };

    public Texture2D(int name) {
        this.name = name;
    }

    /**
     * Returns the texture name
     * 
     * @return
     */
    public int getName() {
        return name;
    }

}
