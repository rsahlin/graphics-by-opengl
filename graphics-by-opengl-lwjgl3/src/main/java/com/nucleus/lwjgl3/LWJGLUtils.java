package com.nucleus.lwjgl3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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

    protected static FloatBuffer toFloatBuffer(float[] data, int length, int offset) {
        FloatBuffer fb = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(data, offset, length);
        fb.position(0);
        return fb;
    }

    protected static IntBuffer toIntBuffer(int[] data, int length, int offset) {
        IntBuffer ib = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ib.put(data, offset, length);
        ib.position(0);
        return ib;
    }

}
