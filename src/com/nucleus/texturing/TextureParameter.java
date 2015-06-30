package com.nucleus.texturing;

import com.nucleus.common.StringUtils;
import com.nucleus.io.DataSetup;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.types.DataType;

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

    public enum TextureParameterMapping implements DataIndexer {
        MIN_FILTER(0, DataType.TEXTURE_PARAMETER),
        MAG_FILTER(1, DataType.TEXTURE_PARAMETER),
        WRAP_S(2, DataType.TEXTURE_PARAMETER),
        WRAP_T(3, DataType.TEXTURE_PARAMETER);

        private final int index;
        private final DataType type;

        private TextureParameterMapping(int index, DataType type) {
            this.index = index;
            this.type = type;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public DataType getType() {
            return type;
        }

    }

    /**
     * Default constructor
     */
    public TextureParameter() {
        super();
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
     * Returns the string (name) for a texture parameter value, eg 9728 will return NEAREST
     * Can be used when exporting to texture format for readability
     * 
     * @param value The texture parameter value, ie the GL value.
     * @return The string name of the value or null.
     */
    public static String valueToString(int value) {

        for (Name n : Name.values()) {
            if (n.value == value) {
                return n.toString();
            }
        }
        return null;

    }

    /**
     * Returns the String value for the specified parameter
     * 
     * @param name
     * @return
     */
    public String getValueAsString(TextureParameterMapping name) {
        return valueToString(values[name.getIndex()]);
    }

    @Override
    public int importData(String[] data, int offset) {
        setValues(data, offset);
        return TextureParameterMapping.values().length;
    }

    @Override
    public String exportDataAsString() {
        return StringUtils.getString(exportDataAsStringArray());
    }

    @Override
    public String[] exportDataAsStringArray() {
        String[] strArray = new String[TextureParameterMapping.values().length];
        setData(strArray, TextureParameterMapping.MIN_FILTER, getValueAsString(TextureParameterMapping.MIN_FILTER));
        setData(strArray, TextureParameterMapping.MAG_FILTER, getValueAsString(TextureParameterMapping.MAG_FILTER));
        setData(strArray, TextureParameterMapping.WRAP_S, getValueAsString(TextureParameterMapping.WRAP_S));
        setData(strArray, TextureParameterMapping.WRAP_T, getValueAsString(TextureParameterMapping.WRAP_T));
        return strArray;
    }
}
