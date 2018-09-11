package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.nucleus.common.Environment;
import com.nucleus.common.Type;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;

public abstract class NucleusMeshNode<T> extends AbstractNode implements RenderableNode<T> {

    transient protected ArrayList<T> meshes = new ArrayList<T>();
    transient protected ArrayList<T> nodeMeshes = new ArrayList<>();
    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected NucleusMeshNode() {
        super();
    }

    protected NucleusMeshNode(RootNode root, Type<Node> type) {
        super(root, type);
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

    @Override
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
    public MeshBuilder<T> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder shapeBuilder)
            throws IOException {

        Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
        return initMeshBuilder(renderer, 1, shapeBuilder, builder);
    }

    /**
     * Sets texture, material and shapebuilder from the parent node - if not already set in builder.
     * Sets objectcount and attribute per vertex size.
     * If parent does not have program the
     * {@link com.nucleus.geometry.Mesh.Builder#createProgram(com.nucleus.opengl.GLES20Wrapper)}
     * method is called to create a suitable program.
     * The returned builder shall have needed values to create a mesh.
     * 
     * @param renderer
     * @param count Number of objects
     * @param shapeBuilder
     * @param builder
     * @throws IOException
     */
    protected MeshBuilder<T> initMeshBuilder(NucleusRenderer renderer, int count, ShapeBuilder shapeBuilder,
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
            setProgram(builder.createProgram(renderer.getGLES()));
        }
        builder.setAttributesPerVertex(getProgram().getAttributeSizes());
        return (MeshBuilder<T>) builder;
    }

    
    @Override
    public boolean renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
        nodeMeshes.clear();
        getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            ShaderProgram program = getProgram(gles, this, currentPass);
            gles.glUseProgram(program.getProgram());
            GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
            // TODO - is this the best place for this check - remember, this should only be done in debug cases.
            if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
                program.validateProgram(gles);
            }
            renderMeshes(renderer, program, nodeMeshes, matrices);
        }
        return true;
    }

    /**
     * 
     * @param node The node being rendered
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(GLES20Wrapper gles, RenderableNode<?> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(gles, pass, program.getShading());
    }

    /**
     * Renders the meshes in this node.
     * 
     * @param renderer
     * @param program
     * @param meshes
     * @param matrices
     * @throws GLException
     */
    protected void renderMeshes(NucleusRenderer renderer, ShaderProgram program, ArrayList<T> meshes,
            float[][] matrices) throws GLException {
        for (T mesh : meshes) {
            renderMesh(renderer, program, mesh, matrices);
        }
    }
    
}
