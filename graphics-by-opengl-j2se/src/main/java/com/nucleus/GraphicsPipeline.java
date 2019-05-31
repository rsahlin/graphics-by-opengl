package com.nucleus;

import java.util.ArrayList;

import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.ShaderVariable;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Primitive.Attributes;

/**
 * Instance of a programmable graphics pipeline
 *
 */
public abstract class GraphicsPipeline extends Pipeline {

    /**
     * Sets the vertexAttribPointers for the glTF primitive
     * 
     * @param attribs
     * @param accessors
     */
    public abstract void glVertexAttribPointer(ArrayList<Attributes> attribs,
            ArrayList<Accessor> accessors) throws BackendException;

    /**
     * Returns an array with number of attributes used per vertex, for each attribute buffer that is used by this
     * program.
     * This is the minimal storage that this shader needs per vertex.
     * 
     * @return
     */
    public abstract int[] getAttributeSizes();

    /**
     * Returns the active shader uniform by name, or null if not found
     * 
     * @param uniform Name of uniform to return
     * @return The shader variable for the uniform, or null if not found
     */
    public abstract ShaderVariable getUniformByName(String uniform);

    /**
     * Returns the attribute if defined in shader program.
     * 
     * @param attribute
     * @return Shader variable for attribute, or null if not defined in shader
     */
    public abstract ShaderVariable getAttributeByName(String attribute);

    /**
     * Returns the number of attributes per vertex that are used by the program.
     * 
     * @param buffer The buffer to get attributes per vertex for
     * @return Number of attributes as used by this program, per vertex 0 or -1 if not defined.
     */
    @Deprecated
    public abstract int getAttributesPerVertex(BufferIndex buffer);

}
