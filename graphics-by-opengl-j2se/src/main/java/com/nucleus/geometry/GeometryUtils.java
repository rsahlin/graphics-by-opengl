package com.nucleus.geometry;

import java.nio.FloatBuffer;

/**
 * Utility class for geometry related operations.
 * 
 * @author Richard Sahlin
 *
 */
public class GeometryUtils {
    /**
     * Same as calling {@link #calculateBounds2D(AttributeBuffer, int)} with the vertice count and first
     * rewinding the buffer.
     * 
     * @param vertices
     * @return
     */
    public static float[] calculateBounds2D(AttributeBuffer vertices) {
        vertices.getBuffer().rewind();
        return calculateBounds2D(vertices, vertices.getVerticeCount());
    }

    /**
     * Calculates the axis aligned 2D bounds for the vertice buffer, starting at the current position.
     * 
     * @param vertices Buffer with vertex values
     * @param count Number of vertices to include in calculation
     * @return Array with the smallest and largest corner (x1y1x2y2)
     */
    public static float[] calculateBounds2D(AttributeBuffer vertices, int count) {
        float[] result = null;
        int stride = (vertices.getByteStride() / 4);
        float[] values = new float[stride];
        FloatBuffer buffer = (FloatBuffer) vertices.getBuffer();
        for (int i = 0; i < count; i++) {
            buffer.get(values);
            if (result == null) {
                result = new float[4];
                System.arraycopy(values, 0, result, 0, 2);
                System.arraycopy(values, 0, result, 2, 2);
            } else {
                result[0] = Math.min(values[0], result[0]);
                result[1] = Math.min(values[1], result[1]);
                result[2] = Math.max(values[2], result[2]);
                result[3] = Math.max(values[3], result[3]);
            }
        }
        return result;
    }
    
}
