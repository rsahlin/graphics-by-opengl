package com.nucleus.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.nucleus.shader.ShaderVariable.VariableBlock;

/**
 * Float storage for a variable block, for instance uniform block
 *
 */
public class FloatBlockBuffer extends BlockBuffer {

    private final FloatBuffer buffer;

    /**
     * Creates a new float block buffer, with the name and size - this buffer is not tied to a {@link VariableBlock}
     * 
     * @param blockName
     * @param size
     */
    public FloatBlockBuffer(String blockName, int size) {
        super(ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(), blockName, null);
        buffer = (FloatBuffer) plainBuffer;
    }

    /**
     * Creates a new float block buffer that is tied to the specified {@link VariableBlock}
     * 
     * @param blockName Name of the block, as defined in the source
     * @param size Number of floats to allocate
     * @param variableBlock The variable block this buffer belongs to - or null if not used in a variable block.
     */

    public FloatBlockBuffer(VariableBlock variableBlock, int size) {
        super(ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(), variableBlock.name,
                variableBlock);
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
