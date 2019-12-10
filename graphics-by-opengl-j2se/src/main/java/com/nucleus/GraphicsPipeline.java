package com.nucleus;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.ShaderBinary;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableIndexer;

/**
 * Instance of a programmable graphics pipeline - this holds the program states and immutable properties
 * for a graphics pipeline.
 * This does NOT hold any buffers for uniform/attribute data - that is held in {@link GraphicsShader}
 * TODO - remove references to opengl {@link NamedShaderVariable}
 *
 */
public interface GraphicsPipeline<S extends ShaderBinary> extends Pipeline<GraphicsShader, S> {

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
     * Internal method
     * TODO - Should this be a seperate interface that handles pipeline/program creation?
     * 
     * Compile and link the shaders needed to produce the program that can be used.
     * 
     * @param renderer
     * @param sources The shader sources to use
     * @param shader The shader program the sources shall be used with
     * @throws BackendException If program could not be compiled and linked
     */
    public void createProgram(NucleusRenderer renderer, S[] sources, GraphicsShader shader) throws BackendException;

    /**
     * Uploads one of more float variables to the render API, supports VEC2, VEC3, VEC4 and MAT2, MAT3,
     * MAT4 types
     * 
     * @param data containing data to be uploaded
     * @param variable Shader variable to set data for, datatype and size is read. If null then nothing is done
     */
    public void uploadVariable(FloatBuffer data, ShaderVariable variable);

    /**
     * Uploads the specified uniforms to render API, float array data is uploaded.
     * 
     * 
     * @param uniformData The uniform source data
     * @param activeUniform The active uniforms to upload, null to set all active uniforms.
     * @throws BackendException
     */
    public void uploadUniforms(FloatBuffer uniformData, ShaderVariable[] activeUniform) throws BackendException;

    /**
     * Uploads the specified attributes to render API, float array data is uploaded.
     * 
     * @param attributeData The attribute source data
     * @param activeAttributes The active attributes to upload, null to set all active uniforms
     */
    public void uploadAttributes(FloatBuffer attributeData, ShaderVariable[] activeAttributes);

    /**
     * Returns the size of all the shader variables of the specified type, either ATTRIBUTE or UNIFORM
     * EXCLUDING the size of Sampler2D variables.
     * For uniforms this corresponds to the total size buffer size needed - the size of Sampler2D variables.
     * For attributes this corresponds to the total buffer size needed, normally attribute data is put in
     * dynamic and static buffers.
     * 
     * @param type
     * @return Total size, in floats, of all defined shader variables of the specified type
     */
    public int getVariableSize(VariableType type);

    /**
     * Returns the active (used) shader variables of the specified type
     * 
     * @param type
     * @return
     */
    public ShaderVariable[] getActiveVariables(VariableType type);

    /**
     * Returns an array of compiled uniform blocks, or null if none declared in the shader
     * 
     * @return
     */
    public BlockBuffer[] getUniformBlocks();

    /**
     * Returns the graphics shader location mapping
     * 
     * @return
     */
    public VariableIndexer getLocationMapping();

}
