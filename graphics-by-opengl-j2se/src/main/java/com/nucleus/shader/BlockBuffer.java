package com.nucleus.shader;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.nucleus.geometry.BufferObject;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;

/**
 * Storage for a variable block, for instance a uniform block
 * The underlying buffer shall be a java.nio buffer
 *
 */
public abstract class BlockBuffer extends BufferObject {

    protected final ByteBuffer plainBuffer;
    /**
     * The block this buffer belongs to or null
     */
    protected final InterfaceBlock interfaceBlock;

    /**
     * Name of the block as defined in the source
     */
    protected final String blockName;

    public BlockBuffer(ByteBuffer buffer, String blockName) {
        super(buffer.capacity());
        this.blockName = blockName;
        this.plainBuffer = buffer;
        this.interfaceBlock = null;

    }

    /**
     * 
     * @param buffer
     * @param blockName
     * @param interfaceBlock The block this buffer belongs to
     * @throws NullPointerException If interfaceBlock is null
     */
    public BlockBuffer(ByteBuffer buffer, String blockName, InterfaceBlock interfaceBlock) {
        super(interfaceBlock.blockDataSize);
        this.blockName = blockName;
        this.interfaceBlock = interfaceBlock;
        this.plainBuffer = buffer;
    }

    /**
     * Returns the capacity of the buffer, in number of bytes
     * 
     * @return Number of bytes capacity
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
     * Returns the interface block that this buffer belongs to
     * 
     * @return
     */
    public InterfaceBlock getInterfaceBlock() {
        return interfaceBlock;
    }

    /**
     * Creates the block buffers, if any are used. Creates instance of {@link FloatBlockBuffer}
     * 
     * @param sizes Number of buffers and size of each, in byte units
     * @return Variable (float) block buffers for this program, or null if not used.
     */
    public static BlockBuffer[] createBlockBuffers(InterfaceBlock[] interfaceBlocks) {
        BlockBuffer[] blockBuffers = null;
        if (interfaceBlocks != null) {
            blockBuffers = new BlockBuffer[interfaceBlocks.length];
            for (int index = 0; index < interfaceBlocks.length; index++) {
                // TODO - need to add stride
                blockBuffers[index] = new FloatBlockBuffer(interfaceBlocks[index],
                        interfaceBlocks[index].blockDataSize >>> 2);
            }
        }
        return blockBuffers;
    }

}
