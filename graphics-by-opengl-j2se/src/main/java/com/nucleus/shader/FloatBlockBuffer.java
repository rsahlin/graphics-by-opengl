package com.nucleus.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Float storage for a variable block, for instance uniform block
 *
 */
public class FloatBlockBuffer extends BlockBuffer {

    private FloatBuffer buffer;

    /**
     * 
     * @param size Number of floats to allocate
     */
    public FloatBlockBuffer(int size) {
        buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Override
    public void put(float[] src, int offset, int length) {
        buffer.put(src, offset, length);
    }

    @Override
    public void position(int newPosition) {
        buffer.position(newPosition);
    }

}
