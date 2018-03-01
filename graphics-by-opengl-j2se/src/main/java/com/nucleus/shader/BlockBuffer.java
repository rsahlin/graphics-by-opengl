package com.nucleus.shader;

import java.nio.Buffer;

import com.nucleus.geometry.BufferObject;

/**
 * Storage for a variable block, for instance a uniform block
 * The underlying buffer shall be a java.nio buffer
 *
 */
public abstract class BlockBuffer extends BufferObject {

    protected final Buffer plainBuffer;
    protected final int blockIndex;

    /**
     * Name of the block as defined in the source
     */
    protected final String blockName;

    public BlockBuffer(Buffer buffer, String blockName, int blockIndex) {
        this.blockName = blockName;
        this.blockIndex = blockIndex;
        this.plainBuffer = buffer;
    }

    /**
     * Returns the capacity of the buffer
     * 
     * @return
     */
    public int capacity() {
        return plainBuffer.capacity();
    }

    /**
     * Sets the position in the buffer
     * 
     * @param newPosition
     */
    public abstract void position(int newPosition);

    /**
     * Returns the name of the block, as defined in the source
     * 
     * @return
     */
    public String getBlockName() {
        return blockName;
    }

    public Buffer getBuffer() {
        return plainBuffer;
    }

}
