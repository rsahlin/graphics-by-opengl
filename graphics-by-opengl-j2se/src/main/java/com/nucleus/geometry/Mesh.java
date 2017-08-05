package com.nucleus.geometry;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.bounds.Bounds;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;

/**
 * This is the smallest renderable self contained unit, it has surface (vertice and triangle) information, attribute
 * data and material.
 * One mesh shall be possible to render with no more than one drawcall.
 * Before rendering is done the generic attribute buffers must be set/updated.
 * 
 * @author Richard Sahlin
 *
 */
public class Mesh extends BaseReference implements AttributeUpdater {

    public enum Mode {
        /**
         * From GL_POINTS
         */
        POINTS(GLES20.GL_POINTS),
        /**
         * From GL_LINE_STRIP
         */
        LINE_STRIP(GLES20.GL_LINE_STRIP),
        /**
         * From GL_LINE_LOOP
         */
        LINE_LOOP(GLES20.GL_LINE_LOOP),
        /**
         * From GL_TRIANGLE_STRIP
         */
        TRIANGLE_STRIP(GLES20.GL_TRIANGLE_STRIP),
        /**
         * From GL_TRIANGLE_FAN
         */
        TRIANGLE_FAN(GLES20.GL_TRIANGLE_FAN),
        /**
         * From GL_TRIANGLES
         */
        TRIANGLES(GLES20.GL_TRIANGLES),
        /**
         * From GL_LINES
         */
        LINES(GLES20.GL_LINES);

        public final int mode;

        private Mode(int mode) {
            this.mode = mode;
        }
    }

    private final static String NULL_NAMES = "Buffer names is null";
    private final static String NOT_ENOUGH_NAMES = "Not enough buffer names";

    /**
     * For the different Vertice/Attribute buffers
     */
    public enum BufferIndex {
        /**
         * Vertex storage buffer, this is usually not updated
         */
        VERTICES(0),
        /**
         * Attribute buffer storage, this is usually dynamic
         */
        ATTRIBUTES(1),
        /**
         * Static attributes
         */
        ATTRIBUTES_STATIC(2);

        public final int index;

        private BufferIndex(int index) {
            this.index = index;
        }

    }

    public static class Builder {

        protected NucleusRenderer renderer;
        protected Texture2D texture;
        protected Material material;
        protected int vertexCount = -1;
        protected int indiceCount = 0;
        protected ElementBuffer.Type indiceBufferType = Type.SHORT;
        protected Mode mode;
        protected ShapeBuilder shapeBuilder;

        /**
         * Creates a new builder
         * 
         * @param renderer
         * @throws IllegalArgumentException If renderer is null
         */
        public Builder(NucleusRenderer renderer) {
            if (renderer == null) {
                throw new IllegalArgumentException("Renderer may not be null");
            }
            this.renderer = renderer;
        }

        /**
         * Sets the drawmode for the mesh
         * 
         * @param mode
         * @return
         */
        public Builder setMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the texture to use for the created mesh
         * 
         * @param texture
         * @return
         */
        public Builder setTexture(Texture2D texture) {
            this.texture = texture;
            return this;
        }

        /**
         * Fetches the texture and stores as texture to be used when creating mesh
         * 
         * @param textureRef
         * @return
         * @throws IOException If the texture could not be loaded
         */
        public Builder setTexture(ExternalReference textureRef) throws IOException {
            this.texture = AssetManager.getInstance().getTexture(renderer, textureRef);
            return this;
        }

        /**
         * Set mode and vertex count for array based drawing - this will not use element (indice) buffer.
         * ie glDrawArrays() will be used to draw the mesh.
         * 
         * @param mode The drawmode for vertices
         * @param vertexCount Number of vertices
         * @return
         */
        public Builder setArrayMode(Mode mode, int vertexCount) {
            this.vertexCount = vertexCount;
            this.mode = mode;
            return this;
        }

        /**
         * Set mode, vertexcount and element (indice) count. The created mesh will have vertexbuffer and indice buffer.
         * When drawn glDrawElements will be used.
         * 
         * @param mode
         * @param vertexCount
         * @param indiceCount
         * @return
         */
        public Builder setElementMode(Mode mode, int vertexCount, int indiceCount) {
            this.indiceCount = indiceCount;
            this.vertexCount = vertexCount;
            this.mode = mode;
            return this;
        }

        /**
         * Sets the material to be used in the mesh
         * 
         * @param material
         * @return
         */
        public Builder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        /**
         * Sets the shapebuilder to be used when building mesh shape(s)
         * 
         * @param shapeBuilder The shape builder, or null
         * @return
         */
        public Builder setShapeBuilder(ShapeBuilder shapeBuilder) {
            this.shapeBuilder = shapeBuilder;
            return this;
        }

