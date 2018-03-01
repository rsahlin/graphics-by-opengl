package com.nucleus.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.nucleus.common.Constants;

/**
 * Float storage for a variable block, for instance uniform block
 *
 */
public class FloatBlockBuffer extends BlockBuffer {

    private final FloatBuffer buffer;

    /**
     * 
     * @param blockName Name of the block, as defined in the source
     * @param size Number of floats to allocate
     * @param blockIndex Index of the variable block that this buffer belongs to, or {@link Constants#NO_VALUE} if
     * buffer does not belong to a variable block.
     */
    public FloatBlockBuffer(String blockName, int size, int blockIndex) {
        super(ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(), blockName,
                blockIndex);
        buffer = (FloatBuffer) plainBuffer;
    }

    @Override
    public void position(int newPosition) {
        buffer.position(newPosition);
    }

    /**
     * Copies length number of values, beginning at offset from src array into this buffer.
     * 
     * @param src The source buffer to read from
     * @param offset source offset
     * @param length Number of values to put
     */
    public void put(float[] src, int offset, int length) {
        buffer.put(src, offset, length);
    }

    /**
     * Copies length number of values, beginning at offset, from this buffer into dst
     * 
     * @param dst The destination array
     * @param offset destination offset
     * @param length Number of values to get
     */
    public void get(float[] dst, int offset, int length) {
        buffer.get(dst, offset, length);
    }

}
