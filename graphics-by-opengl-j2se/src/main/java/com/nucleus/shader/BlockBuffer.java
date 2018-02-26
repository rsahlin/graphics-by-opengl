package com.nucleus.shader;

import com.nucleus.geometry.BufferObject;

/**
 * Storage for a variable block, for instance a uniform block
 *
 */
public abstract class BlockBuffer extends BufferObject {

    /**
     * Copies length number of floats, beginning at srcOffset in src array.
     * 
     * @param src
     * @param offset
     * @param length
     */
    public abstract void put(float[] src, int offset, int length);

    /**
     * Sets the position in the buffer
     * 
     * @param newPosition
     */
    public abstract void position(int newPosition);

}
