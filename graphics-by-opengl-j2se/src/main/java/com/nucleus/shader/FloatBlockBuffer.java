package com.nucleus.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.nucleus.SimpleLogger;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;

/**
 * Float storage for a variable block, for instance uniform block
 *
 */
public class FloatBlockBuffer extends BlockBuffer {

    private final FloatBuffer buffer;

    /**
     * Creates a new float block buffer, with the name and size - this buffer is not tied to a {@link InterfaceBlock}
     * 
     * @param blockName
     * @param size
     */
    public FloatBlockBuffer(String blockName, int size) {
        super(ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(), blockName, size * 4);
        buffer = (FloatBuffer) plainBuffer;
    }

    /**
     * Creates a new float block buffer that is tied to the specified {@link InterfaceBlock}
     * 
     * @param blockName Name of the block, as defined in the source
     * @param size Number of floats to allocate
     * @param interfaceBlock The variable block this buffer belongs to - or null if not used in a variable block.
     */

    public FloatBlockBuffer(InterfaceBlock interfaceBlock, int size) {
        super(ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(), interfaceBlock.name,
                interfaceBlock);
        buffer = (FloatBuffer) plainBuffer;
        SimpleLogger.d(getClass(),
                "Created FloatBlockBuffer for block: " + interfaceBlock.name + ", capacity: " + buffer.capacity());
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
        dirty = true;
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
