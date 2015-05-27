package com.nucleus.texturing;

import com.nucleus.io.DataSetup;
import com.nucleus.opengl.GLES20Wrapper.GLES20;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter extends DataSetup {

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

    public enum TextureParameterMapping implements Indexer {
        MIN_FILTER(0),
        MAG_FILTER(1),
        WRAP_S(2),
        WRAP_T(3);

        private final int index;

        private TextureParameterMapping(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
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
     * Sets the texture parameter values from the String array, use values from the Name enum, eg "CLAMP" for GL_CLAMP
     * 
     * @param parameters
     * @param offset Offset into parameters where values are read.
     */
    public void setValues(String[] parameters, int offset) {
        values[MIN_FILTER] = Name.valueOf(getString(parameters, offset, TextureParameterMapping.MIN_FILTER)).value;
        values[MAG_FILTER] = Name.valueOf(getString(parameters, offset, TextureParameterMapping.MAG_FILTER)).value;
        values[WRAP_S] = Name.valueOf(getString(parameters, offset, TextureParameterMapping.WRAP_S)).value;
        values[WRAP_T] = Name.valueOf(getString(parameters, offset, TextureParameterMapping.WRAP_T)).value;
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
        super();
    }

    @Override
    public int importData(String[] data, int offset) {
        setValues(data, offset);
        return TextureParameterMapping.values().length;
    }

    @Override
    public String exportDataAsString() {
        // TODO Auto-generated method stub
        return null;
    }

}
