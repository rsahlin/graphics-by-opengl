package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter {

    public enum Name {
        MIN_FILTER(0),
        MAG_FILTER(1),
        WRAP_S(2),
        WRAP_T(3);

        public final int index;

        private Name(int index) {
            this.index = index;
        }
    }

    /**
     * Default constructor
     */
    public TextureParameter() {
        super();
    }

    /**
     * Creates a new texture parameter from the source
     * 
     * @param source
     */
    public TextureParameter(TextureParameter source) {
        setValues(source);
    }

    /**
     * Texture parameter values, MUST contain 4 values
     */
    @SerializedName("values")
    protected TexParameter[] values;

    /**
     * Copy values from the source texture parameters
     * 
     * @param source
     * @throws NullPointerException If values is null in source
     */
    public void setValues(TextureParameter source) {
        if (source.values == null) {
            throw new IllegalArgumentException("No texture parameters in source.");
        }
        values = new TexParameter[source.values.length];
        int index = 0;
        for (TexParameter p : source.values) {
            if (p == null) {
                throw new IllegalArgumentException("Texture parameter value is null, invalid name in source?");
            }
            values[index++] = p;
        }
    }

    /**
     * Clears the current values and sets the specified texture parameter values.
     * 
     * @param values The values to set, shall contain min filter, mag filter, wrap s and wrap t
     */
    public void setValues(TexParameter[] values) {
        if (this.values == null) {
            this.values = new TexParameter[Name.values().length];
        }
        int index = 0;
        for (TexParameter p : values) {
            this.values[index++] = p;
        }
    }

    /**
     * Returns the value for the specified parameter name.
     * 
     * @param name
     * @return
     */
    public TexParameter getValue(Name name) {
        return values[name.index];
    }

    /**
     * Checks if the min filter in the texture parameter is one of the mipmap filters.
     * 
     * @return True if the texture parameters use mipmaps
     */
    public boolean isMipMapFilter() {
        switch (getValue(Name.MIN_FILTER)) {
        case LINEAR_MIPMAP_LINEAR:
        case NEAREST_MIPMAP_LINEAR:
        case LINEAR_MIPMAP_NEAREST:
        case NEAREST_MIPMAP_NEAREST:
            return true;
        default:
            return false;
        }
    }
}
