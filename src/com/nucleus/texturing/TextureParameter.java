package com.nucleus.texturing;

import com.nucleus.opengl.GLES20Wrapper.GLES20;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter {

    public enum Name {
        NEAREST(GLES20.GL_NEAREST),
        LINEAR(GLES20.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GLES20.GL_NEAREST_MIPMAP_NEAREST),
        LINEAR_MIPMAP_NEAREST(GLES20.GL_LINEAR_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GLES20.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_LINEAR(GLES20.GL_LINEAR_MIPMAP_LINEAR),
        CLAMP(GLES20.GL_CLAMP_TO_EDGE),
        REPEAT(GLES20.GL_REPEAT),
        MIRRORED_REPEAT(GLES20.GL_MIRRORED_REPEAT);

        private final int value;

        private Name(int value) {
            this.value = value;
        }

        /**
         * Returns the GL value for the texture parameter
         * 
         * @return
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Index into parameters where min filter value is
     */
    public final static int MIN_FILTER = 0;
    /**
     * Index into parameters where mag filter value is
     */
    public final static int MAG_FILTER = 1;
    /**
     * Index into parameters where wrap s value is
     */
    public final static int WRAP_S = 2;
    /**
     * Index into parameters where wrap t value is
     */
    public final static int WRAP_T = 3;

    /**
     * Texture parameter values.
     */
    protected final int[] values = new int[] { GLES20.GL_NEAREST, GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE,
            GLES20.GL_CLAMP_TO_EDGE };

    /**
     * Creates a texture parameter from the String array, the array shall contain the texture parameter values
     * as defined by Names enum
     * 
     * @param parameters String names matched to Name enum
     */
    public TextureParameter(String[] parameters) {
        setValues(parameters);
    }

    /**
     * Sets the texture parameter values from the String array, use values from the Name enum, eg "CLAMP" for GL_CLAMP
     * 
     * @param parameters
     */
    public void setValues(String[] parameters) {
        values[MIN_FILTER] = Name.valueOf(parameters[MIN_FILTER]).value;
        values[MAG_FILTER] = Name.valueOf(parameters[MAG_FILTER]).value;
        values[WRAP_S] = Name.valueOf(parameters[WRAP_S]).value;
        values[WRAP_T] = Name.valueOf(parameters[WRAP_T]).value;
    }

    /**
     * Sets the texture parameter values
     * 
     * @param minFilter
     * @param magFilter
     * @param wrapS
     * @param wrapT
     */
    public void setValues(int minFilter, int magFilter, int wrapS, int wrapT) {
        values[MIN_FILTER] = minFilter;
        values[MAG_FILTER] = magFilter;
        values[WRAP_S] = wrapS;
        values[WRAP_T] = wrapT;
    }

    /**
     * Default constructor, creates default texture parameters
     */
    public TextureParameter() {
    }

}
