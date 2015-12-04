package com.nucleus.texturing;

import com.nucleus.common.StringUtils;
import com.nucleus.io.DataSetup;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.types.DataType;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter extends DataSetup {

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
     * Creates texture parameters with the specified values for MIN_FILTER, MAG_FILTER, WRAP_S and WRAP_T
     * 
     * @param params
     */
    public TextureParameter(TexParameter[] params) {
        int index = 0;
        for (TexParameter tp : params) {
            if (index < values.length) {
                values[index++] = tp.value;
            }
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
        values[MIN_FILTER] = TexParameter.valueOf(getString(parameters, offset, TextureParameterMapping.MIN_FILTER)).value;
        values[MAG_FILTER] = TexParameter.valueOf(getString(parameters, offset, TextureParameterMapping.MAG_FILTER)).value;
        values[WRAP_S] = TexParameter.valueOf(getString(parameters, offset, TextureParameterMapping.WRAP_S)).value;
        values[WRAP_T] = TexParameter.valueOf(getString(parameters, offset, TextureParameterMapping.WRAP_T)).value;
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
     * Copy values from the source texture parameters
     * 
     * @param source
     */
    public void setValues(TextureParameter source) {
        values[MIN_FILTER] = source.values[MIN_FILTER];
        values[MAG_FILTER] = source.values[MAG_FILTER];
        values[WRAP_S] = source.values[WRAP_S];
        values[WRAP_T] = source.values[WRAP_T];
    }

    /**
     * Returns the string (name) for a texture parameter value, eg 9728 will return NEAREST
     * Can be used when exporting to texture format for readability
     * 
     * @param value The texture parameter value, ie the GL value.
     * @return The string name of the value or null.
     */
    public static String valueToString(int value) {

        for (TexParameter n : TexParameter.values()) {
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
