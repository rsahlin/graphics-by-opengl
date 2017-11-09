package com.nucleus.shader;

import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * Data for an active shader variable, this can be either attribute or uniform variables.
 * 
 * @author Richard Sahlin
 *
 */
public class ShaderVariable {

    private final static String ILLEGAL_DATATYPE_ERROR = "Illegal datatype: ";

    /**
     * The different types of active variables - either uniform or attribute.
     * 
     * @author Richard Sahlin
     *
     */
    public enum VariableType {

        UNIFORM(),
        ATTRIBUTE();

        private VariableType() {
        }
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
     * The gl location of the variable
     */
    private int location;
    /**
     * Offset into buffer where the data for this varible is stored, used by GL
     */
    private int offset;

    /**
     * Creates a new ActiveVariable from the specified data.
     * This constructor can be used with the data from GLES
     * 
     * @param type Type of shader variable
     * @param name byte array containing the name
     * @param data Array holding size and type for variable, typically fetched from GL
     * @param nameLengthOffset Offset into array where length of name is
     * @param sizeOffset Offset into array where size of variable is
     * @param typeOffset Offset into array where type of variable is
     * @throws ArrayIndexOutOfBoundsException If sizeOffset or typeOffset is larger than data length, or negative.
     */
    ShaderVariable(VariableType type, byte[] name, int[] data, int nameLengthOffset, int sizeOffset,
            int typeOffset) {
        this.type = type;
        this.name = new String(name, 0, data[nameLengthOffset]);
        size = data[sizeOffset];
        dataType = data[typeOffset];
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
}
