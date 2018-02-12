package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * This interface has the uniform and attribute mapping for a shader program, this is used to find the program specific
 * index and offset of a variable.
 * The offset is used when setting the attribute pointers to GL, ie it is the offset where data for the variable is
 * located in attribute/uniform data.
 * The index is the variable location in the shader program, ie in the array holding active variables.
 * 
 * @author Richard Sahlin
 *
 */
public interface VariableMapping {

    /**
     * Returns the index position of the attribute, this is the position of the variable in array storage if the
     * variable is used.
     * If variable is NOT used in the shader this method MUST return -1
     * 
     * @return Index of variable or -1 if not used in shader.
     */
    public int getIndex();

    /**
     * Sets the index of this variable in array store - this shall only be done once.
     * 
     * @param index
     */
    public void setIndex(int index);

    /**
     * Returns the (static) offset where for data of the variable
     * 
     * @return
     */
    public int getOffset();

    /**
     * Returns the type of variable
     * 
     * @return Type of variable
     */
    public VariableType getType();

    /**
     * Returns the buffer index in the mesh.
     * This value can be used to call {@link Mesh#getVerticeBuffer(BufferIndex)}
     * 
     * @param BufferIndex Index to buffer holding variables
     */
    public BufferIndex getBufferIndex();

    /**
     * Returns the name of the variable - this is the name as defined it shall be defined in shader program.
     * 
     * @return
     */
    public String getName();

}
