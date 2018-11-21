package com.nucleus.scene.gltf;

/**
 * Simple class to wrap accessor Max Min values.
 *
 */
public class MaxMin {

    protected float[] max = new float[] { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
    protected float[] min = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };

    public MaxMin() {

    }

    public MaxMin(float[] max, float[] min) {
        System.arraycopy(max, 0, this.max, 0, max.length);
        System.arraycopy(min, 0, this.min, 0, min.length);
    }

    /**
     * Updates the max/min values in this object compared to the specified values, incoming max/min will be scaled using
     * the specified scale-factor
     * 
     * @param max
     * @param min
     * @param scale max and min will be scaled according to this before comparison.
     */
    public void update(float[] max, float[] min, float[] scale) {
        this.max[0] = Float.max(max[0] * scale[0], this.max[0]);
        this.max[1] = Float.max(max[1] * scale[1], this.max[1]);
        this.max[2] = Float.max(max[2] * scale[2], this.max[2]);

        this.min[0] = Float.min(min[0] * scale[0], this.min[0]);
        this.min[1] = Float.min(min[1] * scale[1], this.min[1]);
        this.min[2] = Float.min(min[2] * scale[2], this.min[2]);
    }

    /**
     * Returns the max of the 3 components
     * 
     * @return
     */
    public float getMaxValue() {
        return Float.max(max[0], Float.max(max[1], max[2]));
    }

    /**
     * Returns the min of the 3 components
     * 
     * @return
     */
    public float getMinValue() {
        return Float.min(min[0], Float.min(min[1], min[2]));
    }

    /**
     * Returns the max delta value for x and y
     * 
     * @param result
     */
    public void getMaxDeltaXY(float[] result) {
        result[0] = max[0] - min[0];
        result[1] = max[1] - min[1];
    }

}
