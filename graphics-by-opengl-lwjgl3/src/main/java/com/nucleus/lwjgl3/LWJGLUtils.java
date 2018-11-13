package com.nucleus.lwjgl3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.nucleus.common.BufferUtils;

public class LWJGLUtils {

    /**
     * Copies the data from the source intbuffer to the destination
     * 
     * @param source
     * @param dest
     * @param destOffset
     */
    protected static void toArray(IntBuffer source, int[] dest, int destOffset) {
        while (source.hasRemaining()) {
            dest[destOffset++] = source.get();
        }
    }

    /**
     * Copies the data from the source bytebuffer to the destination
     * 
     * @param source
     * @param dest
     * @param destOffset
     */
    protected static void toArray(ByteBuffer source, byte[] dest, int destOffset) {
        while (source.hasRemaining()) {
            dest[destOffset++] = source.get();
        }
    }

    protected static IntBuffer toIntBuffer(int[] data, int length, int offset) {
        IntBuffer ib = BufferUtils.createIntBuffer(length);
        ib.put(data, offset, length);
        ib.position(0);
        return ib;
    }

}
