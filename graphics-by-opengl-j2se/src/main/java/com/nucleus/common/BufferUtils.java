package com.nucleus.common;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.nucleus.SimpleLogger;

public class BufferUtils {

    /**
     * Outputs count number of values from the buffer to log - use this for debugging
     * 
     * @param buffer
     * @param count
     */
    public static void logBuffer(Buffer buffer, int count) {
        if (buffer instanceof FloatBuffer) {
            logBuffer((FloatBuffer) buffer, count);
        } else {
            throw new IllegalArgumentException("Not implemented for " + buffer.getClass().getCanonicalName());
        }
    }

    /**
     * Outputs count number of values from the buffer to log - use this for debugging
     * 
     * @param buffer
     * @param count
     */
    public static void logBuffer(FloatBuffer buffer, int count) {
        StringBuffer sb = new StringBuffer();
        float[] storage = new float[count];
        buffer.get(storage);
        for (int i = 0; i < count; i++) {
            sb.append(Float.toString(storage[i]) + ", ");
        }
        SimpleLogger.d(BufferUtils.class, "Contents of float buffer:\n" + sb.toString());
    }

}
