package com.nucleus.geometry;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.nucleus.assets.AssetManager;
import com.nucleus.bounds.Bounds;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.BufferObjectsFactory;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.TranslateProgram;
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
     * Builder for meshes
     *
     * @param <T>
     */
    public static class Builder<T extends Mesh> implements MeshBuilder<Mesh> {

        protected NucleusRenderer renderer;
        protected Texture2D texture;
        protected Material material;
        protected int[] attributesPerVertex;
        protected int vertexCount = -1;
        protected int indiceCount = 0;
        /**
         * Extra attributes to allocate for each vertex
         */
        protected int vertexStride;
        /**
         * Optional builder parameter that can be used to determine number of vertices.
         */
        protected int objectCount = 1;
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
         * Sets the number of objects the builder shall create mesh for, used for instance when mesh uses
         * batching/instancing, or is a geometryshader
         * 
         * @param objectCount Number of objects to create when building the mesh
         * @return
         */
        public Builder<T> setObjectCount(int objectCount) {
            this.objectCount = objectCount;
            return this;
        }

        /**
         * Sets the drawmode for the mesh
         * 
         * @param mode
         * @return;
         */
        public Builder<T> setMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the number of attributes (floats) per vertex to create for each buffer
         * 
         * @param sizePerVertex
         * @return
         */
        public Builder<T> setAttributesPerVertex(int[] sizePerVertex) {
            this.attributesPerVertex = sizePerVertex;
            return this;
        }

        /**
         * Sets the texture to use for the created mesh
         * 
         * @param texture
         * @return
         */
        public Builder<T> setTexture(Texture2D texture) {
            this.texture = texture;
            return this;
        }

        /**
         * 
         * @param vertexStride Extra attributes to allocate per vertex, if a value larger than 0 is specified then this
         * number of attributes will be added to the attributes allocated for the mesh (for each vertex)
         * @return
         */
        public Builder<T> setVertexStride(int vertexStride) {
            this.vertexStride = vertexStride;
            return this;
        }

        @Override
        public Mesh create(RenderableNode<Mesh> parent) throws IOException, GLException {
            validate();
            Mesh mesh = createInstance();
            mesh.createMesh(texture, attributesPerVertex, null, material, vertexCount, indiceCount, mode);
            if (shapeBuilder != null) {
                AttributeBuffer attributes = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
                shapeBuilder.build(attributes, mesh.getTexture(Texture2D.TEXTURE_0), mesh.getElementBuffer(), mesh.getMode());
            }
            if (com.nucleus.renderer.Configuration.getInstance().isUseVBO()) {
                BufferObjectsFactory.getInstance().createVBOs(renderer, mesh);
            }
            if (parent != null) {
                parent.addMesh(mesh);
            }
            return mesh;
        }

        /**
         * Returns the shader program that can be used to draw the mesh. This is normally only used when program to use
         * is not known.
         * For instance when loading nodes, or other scenarios where mesh type is known (but not program)
         * 
         * @param gles
         * @return Shader program to use for drawing mesh.
         */
        public ShaderProgram createProgram(GLES20Wrapper gles) {
            return AssetManager.getInstance().getProgram(gles, new TranslateProgram(texture));
        }

        @Override
        public Mesh createInstance() {
            return new Mesh();
        }

        /**
         * Fetches the texture and stores as texture to be used when creating mesh
         * 
         * @param textureRef
         * @throws IOException If the texture could not be loaded
         */
        public Builder<T> setTexture(ExternalReference textureRef) throws IOException {
            this.texture = AssetManager.getInstance().getTexture(renderer, textureRef);
            return this;
        }

        /**
         * Set mode and vertex count for array based drawing - this will not use element (indice) buffer.
         * ie glDrawArrays() will be used to draw the mesh.
         * 
         * @param mode The drawmode for vertices
         * @param vertexCount Number of vertices
         * @param vertexStride Extra attributes to allocate per vertex, if a value larger than 0 is specified then this
         * number of attributes will be added to the attributes allocated for the mesh (for each vertex)
         * @return
         */
        public Builder<T> setArrayMode(Mode mode, int vertexCount, int vertexStride) {
            this.vertexCount = vertexCount;
            this.mode = mode;
            this.vertexStride = vertexStride;
            return this;
        }

        /**
         * Set mode, vertexcount and element (indice) count. The created mesh will have vertexbuffer and indice buffer.
         * When drawn glDrawElements will be used.
         * 
         * @param mode
         * @param vertexCount
         * @param vertexStride Extra attributes to allocate per vertex, if a value larger than 0 is specified then this
         * number of attributes will be added to the attributes allocated for the mesh (for each vertex)
         * @param indiceCount
         */
        public Builder<T> setElementMode(Mode mode, int vertexCount, int vertexStride, int indiceCount) {
            this.indiceCount = indiceCount;
            this.vertexCount = vertexCount;
            this.mode = mode;
            this.vertexStride = vertexStride;
            return this;
        }

        /**
         * Sets the material to be used in the mesh
         * 
         * @param material
         */
        public Builder<T> setMaterial(Material material) {
            this.material = material;
            return this;
        }

        /**
         * Sets the shapebuilder to be used when building mesh shape(s)
         * 
         * @param shapeBuilder The shape builder, or null
         * @return
         */
        public Builder<T> setShapeBuilder(ShapeBuilder shapeBuilder) {
            this.shapeBuilder = shapeBuilder;
            return this;
        }

        /**
         * Checks that the needed arguments has been set
         */
        protected void validate() {
            if ((attributesPerVertex == null) || texture == null || vertexCount <= 0 || mode == null
                    || material == null) {
                throw new IllegalArgumentException(
                        "Missing argument when creating mesh: texture=" + texture + ", vertexcount="
                                + vertexCount + ", mode=" + mode + ", material=" + material + ", attributesPerVertex="
                                + attributesPerVertex);
            }
        }

        /**
         * Calculates the bounds covering this mesh.
         * 
         * @return
         */
        @Override
        public Bounds createBounds() {
            return null;
        }

        public Texture2D getTexture() {
            return texture;
        }

        public Material getMaterial() {
            return material;
        }

        public ShapeBuilder getShapeBuilder() {
            return shapeBuilder;
        }

    }

    /**
     * Creates a Builder to create a mesh that can be rendered in a Node
     * 
     * @param renderer
     * @param maxVerticeCount
     * @param material
     * @param program
     * @param texture
     * @param shapeBuilder
     * @param mode
     * @return
     */
    public static Builder<Mesh> createBuilder(NucleusRenderer renderer, int maxVerticeCount, Material material,
            ShaderProgram program, Texture2D texture, ShapeBuilder shapeBuilder, Mesh.Mode mode) {
        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
        builder.setTexture(texture);
        builder.setMaterial(material);
        builder.setArrayMode(mode, maxVerticeCount, 0);
        builder.setShapeBuilder(shapeBuilder).setAttributesPerVertex(program.getAttributeSizes());
        return builder;
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
    transient protected AttributeBuffer[] attributes;
    transient protected ElementBuffer indices;
    transient protected BlockBuffer[] blockBuffers;
    /**
     * Number of elements to draw
     */
    transient int drawCount;
    /**
     * Max number of vertices in buffer
     */
    transient int maxVertexCount;
    /**
     * Offset to first element
     */
    transient int offset;

    /**
     * Drawmode, if indices is null then glDrawArrays shall be used with this mode
     */
    transient protected Mode mode;
    /**
     * TODO - material should not be specified both in Node and in Mesh, this instance is copied here from Builder which
     * normally takes it from the Node.
     * Perhaps material should be divided into program and texture/render properties.
     * Each node should render with same programs.
     */
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

    public void createMesh(Texture2D texture, int[] attributeSizes, InterfaceBlock[] interfaceBlocks, Material material,
            int vertexCount,
            int indiceCount, Mode mode) {
        if (texture == null || material == null || mode == null || attributeSizes == null) {
            throw new IllegalArgumentException(
                    "Null parameter: " + texture + ", " + material + ", " + mode + ", " + attributeSizes);
        }
        setTexture(texture, Texture2D.TEXTURE_0);
        setMode(mode);
        this.material = new Material(material);
        internalCreateBuffers(attributeSizes, interfaceBlocks, vertexCount, indiceCount);
    }

    /**
     * Creates the buffers, vertex and indexbuffers as needed - and sets the static data in uniforms
     * If texture is {@link TiledTexture2D} then vertice and index storage will be createde for 1 sprite.
     * Do not call this method directly - it is called from {@link #createMesh(ShaderProgram, Texture2D, Material)}
     * 
     * @param attributeSizes Number of (float) attributes to allocate for each vertex. One float = 4 bytes.
     * @param interfaceBlocks Number of block buffers to allocate and their sizes
     * @param vertexCount Number of vertices to create storage for
     * @param indiceCount Number of elementbuffer indices
     * @param drawMode Mesh drawmode
     */
    protected void internalCreateBuffers(int[] attributeSizes, InterfaceBlock[] interfaceBlocks, int vertexCount,
            int indiceCount) {
        attributes = AttributeBuffer.createAttributeBuffers(attributeSizes, vertexCount, GLES20.GL_FLOAT);
        maxVertexCount = vertexCount;
        if (indiceCount > 0) {
            indices = new ElementBuffer(indiceCount, Type.SHORT);
            setDrawCount(indiceCount, 0);
        } else {
            setDrawCount(vertexCount, 0);
        }
        blockBuffers = BlockBuffer.createBlockBuffers(interfaceBlocks);
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

    @Override
    public AttributeBuffer getAttributeBuffer(BufferIndex buffer) {
        return attributes[buffer.index];
    }

    @Override
    public AttributeBuffer getAttributeBuffer(int index) {
        return attributes[index];
    }

    /**
     * Returns the attribute/vertice buffers.
     * 
     * @return
     */
    public AttributeBuffer[] getAttributeBuffers() {
        return attributes;
    }

    /**
     * Returns the block buffer storage, for instance uniform blocks.
     * 
     * @return
     */
    public BlockBuffer[] getBlockBuffers() {
        return blockBuffers;
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

    @Override
    public void setAttributeUpdater(Consumer attributeConsumer) {
        this.attributeConsumer = attributeConsumer;
    }

    @Override
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
        for (AttributeBuffer b : attributes) {
            if (b != null) {
                b.setBufferName(names[offset++]);
            }
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
        int count = indices != null ? 1 : 0;
        for (AttributeBuffer vb : attributes) {
            if (vb != null) {
                count++;
            }
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
        attributeConsumer = null;
        attributes = null;
        indices = null;
        mode = null;
        material = null;
        deleteVBO(renderer);
    }

    private void deleteVBO(NucleusRenderer renderer) {
        if (indices != null && indices.getBufferName() > 0) {
            renderer.deleteBuffers(1, new int[] { indices.getBufferName() }, 0);
        }
        if (attributes != null) {
            for (AttributeBuffer buffer : attributes) {
                if (buffer.getBufferName() > 0) {
                    renderer.deleteBuffers(1, new int[] { buffer.getBufferName() }, 0);
                }
            }
        }
    }

    /**
     * Sets the number of elements/vertices to draw and the offset to first element.
     * offset + drawCount must be less or equal to count.
     * TODO - Unify this with the usage in ElementBuffer
     * 
     * @param drawCount Number of elements to draw (indices)
     * @param offset First element to draw
     */
    public void setDrawCount(int drawCount, int offset) {
        ElementBuffer buffer = getElementBuffer();
        int max = maxVertexCount;
        if (buffer != null && buffer.getCount() > 0) {
            max = buffer.getCount();
        }
        this.drawCount = Math.min(drawCount, max);
        this.offset = offset;
    }

    /**
     * Returns the number of vertices to draw - this is set to the same as the vertice count when the buffer
     * is created
     * 
     * @return
     */
    public int getDrawCount() {
        return drawCount;
    }

    /**
     * Returns the offset to the first vertice to draw
     * 
     * @return
     */
    public int getOffset() {
        return offset;
    }

}
