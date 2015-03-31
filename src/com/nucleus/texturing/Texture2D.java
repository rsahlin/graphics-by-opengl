package com.nucleus.texturing;

import com.nucleus.opengl.GLES20Wrapper;
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
     * Width of texture in pixels
     */
    protected int width;
    /**
     * Height of texture in pixels
     */
    protected int height;

    /**
     * min filter, mag filter, wrap s and wrap t parameters
     */
    protected final int[] parameters = new int[] { GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_TEXTURE_WRAP_T };

    /**
     * Texture parameter values.
     */
    protected final int[] values = new int[] { GLES20.GL_NEAREST, GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE,
            GLES20.GL_CLAMP_TO_EDGE };

    /**
     * Creates a texture reference with name, width and height.
     * 
     * @param name
     * @param width
     * @param height
     */
    public Texture2D(int name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the texture name
     * 
     * @return
     */
    public int getName() {
        return name;
    }

    /**
     * Returns the texture parameter values, min and mag filter, wrap s and t.
     * 
     * @return The texture parameter values
     */
    public int[] getValues() {
        return values;
    }

    /**
     * Returns the width, in pixels, of the texture
     * 
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height, pixels, of the texture
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the texture parameter values for this texture to OpenGL, call this to set the correct texture parameters
     * when rendering.
     * 
     * @param gles
     */
    public void setValues(GLES20Wrapper gles) {

        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                values[Texture2D.MIN_FILTER_INDEX]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                values[Texture2D.MAG_FILTER_INDEX]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                values[Texture2D.WRAP_S_INDEX]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                values[Texture2D.WRAP_T_INDEX]);

    }

}
