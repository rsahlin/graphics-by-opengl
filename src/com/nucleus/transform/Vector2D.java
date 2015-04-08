package com.nucleus.transform;

/**
 * Utility class for euclidean 2d vector operations, ie the vector is made up of direction and magnitude.
 * A 2D vector is made up of 3 float values, the methods in this class either take this class as parameter or just the
 * float[] with an index.
 *
 */
public final class Vector2D {

    public final static int X_AXIS = 0;
    public final static int Y_AXIS = 1;
    public final static int MAGNITUDE = 2;
    public final float[] vector = new float[3];

    public Vector2D() {

    }

    /**
     * Creates a 2D vector by normalizing the x and y value and storing the length as the 3'rd component.
     * 
     * @param x
     * @param y
     */
    public Vector2D(float x, float y) {
        setNormalized(x, y);
    }

    /**
     * Sets x and y as normalized direction and magnitude.
     * 
     * @param x Size of vector x axis
     * @param y Size of vector y axis
     */
    public void setNormalized(float x, float y) {
        float length = length(x, y);
        vector[X_AXIS] = x / length;
        vector[Y_AXIS] = y / length;
        vector[MAGNITUDE] = length;
    }

    /**
     * Creates a 2D vector by normalizing the first 2 (x and y) values in the input array and storing the length as the
     * magnitude.
     * 
     * @param values
     */
    public Vector2D(float[] values) {
        float length = length(values[X_AXIS], values[Y_AXIS]);
        vector[X_AXIS] = values[X_AXIS] / length;
        vector[Y_AXIS] = values[Y_AXIS] / length;
        vector[MAGNITUDE] = length;
    }

    /**
     * Computes the length of the 2D vector made up of x and y.
     * 
     * @param x
     * @param y
     * @return Length of the 2D vector.
     */
    public final static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the dot product of this Vector and vector2
     * 
     * @param vector2
     * @return The dot product of the 2 Vectors
     */
    public final float dot(Vector2D vector2) {
        return vector[X_AXIS] * vector2.vector[X_AXIS] + vector[Y_AXIS] * vector2.vector[Y_AXIS];
    }

    /**
     * Calculate the dot product of this Vector and vector2
     * 
     * @param vector2 Float array with 2 values for x and y.
     * @return The dot product of the 2 Vectors
     */
    public final float dot(float[] vector2) {
        return vector[X_AXIS] * vector2[X_AXIS] + vector[Y_AXIS] * vector2[Y_AXIS];
    }

}
