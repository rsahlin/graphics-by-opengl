package com.nucleus.geometry;

import com.nucleus.texturing.Texture2D;

/**
 * This is the smallest renderable self contained unit, it has surface (vertice and triangle) information, attribute
 * data and material.
 * 
 * @author Richard Sahlin
 *
 */
public class Mesh {

    private final static String NULL_PARAMETER_STR = "Null parameter";

    /**
     * One or more generic attribute arrays, read by the program specified in the material.
     */
    protected VertexBuffer[] attributes;
    protected ElementBuffer indices;
    protected Material material;
    /**
     * Currently only supports single texture
     */
    protected Texture2D[] texture = new Texture2D[1];

    /**
     * Uniform vectors, used when rendering this Mesh depending on what ShaderProgram is used.
     */
    protected float[] uniformVectors;

    /**
     * Creates a new Mesh that can be rendered using drawElements()
     * 
     * @param indices Buffer with element data for vertices to be drawn.
     * @param vertices One or more buffers with vertice/attribute data
     * @param material The material to use when rendering this mesh.
     * @param texture Texture to set or null
     * @throws IllegalArgumentException If vertices, indices or material is null.
     */
    public Mesh(ElementBuffer indices, VertexBuffer[] vertices, Material material, Texture2D texture) {
        init(indices, vertices, material, texture);
    }

    /**
     * Creates a new Mesh that can be rendered using drawArrays()
     * 
     * @param vertices One or more buffers with vertice/attribute data
     * @param material The material to use when rendering this mesh.
     * @param texture Texture to set or null
     * @throws IllegalArgumentException If vertices or material is null.
     */
    public Mesh(VertexBuffer[] vertices, Material material, Texture2D texture) {
        init(vertices, material, texture);
    }

    /**
     * Internal method to init this class.
     * 
     * @param indices Buffer with element data for vertices to be drawn.
     * @param vertices One or more buffers with vertice/attribute data
     * @param material The material to use when rendering this mesh.
     * @param texture Texture to set or null
     * @throws IllegalArgumentException If vertices or material is null.
     */
    private void init(ElementBuffer indices, VertexBuffer[] vertices, Material material, Texture2D texture) {
        if (indices == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_STR);
        }
        this.indices = indices;
        init(vertices, material, texture);
    }

    /**
     * Internal method to init this class.
     * 
     * @param vertices One or more buffers with vertice/attribute data
     * @param material The material to use when rendering this mesh.
     * @param texture Texture to set or null
     * @throws IllegalArgumentException If vertices or material is null.
     */
    private void init(VertexBuffer[] vertices, Material material, Texture2D texture) {
        if (vertices == null || material == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_STR);
        }
        this.attributes = vertices;
        this.material = material;
        if (texture != null) {
            this.texture[Texture2D.TEXTURE_0] = texture;
        }
    }

    /**
     * Returns the buffer, at the specified index, containing vertices and attribute data
     * 
     * @param index Index into the vertex/attribute buffer to return
     * @return The vertexbuffer
     */
    public VertexBuffer getVerticeBuffer(int index) {
        return attributes[index];
    }

    /**
     * Returns the optoional element buffer, this is used when drawing using indexed vertices
     * 
     * @return
     */
    public ElementBuffer getElementBuffer() {
        return indices;
    }

    /**
     * Returns the material
     * 
     * @return
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the texture
     * 
     * @param index Active texture to return, 0 for the first texture.
     * @return
     */
    public Texture2D getTexture(int index) {
        return (texture[index]);
    }

    /**
     * Returns one or more defined uniform vectors used when rendering.
     * 
     * @return One or more uniform vector as used by the shader program implementation
     */
    public float[] getUniformVectors() {
        return uniformVectors;
    }

    /**
     * Sets a reference to an array with float values that can be used by when rendering this Mesh.
     * Note that the use of uniforms is depending on the shader program used.
     * 
     * @param uniformVectors Values to reference in this class, note that values are NOT copied.
     * 
     */
    public void setUniformVectors(float[] uniformVectors) {
        this.uniformVectors = uniformVectors;
    }

}
