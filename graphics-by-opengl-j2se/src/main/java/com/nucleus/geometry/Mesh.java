package com.nucleus.geometry;

import com.google.gson.annotations.SerializedName;
import com.nucleus.Backend.DrawMode;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.io.BaseReference;
import com.nucleus.renderer.NucleusRenderer;
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
public abstract class Mesh extends BaseReference implements AttributeUpdater {

    private final static String NULL_NAMES = "Buffer names is null";
    private final static String NOT_ENOUGH_NAMES = "Not enough buffer names";

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
    /**
     * Number of elements to draw
     */
    transient int drawCount;
    /**
     * Max number of vertices in buffer
     */
    protected transient int maxVertexCount;
    /**
     * Offset to first element
     */
    transient int offset;

    /**
     * Drawmode, if indices is null then glDrawArrays shall be used with this mode
     */
    transient protected DrawMode mode;
    /**
     * TODO - material should not be specified both in Node and in Mesh, this instance is copied here from Builder which
     * normally takes it from the Node.
     * Perhaps material should be divided into program and texture/render properties.
     * Each node should render with same programs.
     */
    transient protected Material material;

    protected Mesh() {
        super();
    }

    protected Mesh(Mesh source) {
        setId(source.getId());
        textureRef = source.textureRef;
    }

    public void createMesh(Texture2D texture, int[] attributeSizes, Material material,
            int vertexCount, int indiceCount, DrawMode mode) {
        if (texture == null || material == null || mode == null || attributeSizes == null) {
            throw new IllegalArgumentException(
                    "Null parameter: " + texture + ", " + material + ", " + mode + ", " + attributeSizes);
        }
        setTexture(texture, Texture2D.TEXTURE_0);
        setMode(mode);
        this.material = new Material(material);
        internalCreateBuffers(attributeSizes, vertexCount, indiceCount);
    }

    /**
     * Creates the buffers, vertex and indexbuffers as needed - and sets the static data in uniforms
     * If texture is {@link TiledTexture2D} then vertice and index storage will be createde for 1 sprite.
     * 
     * @param attributeSizes Number of (float) attributes to allocate for each vertex. One float = 4 bytes.
     * @param vertexCount Number of vertices to create storage for
     * @param indiceCount Number of elementbuffer indices
     * @param drawMode Mesh drawmode
     */
    protected void internalCreateBuffers(int[] attributeSizes, int vertexCount, int indiceCount) {
        attributes = AttributeBuffer.createAttributeBuffers(attributeSizes, vertexCount);
        maxVertexCount = vertexCount;
        if (indiceCount > 0) {
            indices = new ElementBuffer(indiceCount, Type.SHORT);
            setDrawCount(indiceCount, 0);
        } else {
            setDrawCount(vertexCount, 0);
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
     * Usage of buffer objects is recommended over passing java.nio.Buffers directly
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
     * @param mode Drawmode to use for this mesh
     */
    public void setMode(DrawMode mode) {
        this.mode = mode;
    }

    /**
     * Gets the draw mode to use when drawing this mesh
     * 
     * @return The drawmode for drawing this mesh
     */
    public DrawMode getMode() {
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