        /**
         * Checks that the needed arguments has been set
         */
        protected void validate() {
            if (texture == null || vertexCount <= 0 || mode == null || material == null) {
                throw new IllegalArgumentException("Missing argument when creating mesh: " + texture + ", " + ", "
                        + vertexCount + ", " + mode + ", " + material);
            }
        }

        /**
         * Creates the mesh for the arguments supplied to this builder.
         * 
         * @return The mesh
         * @throws IllegalArgumentException If the needed arguments has not been set
         */
        public Mesh create() throws IOException {
            validate();
            Mesh mesh = new Mesh();
            mesh.createMesh(texture, material, vertexCount, indiceCount, mode);
            if (shapeBuilder != null) {
                shapeBuilder.build(mesh);
            }
            return mesh;
        }

        /**
         * Calculates the bounds covering this mesh.
         * 
         * @return
         */
        public Bounds createBounds() {
            return null;
        }

    }

    public final static int MAX_TEXTURE_COUNT = 1;
    private final static String NULL_PARAMETER_STR = "Null parameter";

    /**
     * Currently only supports single texture
     * Texture object should not be exported, store texture as resource and use a reference
     */
    transient protected Texture2D[] texture = new Texture2D[MAX_TEXTURE_COUNT];

    /**
     * Reference to texture, used when importing/exporting
     */
    @SerializedName("textureref")
    protected String textureRef;

    /**
     * Uniforms, used when rendering this Mesh depending on what ShaderProgram is used.
     */
    transient protected float[] uniforms;
    /**
     * The mapper used to find positions of property attributes.
     */
    protected transient PropertyMapper mapper;

    /**
     * Optional consumer for attributes, use this when dynamic mesh is needed. ie when the generic attribute data must
     * be updated each frame.
     * TODO Maybe move this to the node?
     */
    transient protected Consumer attributeConsumer;
    /**
     * One or more generic attribute arrays, read by the program specified in the material.
     * The reason to have multiple buffers is for cases where for instance vertex and UV data does not change
     * but other attributes change (per frame).
     */
    transient protected VertexBuffer[] attributes;
    transient protected ElementBuffer indices;
    /**
     * Drawmode, if indices is null then glDrawArrays shall be used with this mode
     */
    transient protected Mode mode;
    transient protected Material material;

    /**
     * Creates a new empty mesh, the attribute/index buffers must be prepared before rendering can take place.
     */
    public Mesh() {
        super();
    }

    /**
     * Creates a shallow copy of the source mesh, only the serialized values are copied, id and textureRef.
     * 
     * @param source
     */
    public Mesh(Mesh source) {
        setId(source.getId());
        textureRef = source.textureRef;
    }

    /**
     * Creates the Mesh to be rendered, creating buffers as needed.
     * After this method returns it shall be possible to render the mesh although it must be filled with data, for
     * instance using a {@link ShapeBuilder}
     * The program will be set to the material in this mesh.
     * 
     * @param texture The texture to use, depends on mesh implementation
     * @param material
     * @param vertexCount Number of vertices to create storage for
     * @param indiceCount Number of indices in elementbuffer
     * @param mode The drawmode, eg how primitives are drawn
     * @return
     * @throws IllegalArgumentException If texture, material or mode is null
     */
    public void createMesh(Texture2D texture, Material material, int vertexCount, int indiceCount, Mode mode) {
        if (texture == null || material == null || mode == null) {
            throw new IllegalArgumentException("Null parameter: " + texture + ", " + material + ", " + mode);
        }
        setTexture(texture, Texture2D.TEXTURE_0);
        setMode(mode);
        this.material = new Material(material);
        ShaderProgram program = material.getProgram();
        mapper = new PropertyMapper(program);
        this.material.setProgram(program);
        internalCreateBuffers(program, vertexCount, indiceCount);
    }

    /**
     * Creates the buffers, vertex and indexbuffers as needed. Attribute and uniform storage.
     * If texture is {@link TiledTexture2D} then vertice and index storage will be createde for 1 sprite.
     * Do not call this method directly - it is called from {@link #createMesh(ShaderProgram, Texture2D, Material)}
     * 
     * @param program
     * @param vertexCount Number of vertices to create storage for
     * @param indiceCount Number of elementbuffer indices
     * @param drawMode Mesh drawmode
     */
    protected void internalCreateBuffers(ShaderProgram program, int vertexCount, int indiceCount) {
        attributes = program.createAttributeBuffers(this, vertexCount);
        indices = new ElementBuffer(indiceCount, Type.SHORT);
        program.setupUniforms(this);
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
        setBuffers(vertices);
        this.material = material;
        if (texture != null) {
            this.texture[Texture2D.TEXTURE_0] = texture;
        }
    }

