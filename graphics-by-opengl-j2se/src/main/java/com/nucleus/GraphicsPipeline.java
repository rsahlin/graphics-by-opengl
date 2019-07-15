package com.nucleus;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.VariableIndexer;

/**
 * Instance of a programmable graphics pipeline
 * TODO - remove references to opengl
 *
 */
public interface GraphicsPipeline extends Pipeline<GraphicsShader> {

    /**
     * Sets the vertexAttribPointers for the glTF primitive
     * 
     * @param attribs
     * @param accessors
     */
    public void glVertexAttribPointer(ArrayList<Attributes> attribs,
            ArrayList<Accessor> accessors) throws BackendException;

    /**
     * Returns an array with number of attributes used per vertex, for each attribute buffer that is used by this
     * program.
     * This is the minimal storage that this shader needs per vertex.
     * 
     * @return
     */
    public int[] getAttributeSizes();

    /**
     * Returns the active shader uniform by name, or null if not found
     * 
     * @param uniform Name of uniform to return
     * @return The shader variable for the uniform, or null if not found
     */
    public NamedShaderVariable getUniformByName(String uniform);

    /**
     * Returns the attribute if defined in shader program.
     * 
     * @param attribute
     * @return Shader variable for attribute, or null if not defined in shader
     */
    public NamedShaderVariable getAttributeByName(String attribute);

    /**
     * Sets the float values from data at the offset from variable, use this to set more than one value.
     * 
     * @param variable The shader variable to set uniform data to
     * @param data The uniform data to set
     * @param sourceOffset Offset into data where values are read
     */
    public void setUniformData(NamedShaderVariable variable, float[] data, int sourceOffset);

    /**
     * Returns the uniform data, this shall be mapped to shader
     * 
     * @return The floatbuffer holding uniform data
     */
    public FloatBuffer getUniformData();

    /**
     * 
     * Returns the location mapping of the shader program being used
     * 
     * @return
     */
    public VariableIndexer getLocationMapping();

}
