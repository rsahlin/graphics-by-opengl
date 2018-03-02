package com.nucleus.shader;

import java.nio.Buffer;

import com.nucleus.geometry.BufferObject;
import com.nucleus.shader.ShaderVariable.VariableBlock;

/**
 * Storage for a variable block, for instance a uniform block
 * The underlying buffer shall be a java.nio buffer
 *
 */
public abstract class BlockBuffer extends BufferObject {

    protected final Buffer plainBuffer;
    protected final VariableBlock variableBlock;

    /**
     * Name of the block as defined in the source
     */
    protected final String blockName;

    public BlockBuffer(Buffer buffer, String blockName, VariableBlock variableBlock) {
        this.blockName = blockName;
        this.variableBlock = variableBlock;
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

    /**
     * Returns the underlying buffer as an un-typed Buffer
     * 
     * @return
     */
    public Buffer getBuffer() {
        return plainBuffer;
    }

    /**
     * Returns the variable block that this buffer belongs to
     * 
     * @return
     */
    public VariableBlock getVariableBlock() {
        return variableBlock;
    }

}
