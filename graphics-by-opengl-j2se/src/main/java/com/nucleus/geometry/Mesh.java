package com.nucleus.geometry;

import com.nucleus.io.BaseReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.texturing.Texture2D;

/**
 * This is the smallest renderable self contained unit, it has surface (vertice and triangle) information, attribute
 * data and material.
 * One mesh shall be possible to render with no more than one drawcall.
 * Before rendering is done the generic attribute buffers must be set/updated.
 * 
 * @author Richard Sahlin
 *
 */
public class Mesh extends BaseReference {

    /**
     * For the different Vertice/Attribute buffers
     */
    public enum BufferIndex {
        /**
         * Vertex storage buffer
         */
        VERTICES(0),
        /**
         * Attribute buffer storage
         */
        ATTRIBUTES(1);

        public final int index;

        private BufferIndex(int index) {
            this.index = index;
        }

    }

    public final static int MAX_TEXTURE_COUNT = 1;
    private final static String NULL_PARAMETER_STR = "Null parameter";
    /**
     * Set the BLEND_EQUATION_RGB index in blendModes to this value to turn off alpha.
     */
    public final static int NO_ALPHA = -1;

    /**
     * Index to the RGB blend equation, set this to 0 to turn off alpha for a mesh
     */
    public final static int BLEND_EQUATION_RGB = 0;
    /**
     * Index to the alpha blend equation
     */
    public final static int BLEND_EQUATION_ALPHA = 1;
    /**
     * Index to the source RGB blend function
     */
    public final static int SOURCE_RGB = 2;
    /**
     * Index to the destination RGB blend function
     */
    public final static int DESTINATION_RGB = 3;
    /**
     * Index to the source Alpha blend function
     */
    public final static int SOURCE_ALPHA = 4;
    /**
     * Index to the destination Alpha blend function
     */
    public final static int DESTINATION_ALPHA = 5;

    /**
     * One or more generic attribute arrays, read by the program specified in the material.
     */
    protected VertexBuffer[] attributes;
    protected ElementBuffer indices;
    protected Material material;
    /**
     * Currently only supports single texture
     */
    protected Texture2D[] texture = new Texture2D[MAX_TEXTURE_COUNT];

    /**
     * Array with values for blend equation separate (blend equation RGB, blend equation Alpha, src RGB, dst RGB, src
     * Alpha, dst Alpha
     */
    protected final int[] blendModes = new int[] { GLES20.GL_FUNC_ADD, GLES20.GL_FUNC_ADD,
            GLES20.GL_SRC_ALPHA,
            GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA };

    /**
     * Uniform vectors, used when rendering this Mesh depending on what ShaderProgram is used.
     */
    protected float[] uniformVectors;
    /**
     * Uniform matrices, used when rendering this Mesh depending on what ShaderProgram is used.
     */
    protected float[] uniformMatrices;

    /**
     * Optional updater for attributes, use this when dynamic mesh is needed. ie when the generic attribute data must be
     * updated each frame.
     * TODO Maybe move this to the node?
     */
    protected AttributeUpdater attributeUpdater;

    /**
     * Creates a new empty mesh, the attribute/index buffers must be prepared before rendering can take place.
     */
    public Mesh() {
        super();
    }

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
        setupIndexed(indices, vertices, material, texture);
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
        setupVertices(vertices, material, texture);
    }

    /**
     * Setup the buffers needed for indexed (elements) rendering using glDrawElements()
     * 
     * @param indices Buffer with element data for vertices to be drawn.
     * @param vertices One or more buffers with vertice/attribute data
     * @param material The material to use when rendering this mesh.
     * @param texture Texture to set or null
     * @throws IllegalArgumentException If vertices or material is null.
     */
    public void setupIndexed(ElementBuffer indices, VertexBuffer[] vertices, Material material, Texture2D texture) {
        if (indices == null) {
            throw new IllegalArgumentException(NULL_PARAMETER_STR);
        }
        this.indices = indices;
        setupVertices(vertices, material, texture);
    }

    /**
     * Setup the buffers needed for drawing using only the vertices (no indexed buffer)
     * 
     * @param vertices One or more buffers with vertice/attribute data
     * @param material The material to use when rendering this mesh.
     * @param texture Texture to set or null
     * @throws IllegalArgumentException If vertices or material is null.
     */
    public void setupVertices(VertexBuffer[] vertices, Material material, Texture2D texture) {
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
     * Sets the texture into this mesh as the specified texture index.
     * The texture object must have a valid texture name, the texture will be active when the mesh is rendered.
     * 
     * @param texture
     * @param index
     */
    public void setTexture(Texture2D texture, int index) {
        this.texture[index] = texture;
    }

    /**
     * Returns the buffer, at the specified index, containing vertices and attribute data
     * 
     * @param buffer Index into the vertex/attribute buffer to return
     * @return The vertexbuffer
     */
    public VertexBuffer getVerticeBuffer(BufferIndex buffer) {
        return attributes[buffer.index];
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

    /**
     * Returns one or more defined uniform matrices used when rendering.
     * 
     * @return
     */
    public float[] getUniformMatrices() {
        return uniformMatrices;
    }

    /**
     * Sets a reference to an array with float values that can be used by when rendering this Mesh.
     * Note that the use of uniforms is depending on the shader program used.
     * 
     * @param uniformMatrices Values to reference in this class, note that values are NOT copied.
     * 
     */
    public void setUniformMatrices(float[] uniformMatrices) {
        this.uniformMatrices = uniformMatrices;
    }

    /**
     * Returns the separate blend modes:
     * blend equation RGB, blend equation Alpha, source RGB, destination RGB, source Alpha, destination Alpha
     * Use BLEND_EQUATION_RGB, BLEND_EQUATION_ALPHA, SOURCE_RGB, SOURCE_ALPHA, DESTINATION_RGB, DESTINATION_ALPHA to
     * access the values.
     * 
     * @return Array with values for separate alpha blend equation and function
     */
    public int[] getBlendModes() {
        return blendModes;
    }

    /**
     * Sets the separate blend equation/function for this mesh.
     * 
     * @param gles
     */
    public void setBlendModeSeparate(GLES20Wrapper gles) {
        if (blendModes[BLEND_EQUATION_RGB] == NO_ALPHA) {
            gles.glDisable(GLES20.GL_BLEND);
        } else {
            gles.glEnable(GLES20.GL_BLEND);
            gles.glBlendEquationSeparate(blendModes[BLEND_EQUATION_RGB], blendModes[BLEND_EQUATION_ALPHA]);
            gles.glBlendFuncSeparate(blendModes[SOURCE_RGB], blendModes[DESTINATION_RGB], blendModes[SOURCE_ALPHA],
                    blendModes[SOURCE_RGB]);
        }
    }

    /**
     * Sets the attribute updater for this mesh, use this for meshes where the attribute data must be updated each
     * frame.
     * This method shall copy data, as needed, into the VertexBuffer arrays that are used when the mesh is rendered.
     * What data to copy is implementation specific.
     * 
     * @param attributeUpdater Callback to set data into the generic vertex arrays, or null to remove.
     */
    public void setAttributeUpdater(AttributeUpdater attributeUpdater) {
        this.attributeUpdater = attributeUpdater;
    }

    /**
     * Returns the attribute updater.
     * 
     * @return The attribute updater or null if none is set.
     */
    public AttributeUpdater getAttributeUpdater() {
        return attributeUpdater;
    }

}
