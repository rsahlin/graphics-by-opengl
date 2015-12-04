package com.nucleus.scene;

/**
 * Transform data
 * translate is simply offset values for 1-3 dimensions
 * scale is scale values for 1-3 dimensions
 * rotate is axis-angle, ie 4 values for 3 dimensional rotation
 * Use {@link Axis} to index values.
 * 
 * @author Richard Sahlin
 *
 */
public class TransformData {

    float[] translate;
    float[] scale;
    float[] rotate;

    public float[] getTranslate() {
        return translate;
    }

    public float[] getScale() {
        return scale;
    }

    public float[] getRotate() {
        return rotate;
    }

}
