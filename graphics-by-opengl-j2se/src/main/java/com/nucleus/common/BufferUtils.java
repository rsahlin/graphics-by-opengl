package com.nucleus.common;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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

    /**
     * Returns the contents of buffer as a String, adjusting length to match position and capacity
     * 
     * @param position
     * @param length
     * @param buffer
     * @return
     */
    public static String getContentAsString(int position, int length, ByteBuffer buffer) {
        if (buffer != null) {
            StringBuffer result = new StringBuffer();
            length = length < buffer.capacity() - position ? length : buffer.capacity() - position;
            buffer.position(position);
            for (int i = 0; i < length; i++) {
                result.append(Integer.toString(buffer.get()) + ", ");
            }
            return result.toString();
        }
        return "Null buffer";
    }

    /**
     * Returns the contents of buffer as a String, adjusting length to match position and capacity
     * 
     * @param position
     * @param length
     * @param buffer
     * @return
     */
    public static String getContentAsString(int position, int length, ShortBuffer buffer) {
        if (buffer != null) {
            StringBuffer result = new StringBuffer();
            length = length < buffer.capacity() - position ? length : buffer.capacity() - position;
            buffer.position(position);
            for (int i = 0; i < length; i++) {
                result.append(Integer.toString(buffer.get()) + ", ");
            }
            return result.toString();
        }
        return "Null buffer";
    }

    /**
     * Returns the contents of buffer as a String, adjusting length to match position and capacity
     * 
     * @param position
     * @param length
     * @param buffer
     * @return
     */
    public static String getContentAsString(int position, int length, FloatBuffer buffer) {
        if (buffer != null) {
            StringBuffer result = new StringBuffer();
            length = length < buffer.capacity() - position ? length : buffer.capacity() - position;
            buffer.position(position);
            for (int i = 0; i < length; i++) {
                result.append(Float.toString(buffer.get()) + ", ");
            }
            return result.toString();
        }
        return "Null buffer";
    }

}
