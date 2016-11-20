package com.nucleus.texturing;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

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
     * Texture parameter values, MUST contain 4 values
     */
    @SerializedName("values")
    protected ArrayList<TexParameter> values;

    private transient int[] intArray;

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
        if (this.values == null) {
            values = new ArrayList<>();
        } else {
            values.clear();
        }
        intArray = new int[4];
        int index = 0;
        for (TexParameter p : source.values) {
            if (p == null) {
                throw new IllegalArgumentException("Texture parameter value is null, invalid name in source?");
            }
            values.add(p);
            intArray[index++] = p.value;
        }
    }

    /**
     * Clears the current values and sets the specified texture parameter values.
     * 
     * @param values The values to set, shall contain min filter, mag filter, wrap s and wrap t
     */
    public void setValues(TexParameter[] values) {
        if (this.values == null) {
            this.values = new ArrayList<>();
        } else {
            this.values.clear();
        }
        for (TexParameter p : values) {
            this.values.add(p);
        }
    }

    private void createIntArray() {
        intArray = new int[4];
        int index = 0;
        for (TexParameter p : values) {
            intArray[index++] = p.value;
        }
    }

    /**
     * Returns the texture parameters as int array.
     * Caches the result for next usage.
     * 
     * @return
     */
    public int[] getAsIntArray() {
        if (intArray == null) {
            createIntArray();
        }
        return intArray;
    }

}
