package com.nucleus.data;

import com.google.gson.annotations.SerializedName;

/**
 * This class is deprecated - do NOT use Anchor, use position + size instead.
 * Anchor values for objects
 * The anchor value for each axis will be multiplied by the size of the object,
 * 0 on the X axis will give an anchor value of 0, 1 will give an anchor value of width.
 * A value of 0.5 will center the object.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
@Deprecated
public class Anchor {

    /**
     * The anchor values for the axis in use
     */
    @SerializedName("values")
    private float[] values;

    public Anchor(Anchor source) {
        set(source);
    }

    /**
     * Sets the values from the source Anchor, createing the array for values if needed.
     * 
     * @param source
     */
    public void set(Anchor source) {
        float[] v = source.getValues();
        if (values == null || values.length != v.length) {
            values = new float[v.length];
        }
        System.arraycopy(v, 0, values, 0, v.length);
    }

    public float[] getValues() {
        return values;
    }

    /**
     * Calculates the offsets for the axis based on the sizes, the resulting array will hold size[axis] * -anchor[axis]
     * 
     * @param size
     * @return Array with offset for the anchor values and sizes, if there are less values in size the remaining values
     * will hold only the anchor value
     */
    public float[] calcOffsets(float[] size) {
        float[] offsets = new float[values.length];
        for (int i = 0; i < offsets.length; i++) {
            if (i >= size.length) {
                offsets[i] = values[i];
            } else {
                offsets[i] = -values[i] * size[i];
            }
        }
        return offsets;
    }

}
