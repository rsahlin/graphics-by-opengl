package com.nucleus.shader;

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
     * Returns the index position of the attribute
     * 
     * @return
     */
    public int getIndex();

    /**
     * The offset into attribute/uniform data where the data for the variable is, this value is used by GL
     * 
     * @return Variable data offset, used by GL when setting attribute/uniform data.
     */
    public int getOffset();

}
