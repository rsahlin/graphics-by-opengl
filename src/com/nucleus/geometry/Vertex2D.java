package com.nucleus.geometry;

/**
 * Utility class for Vertex 2D operations.
 *
 */
public final class Vertex2D {

    public final static int X_AXIS = 0;
    public final static int Y_AXIS = 1;

    /**
     * Calculates the point in the middle of vertex1 and vertex2 and returns.
     * 
     * @param vertex1
     * @param vertex2
     * @return
     */
    public final static float[] middle(float[] vertex1, float[] vertex2) {
        float[] result = new float[2];
        result[X_AXIS] = vertex1[X_AXIS] - (vertex1[X_AXIS] - vertex2[X_AXIS]) / 2;
        result[Y_AXIS] = vertex1[Y_AXIS] - (vertex1[Y_AXIS] - vertex2[Y_AXIS]) / 2;
        return result;
    }

    /**
     * Computes the 2 dimensional distance between the 2 Vertices.
     * 
     * @param start The start position, must contain at least 2 values.
     * @param end The end position, must contain at lest 2 values.
     * @param result Distance between the 2 points is stored here.
     */
    public final static void getDistance(float[] start, float[] end, float[] result) {
        result[X_AXIS] = end[X_AXIS] - start[X_AXIS];
        result[Y_AXIS] = end[Y_AXIS] - start[Y_AXIS];
    }

}
