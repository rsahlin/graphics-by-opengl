package com.nucleus.opengl.shader;

import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.shader.ShaderVariable;

/**
 * Data for an active named shader variable, ie variable declared and used in a compiled shader.
 * This can be attribute, uniform or block variables.
 * 
 */
public class NamedShaderVariable extends ShaderVariable {

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
     * Offset to variable data offset
     */
    public final static int DATA_OFFSET = 3;
    /**
     * Offset to block index
     */
    public final static int BLOCK_INDEX_OFFSET = 4;

    private String name;
    /**
     * The gl index of this active variable
     */
    private int activeIndex;

    /**
     * Creates a new ActiveVariable from the specified data.
     * This constructor can be used with the data from GLES
     * 
     * @param type Type of shader variable
     * @param name Name of variable excluding [] and . chars.
     * @param index Index of current active variable
     * @param data Array holding data at {@value #SIZE_OFFSET}, {@value #TYPE_OFFSET},
     * {@value #DATA_OFFSET} {@value #BLOCK_INDEX_OFFSET}
     * @param startIndex Start of data.
     * @throws ArrayIndexOutOfBoundsException If sizeOffset or typeOffset is larger than data length, or negative.
     */
    public NamedShaderVariable(VariableType type, String name, int index, int[] data, int startIndex) {
        this.type = type;
        this.name = name;
        size = data[startIndex + SIZE_OFFSET];
        dataType = data[startIndex + TYPE_OFFSET];
        activeIndex = index;
        this.offset = data.length > startIndex + DATA_OFFSET ? data[startIndex + DATA_OFFSET] : this.offset;
        this.blockIndex = data.length > startIndex + BLOCK_INDEX_OFFSET ? data[startIndex + BLOCK_INDEX_OFFSET]
                : this.blockIndex;
    }

    /**
     * Returns the active index of this shader variable, this will be 0 - number of active variables.
     * 
     * @return The index of this variable as an active variable
     */
    public int getActiveIndex() {
        return activeIndex;
    }

    /**
     * Returns the name of this shader variable
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
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

    @Override
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

    @Override
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
        return name + " : " + super.toString();
    }

}
