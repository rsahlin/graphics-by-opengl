package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.nucleus.common.Type;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.DefaultNodeRenderer;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.ShaderProgram;

/**
 * Node that has support for one or more generic Meshes.
 * This node can be used by implementations as a base for {@link RenderableNode} functionality.
 * Use this for custom Mesh types that are not loaded assets.
 *
 * @param <T> The Mesh typeclass. This is the Mesh class that will be rendered.
 */
public abstract class AbstractMeshNode<T> extends AbstractNode implements RenderableNode<T> {

    transient protected ArrayList<T> meshes = new ArrayList<T>();
    transient protected ArrayList<T> nodeMeshes = new ArrayList<>();
    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    transient protected NodeRenderer<RenderableNode<Mesh>> nodeRenderer = new DefaultNodeRenderer();

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected AbstractMeshNode() {
        super();
    }

    /**
     * Default constructor
     * @param root
     * @param type
     */
    protected AbstractMeshNode(RootNode root, Type<Node> type) {
        super(root, type);
        createMeshRenderer();
    }

    @Override
    public ArrayList<T> getMeshes(ArrayList<T> list) {
        list.addAll(meshes);
        return list;
    }

    /**
     * Adds a mesh to be rendered with this node. The mesh is added at the specified index, if specified
     * NOT THREADSAFE
     * THIS IS AN INTERNAL METHOD AND SHOULD NOT BE USED!
     * 
     * @param mesh
     * @param index The index where this mesh is added or null to add at end of current list
     */
    @Deprecated
    public void addMesh(T mesh, MeshIndex index) {
        if (index == null) {
            meshes.add(mesh);
        } else {
            meshes.add(index.index, mesh);
        }
    }

    /**
     * Removes the mesh from this node, if present.
     * If many meshes are added this method may have a performance impact.
     * NOT THREADSAFE
     * THIS IS AN INTERNAL METHOD AND SHOULD NOT BE USED!
     * 
     * @param mesh The mesh to remove from this Node.
     * @return true if the mesh was removed
     */
    @Deprecated
    public boolean removeMesh(T mesh) {
        return meshes.remove(mesh);
    }

    /**
     * Returns the number of meshes in this node
     * 
     * @return
     */
    public int getMeshCount() {
        return meshes.size();
    }

    @Override
    public ShaderProgram getProgram() {
        return program;
    }

    @Override
    public void setProgram(ShaderProgram program) {
        if (program == null) {
            throw new IllegalArgumentException(NULL_PROGRAM_STRING);
        }
        this.program = program;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the external reference of the texture for this node, this is used when importing
     * 
     * @return
     */
    public ExternalReference getTextureRef() {
        return textureRef;
    }

    /**
     * Returns the mesh for a specific index
     * 
     * @param type
     * @return Mesh for the specified index or null
     */
    public T getMesh(MeshIndex index) {
        if (index.index < meshes.size()) {
            return meshes.get(index.index);
        }
        return null;
    }

    /**
     * Returns the mesh at the specified index
     * 
     * @param index
     * @return The mesh, or null
     */
    public T getMesh(int index) {
        return meshes.get(index);
    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        meshes = null;
    }

    @Override
    public void addMesh(T mesh) {
        if (mesh != null) {
            meshes.add(mesh);
        }
    }

    @Override
    public MeshBuilder<T> createMeshBuilder(GLES20Wrapper gles, ShapeBuilder shapeBuilder)
            throws IOException {

        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(gles);
        return initMeshBuilder(gles, 1, shapeBuilder, builder);
    }

    /**
     * Sets texture, material and shapebuilder from the parent node - if not already set in builder.
     * Sets objectcount and attribute per vertex size.
     * If parent does not have program the
     * {@link com.nucleus.geometry.Mesh.Builder#createProgram(com.nucleus.opengl.GLES20Wrapper)}
     * method is called to create a suitable program.
     * The returned builder shall have needed values to create a mesh.
     * 
     * @param gles
     * @param count Number of objects
     * @param shapeBuilder
     * @param builder
     * @throws IOException
     */
    protected MeshBuilder<T> initMeshBuilder(GLES20Wrapper gles, int count, ShapeBuilder shapeBuilder,
            Mesh.Builder<Mesh> builder)
            throws IOException {
        if (builder.getTexture() == null) {
            builder.setTexture(getTextureRef());
        }
        if (builder.getMaterial() == null) {
            builder.setMaterial(getMaterial() != null ? getMaterial() : new Material());
        }
        builder.setObjectCount(count);
        if (builder.getShapeBuilder() == null) {
            builder.setShapeBuilder(shapeBuilder);
        }
        if (getProgram() == null) {
            setProgram(builder.createProgram(gles));
        }
        builder.setAttributesPerVertex(getProgram().getAttributeSizes());
        return (MeshBuilder<T>) builder;
    }

    /**
     * Creates the mesh renderer - called if {@link #meshRenderer} is null.
     */
    protected abstract void createMeshRenderer();

    @Override
    public com.nucleus.renderer.NodeRenderer<?> getNodeRenderer() {
        return nodeRenderer;
    }
    
}


