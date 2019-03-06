package com.nucleus.shader;

import java.nio.IntBuffer;

import com.nucleus.common.Constants;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;

/**
 * Data for an active shader variable, ie varible declared and used in a compiled shader.
 * This can be attribute, uniform or block variables.
 * 
 * @author Richard Sahlin
 *
 */
public class ShaderVariable {

    private final static String ILLEGAL_DATATYPE_ERROR = "Illegal datatype: ";

    /**
     * Offset to size
     */
    public final static int SIZE_OFFSET = 0;
    /**
     * Offset to type
     */
    public final static int TYPE_OFFSET = 1;

    public static final int NAME_LENGTH_OFFSET = 2;

    /**
     * Offset to index of active (location) variable
     */
    public final static int ACTIVE_INDEX_OFFSET = 3;
    /**
     * Offset to variable data offset
     */
    public final static int DATA_OFFSET = 4;
    /**
     * Offset to block index
     */
    public final static int BLOCK_INDEX_OFFSET = 5;

    /**
     * The different types of active variables - attribute, uniform or uniform block
     * 
     * @author Richard Sahlin
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

        protected final int activeCount;
        protected final int[] indices;
        public final int blockIndex;
        public final int program;
        /**
         * Number of bytes needed for the block data.
         */
        protected final int blockDataSize;
        protected final String name;
        protected final Usage usage;

    }

    /**
     * Returns the name of this active attribute or uniform variable.
     * 
     * @return
     */
    public String getName() {
        return name;
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
    private VariableType type;
    private String name;
    private int size;
    private int dataType;
    /**
     * The gl location of the variable, used when calling setting attrib pointer or uploading uniforms.
     */
    private int location;
    /**
     * The gl index of this active variable
     */
    private int activeIndex;
    /**
     * Offset into buffer where the data for this variable is stored, used by GL when setting attrib pointer
     * or uploading uniforms.
     */
    private int offset = Constants.NO_VALUE;

    /**
     * If this variable belongs to a block
     */
    private int blockIndex = Constants.NO_VALUE;

    /**
     * Creates a new ActiveVariable from the specified data.
     * This constructor can be used with the data from GLES
     * 
     * @param type Type of shader variable
     * @param name Name of variable excluding [] and . chars.
     * @param data Array holding data at {@value #SIZE_OFFSET}, {@value #TYPE_OFFSET}, {@value #INDEX_OFFSET},
     * {@value #DATA_OFFSET} {@value #BLOCK_INDEX_OFFSET}
     * @param startIndex Start of data.
     * @throws ArrayIndexOutOfBoundsException If sizeOffset or typeOffset is larger than data length, or negative.
     */
    public ShaderVariable(VariableType type, String name, int[] data, int startIndex) {
        this.type = type;
        this.name = name;
        size = data[startIndex + SIZE_OFFSET];
        dataType = data[startIndex + TYPE_OFFSET];
        activeIndex = data[startIndex + ACTIVE_INDEX_OFFSET];
        this.offset = data.length > startIndex + DATA_OFFSET ? data[startIndex + DATA_OFFSET] : this.offset;
        this.blockIndex = data.length > startIndex + BLOCK_INDEX_OFFSET ? data[startIndex + BLOCK_INDEX_OFFSET]
                : this.blockIndex;
    }

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
     * Returns the active index of this shader variable
     * 
     * @return The index of this variable as an active variable
     */
    public int getActiveIndex() {
        return activeIndex;
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
    public int getSizeInFloats() {
        switch (dataType) {
            case GLES20.GL_FLOAT:
                return size;
            case GLES20.GL_FLOAT_VEC2:
                return 2 * size;
            case GLES20.GL_FLOAT_VEC3:
                return 3 * size;
            case GLES20.GL_FLOAT_VEC4:
                return 4 * size;
            case GLES20.GL_FLOAT_MAT2:
                return 4 * size;
            case GLES20.GL_FLOAT_MAT3:
                return 9 * size;
            case GLES20.GL_FLOAT_MAT4:
                return 16 * size;
            case GLES20.GL_SAMPLER_2D:
                return size;
            case GLES30.GL_SAMPLER_2D_SHADOW:
                return size;
        }
        throw new IllegalArgumentException(ILLEGAL_DATATYPE_ERROR + dataType);
    }

    /**
     * Utility method that returns the total number of bytes that this variable occupies.
     * This is needed when allocating the client (Java) side of the variable.
     * 
     * @return Number of bytes that this variable needs
     */
    public int getSizeInBytes() {
        switch (dataType) {
            case GLES20.GL_FLOAT:
                return size * 4;
            case GLES20.GL_FLOAT_VEC2:
                return 2 * size * 4;
            case GLES20.GL_FLOAT_VEC3:
                return 3 * size * 4;
            case GLES20.GL_FLOAT_VEC4:
                return 4 * size * 4;
            case GLES20.GL_FLOAT_MAT2:
                return 4 * size * 4;
            case GLES20.GL_FLOAT_MAT3:
                return 9 * size * 4;
            case GLES20.GL_FLOAT_MAT4:
                return 16 * size * 4;
            case GLES20.GL_SAMPLER_2D:
                return size * 4;
            case GLES30.GL_SAMPLER_2D_SHADOW:
                return size * 4;
        }
        throw new IllegalArgumentException(ILLEGAL_DATATYPE_ERROR + dataType);
    }

    public int getComponentCount() {
        switch (dataType) {
            case GLES20.GL_FLOAT:
                return 1;
            case GLES20.GL_FLOAT_VEC2:
                return 2;
            case GLES20.GL_FLOAT_VEC3:
                return 3;
            case GLES20.GL_FLOAT_VEC4:
                return 4;
            case GLES20.GL_FLOAT_MAT2:
                return 2;
            case GLES20.GL_FLOAT_MAT3:
                return 3;
            case GLES20.GL_FLOAT_MAT4:
                return 4;
        }
        throw new IllegalArgumentException(ILLEGAL_DATATYPE_ERROR + dataType);
    }

    @Override
    public String toString() {
        return name + " : " + type + ", size " + size + ", sizeinbytes: " + getSizeInBytes() + ", offset " + offset
                + ", activeIndex " + activeIndex + ", blockIndex " + blockIndex;
    }

}
