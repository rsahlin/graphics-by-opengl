package com.nucleus.texturing;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter {

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
     * Creates texture parameters with the specified values for MIN_FILTER, MAG_FILTER, WRAP_S and WRAP_T
     * 
     * @param params
     */
    public TextureParameter(TexParameter[] params) {
        int index = 0;
        for (TexParameter tp : params) {
            if (index < values.length) {
                values[index++] = tp;
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
    protected final TexParameter[] values = new TexParameter[] { TexParameter.NEAREST, TexParameter.NEAREST,
            TexParameter.CLAMP, TexParameter.CLAMP };

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

}
