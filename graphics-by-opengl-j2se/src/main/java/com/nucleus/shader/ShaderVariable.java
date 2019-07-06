package com.nucleus.shader;

import java.nio.IntBuffer;

import com.nucleus.common.Constants;

/**
 * Data for an active shader variable, ie variable declared and used in a compiled shader.
 * This can be attribute, uniform or block variables.
 * This shader variable can NOT have a name
 * 
 *
 */
public abstract class ShaderVariable {

    /**
     * The different types of active variables - attribute, uniform or uniform block
     * 
     */
    public enum VariableType {

    UNIFORM(0),
    ATTRIBUTE(1),
    UNIFORM_BLOCK(2);

        public final int index;

        private VariableType(int index) {
            this.index = index;
        }
    }

    /**
     * Containing information about a interface block
     *
     */
    public static class InterfaceBlock {

        public static final int ACTIVE_COUNT_INDEX = 0;
        /**
         * GL_UNIFORM_BLOCK_DATA_SIZE is stored at this index - this is the number of bytes needed for interface block.
         */
        public static final int BLOCK_DATA_SIZE_INDEX = 1;
        public static final int VERTEX_REFERENCE_INDEX = 2;
        public static final int FRAGMENT_REFERENCE_INDEX = 3;

        /**
         * Where the block variables are used
         */
        public enum Usage {
            /**
             * Variables used only in vertex shader
             */
            VERTEX_SHADER(),
            /**
             * Variables used only in fragment shader
             */
            FRAGMENT_SHADER(),
            /**
             * Variables used in vertex and fragment shader
             */
            VERTEX_FRAGMENT_SHADER();
        }

        /**
         * Creates a new interface block for the specified program and block index.
         * blockInfo shall contain: active variables, block data size
         * 
         * @param program
         * @param blockIndex
         * @param blockName
         * @param blockInfo active variable count, block data size is read here
         * @param indices
         */
        public InterfaceBlock(int program, int blockIndex, String blockName, IntBuffer blockInfo, IntBuffer indices) {
            this.program = program;
            this.blockIndex = blockIndex;
            this.activeCount = blockInfo.get(ACTIVE_COUNT_INDEX);
            this.indices = new int[indices.capacity()];
            this.name = blockName;
            this.blockDataSize = blockInfo.get(BLOCK_DATA_SIZE_INDEX);
            indices.rewind();
            indices.get(this.indices);
            int value = blockInfo.get(VERTEX_REFERENCE_INDEX) + (blockInfo.get(FRAGMENT_REFERENCE_INDEX) << 1);
            usage = value == 3 ? Usage.VERTEX_FRAGMENT_SHADER
                    : value == 1 ? Usage.VERTEX_SHADER : Usage.FRAGMENT_SHADER;
        }

        public final int activeCount;
        public final int[] indices;
        public final int blockIndex;
        public final int program;
        /**
         * Number of bytes needed for the block data.
         */
        public final int blockDataSize;
        public final String name;
        public final Usage usage;

    }

    /**
     * The size of the variable, this will be 1 for all variables that are not arrays - if it is an array the size
     * will be the length of the array.
     * 
     * @return (Array) size of the variable, 1 or the length if the variable is an array.
     */
    public int getSize() {
        return size;
    }

    /**
     * The data type of the variable, eg GL_FLOAT_VEC4
     * 
     * @return GLES constant for the type.
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * Type of variable - either attribute or uniform.
     */
    protected VariableType type;
    protected int size;
    protected int dataType;
    /**
     * The ocation of the variable, used when calling setting attrib pointer or uploading uniforms.
     */
    protected int location;
    /**
     * Offset into buffer where the data for this variable is stored, used by GL when setting attrib pointer
     * or uploading uniforms.
     */
    protected int offset = Constants.NO_VALUE;

    /**
     * If this variable belongs to a block
     */
    protected int blockIndex = Constants.NO_VALUE;

    /**
     * Returns the type of shader variable, ATTRIBUTE or UNIFORM
     * 
     * @return
     */
    public VariableType getType() {
        return type;
    }

    /**
     * If this variable belongs to a variable block, this contains the block index - if not this is -1
     * 
     * @return Block index, or -1 if not variable in a block.
     */
    public int getBlockIndex() {
        return blockIndex;
    }

    /**
     * Sets the location of the shader variable, set this to the value from glGetAttribLocation or glGetUniformLocation
     * 
     * @param location The location of this shader variable
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * Returns the location of this shader variable, this is used when setting pointers to GL.
     * 
     * @return The location of this shader variable
     */
    public int getLocation() {
        return location;
    }

    /**
     * Sets the offset into buffer where the data for this variable is.
     * 
     * @param offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the offset into buffer where the data for this variable is.
     * 
     * @return
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Utility method that returns the total number of floats that this variable occupies.
     * This is needed when allocating the client (Java) side of the variable.
     * 
     * @return Number of floats that this variable needs (size * datatype in floats)
     */
    public abstract int getSizeInFloats();

    /**
     * Utility method that returns the total number of bytes that this variable occupies.
     * This is needed when allocating the client (Java) side of the variable.
     * 
     * @return Number of bytes that this variable needs
     */
    public abstract int getSizeInBytes();

    /**
     * Returns the number of components, ie 3 for a VEC3 and 4 for a MAT4. Used when setting attrib data
     * 
     * @return number of components in this shader variable
     */
    public abstract int getComponentCount();

    @Override
    public String toString() {
        return type + ", size " + size + ", sizeinbytes: " + getSizeInBytes() + ", offset " + offset
                + ", blockIndex " + blockIndex;
    }

}