    /**
     * Sets the vertice/attribute buffers,
     * 
     * @param attributes Buffers containing vertices/attributes, what this means is specific to the program
     * used to render the mesh.
     */
    protected void setBuffers(VertexBuffer[] attributes) {
        this.attributes = attributes;
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
     * Returns the texture reference or null if not set, this is used when importing/exporting
     * 
     * @return
     */
    public String getTextureRef() {
        return textureRef;
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
     * Returns the buffer, at the specified index, containing vertices and attribute data
     * 
     * @param buffer Index into the vertex/attribute buffer to return
     * @return The vertexbuffer
     */
    public VertexBuffer getVerticeBuffer(int index) {
        return attributes[index];
    }

    /**
     * Returns the attribute/vertice buffers.
     * 
     * @return
     */
    public VertexBuffer[] getVerticeBuffers() {
        return attributes;
    }

    /**
     * Returns the optional element buffer, this is used when drawing using indexed vertices
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
     * @return The texture
     * @throws ArrayIndexOutOfBoundsException If index is larger than max number of textures
     */
    public Texture2D getTexture(int index) {
        return (texture[index]);
    }

    /**
     * Returns the array containing the textures.
     * Please not that this is the reference to the textures - any modifications to the texture will be
     * reflected in this class.
     * 
     * @return The textures for this mesh
     */
    public Texture2D[] getTextures() {
        return texture;
    }

    /**
     * Returns one or more defined uniform vectors used when rendering.
     * 
     * @return One or more uniform vector as used by the shader program implementation
     */
    public float[] getUniforms() {
        return uniforms;
    }

    /**
     * Sets a reference to an array with float values that can be used by when rendering this Mesh.
     * Note that the use of uniforms is depending on the shader program used.
     * 
     * @param uniforms Values to reference in this class, note that values are NOT copied.
     * 
     */
    public void setUniforms(float[] uniforms) {
        this.uniforms = uniforms;
    }


    /**
     * Sets the attribute updater for this mesh, use this for meshes where the attribute data must be updated each
     * frame.
     * This method shall copy data, as needed, into the VertexBuffer arrays that are used when the mesh is rendered.
     * What data to copy is implementation specific.
     * 
     * @param attributeConsumer Callback to set data into the generic vertex arrays, or null to remove.
     */
    public void setAttributeUpdater(Consumer attributeConsumer) {
        this.attributeConsumer = attributeConsumer;
    }

    /**
     * Returns the attribute updater.
     * 
     * @return The attribute updater or null if none is set.
     */
    public Consumer getAttributeConsumer() {
        return attributeConsumer;
    }

    /**
     * Sets the named object buffers for this mesh, the number of names allocated for this
     * mesh must match {@link #getBufferNameCount()} The named objects will be set in the buffers contained in this
     * mesh.
     * Name at offset will be put in the first vertice buffer, the second in the following vertice buffer if allocated.
     * The last name will be put in the elementbuffer if one is allocated.
     * 
     * @param count Number of names
     * @param names Array with allocated buffer names
     * @param offset Offset into array to buffer names
     * @throws IllegalArgumentException If names is null, or there is not enough names.
     */
    public void setBufferNames(int count, int[] names, int offset) {
        if (names == null) {
            throw new IllegalArgumentException(NULL_NAMES);
        }
        if (names.length < offset + count) {
            throw new IllegalArgumentException(NOT_ENOUGH_NAMES);
        }
        if (indices != null) {
            indices.setBufferName(names[offset++]);
        }
        for (VertexBuffer b : attributes) {
            b.setBufferName(names[offset++]);
        }
    }

    /**
     * Returns the number of buffer object names that are needed for this mesh.
     * Usage of buffer objects is recommended over passing java.nio.Buffers directly to
     * {@link GLES20Wrapper#glVertexAttribPointer(int, int, int, boolean, int, java.nio.Buffer)}
     * 
     * @return
     */
    public int getBufferNameCount() {
        int count = attributes.length;
        if (indices != null) {
            count++;
        }
        return count;
    }

    /**
     * Sets the draw mode to use
     * 
     * @param mode GL drawmode, one of GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP,
     * GL_TRIANGLE_FAN, and GL_TRIANGLES
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets the draw mode to use when drawing this mesh
     * 
     * @return The GL drawmode for drawing this mesh
     */
    public Mode getMode() {
        return mode;
    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        texture = null;
        textureRef = null;
        uniforms = null;
        mapper = null;
        attributeConsumer = null;
        attributes = null;
        indices = null;
        mode = null;
        material = null;
        deleteVBO(renderer);
    }

    private void deleteVBO(NucleusRenderer renderer) {
        if (indices.getBufferName() > 0) {
            renderer.deleteBuffers(1, new int[] { indices.getBufferName() }, 0);
        }
        for (VertexBuffer buffer : attributes) {
            if (buffer.getBufferName() > 0) {
                renderer.deleteBuffers(1, new int[] { buffer.getBufferName() }, 0);
            }
        }
    }

    @Override
    public PropertyMapper getMapper() {
        return mapper;
    }

}
