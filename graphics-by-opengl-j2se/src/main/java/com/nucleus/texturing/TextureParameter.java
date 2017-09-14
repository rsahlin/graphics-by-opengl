package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter {

    public static final TexParameter[] DEFAULT_TEXTURE_PARAMETERS = new TexParameter[] { TexParameter.NEAREST, TexParameter.NEAREST, TexParameter.CLAMP,
            TexParameter.CLAMP };

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
     * Texture parameter values, MUST contain 4 values
     */
    @SerializedName("values")
    protected TexParameter[] values;

    /**
     * Default constructor
     */
    public TextureParameter() {
        super();
    }

    /**
     * Creates new texture parameters, must contain 4 values.
     * 
     * @param values 4 texture parameters, one for eac {@link Name}
     * @throws IllegalArgumentException If params does not contain 4 values
     */
    public TextureParameter(TexParameter[] values) {
        if (values == null || values.length < 4 || values.length > 4) {
            throw new IllegalArgumentException("Invalid parameters, length of array is not 4");
        }
        setValues(values);
    }

    public TextureParameter(TexParameter min, TexParameter mag, TexParameter wrapS, TexParameter wrapT) {
        setValues(values);
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
     * @throws IllegalArgumentException if values is null or does not contain 4 values, or contains invalid values
     */
    public void setValues(TexParameter[] values) {
        if (values == null || values.length < 4) {
            throw new IllegalArgumentException("Invalid values:" + values);
        }
        if (this.values == null) {
            this.values = new TexParameter[Name.values().length];
        }
        int index = 0;
        for (TexParameter p : values) {
            this.values[index++] = p;
        }
        validateValues();
    }

    /**
     * Replaces the current values with the specified min, mag, wrapS, wrapT values
     * 
     * @param min
     * @param mag
     * @param wrapS
     * @param wrapT
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public void setValues(TexParameter min, TexParameter mag, TexParameter wrapS, TexParameter wrapT) {
        if (values == null) {
            values = new TexParameter[Name.values().length];
        }
        values[Name.MIN_FILTER.index] = min;
        values[Name.MAG_FILTER.index] = mag;
        values[Name.WRAP_S.index] = wrapS;
        values[Name.WRAP_T.index] = wrapT;
        validateValues();
    }

    protected void validateValues() {
        if (!TexParameter.validateMinFilter(values[Name.MIN_FILTER.index])
                || TexParameter.validateMagFilter(values[Name.MAG_FILTER.index])
                || !TexParameter.validateWrapMode(values[Name.WRAP_S.index]) || !TexParameter.validateWrapMode(values[Name.WRAP_T.index])) {
            throw new IllegalArgumentException(
                    "Invalid texture mode:" + values[0] + ", " + values[1] + ", " + values[2] + ", " + values[3]);
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
