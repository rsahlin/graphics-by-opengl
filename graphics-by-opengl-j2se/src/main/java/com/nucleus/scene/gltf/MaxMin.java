package com.nucleus.scene.gltf;

import com.nucleus.vecmath.Matrix;

/**
 * Simple class to wrap accessor Max Min values.
 *
 */
public class MaxMin {

    private final int COMPONENTS = 3;

    protected float[] maxmin = new float[] { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE,
            Float.MAX_VALUE, Float.MAX_VALUE };

    public MaxMin() {

    }

    public MaxMin(float[] max, float[] min) {
        System.arraycopy(max, 0, maxmin, 0, COMPONENTS);
        System.arraycopy(min, 0, maxmin, COMPONENTS, COMPONENTS);
    }

    /**
     * Updates the max/min values in this object compared to the specicied transformed maxmin
     * 
     * @param compare This will be transformed using the matrix then compared to values in this object
     * @param matrix
     * 
     */
    public void update(MaxMin compare, float[] matrix) {
        update(compare.maxmin, matrix);
    }

    /**
     * Updates the max/min values in this object compared to the specified values, incoming max/min will be transformed
     * using matrix
     * 
     * @param maxmin
     * @param Transform matrix
     */
    public void update(float[] maxmin, float[] matrix) {
        float[] vec = new float[COMPONENTS * 2];
        Matrix.transformVec3(matrix, 0, maxmin, vec, 2);

        this.maxmin[0] = Float.max(vec[0], this.maxmin[0]);
        this.maxmin[1] = Float.max(vec[1], this.maxmin[1]);
        this.maxmin[2] = Float.max(vec[2], this.maxmin[2]);

        this.maxmin[3] = Float.min(vec[3], this.maxmin[3]);
        this.maxmin[4] = Float.min(vec[4], this.maxmin[4]);
        this.maxmin[5] = Float.min(vec[5], this.maxmin[5]);
    }

    /**
     * Returns the max of the 3 components
     * 
     * @return
     */
    public float getMaxValue() {
        return Float.max(maxmin[0], Float.max(maxmin[1], maxmin[2]));
    }

    /**
     * Returns the min of the 3 components
     * 
     * @return
     */
    public float getMinValue() {
        return Float.min(maxmin[3], Float.min(maxmin[4], maxmin[5]));
    }

    /**
     * Returns the min, x, y and z value
     * 
     * @param result, result array. If null a new result array is created with size 3
     * @return
     */
    public float[] getMinValue(float[] result) {
        if (result == null) {
            result = new float[3];
        }
        result[0] = maxmin[3];
        result[1] = maxmin[4];
        result[2] = maxmin[5];
        return result;
    }

    /**
     * Returns the max delta value for x, y and z
     * 
     * @param result
     */
    public float[] getMaxDelta(float[] result) {
        result[0] = maxmin[0] - maxmin[3];
        result[1] = maxmin[1] - maxmin[4];
        result[2] = maxmin[2] - maxmin[5];
        return result;
    }

    /**
     * Returns how much this maxmin shall be translated to be centered
     * 
     * @param result
     * @return
     */
    public float[] getTranslateToCenter(float[] result) {
        result[0] = (maxmin[0] + maxmin[3]) / 2;
        result[1] = (maxmin[1] + maxmin[4]) / 2;
        result[2] = (maxmin[2] + maxmin[5]) / 2;

        return result;
    }

}
