package com.nucleus.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.common.Type;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.DefaultNodeRenderer;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Rectangle;
import com.nucleus.vecmath.Transform;

/**
 * Node that has support for one or more generic Meshes.
 * This node can be used by implementations as a base for {@link RenderableNode} functionality.
 * Use this for custom Mesh types that are not loaded assets.
 *
 * @param <T> The Mesh typeclass. This is the Mesh class that will be rendered.
 */
public abstract class AbstractMeshNode<T> extends AbstractNode implements RenderableNode<T> {

    public final static String NULL_PROGRAM_STRING = "Pipeline is null";

    @SerializedName(Transform.TRANSFORM)
    protected Transform transform;
    @SerializedName(ViewFrustum.VIEWFRUSTUM)
    protected ViewFrustum viewFrustum;
    @SerializedName(Material.MATERIAL)
    protected Material material;
    @SerializedName(RenderPass.RENDERPASS)
    private ArrayList<RenderPass> renderPass;
    /**
     * Reference to texture, used when importing / exporting.
     * No runtime meaning
     */
    @SerializedName(TEXTUREREF)
    protected ExternalReference textureRef;

    transient protected ArrayList<T> meshes = new ArrayList<T>();
    transient protected ArrayList<T> nodeMeshes = new ArrayList<>();
    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    transient protected NodeRenderer<RenderableNode<Mesh>> nodeRenderer = new DefaultNodeRenderer();
    /**
     * Optional projection Matrix for the node, this will affect all child nodes.
     */
    transient float[] projection;
    /**
     * The node concatenated model matrix at time of render, this is set when the node is rendered and
     * {@link #concatModelMatrix(float[])} is called
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     */
    transient float[] modelMatrix = Matrix.createMatrix();
    transient protected GraphicsPipeline pipeline;

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected AbstractMeshNode() {
        super();
    }

    /**
     * Default constructor
     * 
     * @param root
     * @param type
     */
    protected AbstractMeshNode(RootNode root, Type<Node> type) {
        super(root, type);
    }

    /**
     * Sets (copies) the data from the source
     * Note! This will not copy children or the transient values.
     * Call {@link #createTransient()} to set transient values
     * 
     * @param source
     * @throws ClassCastException If source node is not same class as this.
     */
    protected void set(AbstractMeshNode<T> source) {
        super.set(source);
        setRenderPass(source.getRenderPass());
        textureRef = source.textureRef;
        copyTransform(source);
        copyViewFrustum(source);
        copyMaterial(source);
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
    public GraphicsPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void setPipeline(GraphicsPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException(NULL_PROGRAM_STRING);
        }
        this.pipeline = pipeline;
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
        super.destroy(renderer);
        transform = null;
        viewFrustum = null;
    }

    @Override
    public void addMesh(T mesh) {
        if (mesh != null) {
            meshes.add(mesh);
        }
    }

    /**
     * Sets texture, material and shapebuilder from the parent node - if not already set in builder.
     * Sets objectcount and attribute per vertex size.
     * If parent does not have program the
     * {@link com.nucleus.geometry.MeshBuilder#createProgram()}
     * method is called to create a suitable program.
     * The returned builder shall have needed values to create a mesh.
     * 
     * @param renderer
     * @param count Number of objects
     * @param shapeBuilder
     * @param builder
     * @throws IOException
     * @throws BackendException
     */
    protected MeshBuilder<T> initMeshBuilder(NucleusRenderer renderer, int count, ShapeBuilder<T> shapeBuilder,
            MeshBuilder<Mesh> builder)
            throws IOException, BackendException {
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
        if (getPipeline() == null) {
            setPipeline(builder.createPipeline());
        }
        builder.setAttributesPerVertex(getPipeline().getAttributeSizes());
        return (MeshBuilder<T>) builder;
    }

    @Override
    public com.nucleus.renderer.NodeRenderer<?> getNodeRenderer() {
        return nodeRenderer;
    }

    /**
     * Copies the transform from the source node, if the transform in the source is null then this nodes transform
     * is set to null as well.
     * 
     * @param source The node to copy the transform from.
     */
    public void copyTransform(RenderableNode<T> source) {
        if (source.getTransform() != null) {
            copyTransform(source.getTransform());
        } else {
            this.transform = null;
        }
    }

    /**
     * Fetches the projection matrix for the specified pass, if set.
     * 
     * @param pass
     * @return Projection matrix for this node and childnodes, or null if not set
     */
    @Override
    public float[] getProjection(Pass pass) {
        switch (pass) {
            case SHADOW1:
                return null;
            default:
                return projection;

        }
    }

    @Override
    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    @Override
    public void setViewFrustum(ViewFrustum source) {
        viewFrustum = source;
        setProjection(source.getMatrix());
    }

    /**
     * Copies the viewfrustum from the source node into this class, if the viewfrustum is null in the source
     * the viewfrustum is set to null
     * 
     * @param source The source node
     * @throws NullPointerException If source is null
     */
    protected void copyViewFrustum(RenderableNode<T> source) {
        if (source.getViewFrustum() != null) {
            copyViewFrustum(source.getViewFrustum());
        } else {
            viewFrustum = null;
        }
    }

    /**
     * Copies the viewfrustum into this class.
     * 
     * @param source The viewfrustum to copy
     * @throws NullPointerException If source is null
     */
    public void copyViewFrustum(ViewFrustum source) {
        if (viewFrustum != null) {
            viewFrustum.set(source);
        } else {
            viewFrustum = new ViewFrustum(source);
        }
    }

    /**
     * Returns the transform for this node.
     * 
     * @return
     */
    @Override
    public Transform getTransform() {
        return transform;
    }

    /**
     * Copies the material from the source to this node. If the material in the source is null, the material in this
     * node is set to null
     * 
     * @param source
     * @throws NullPointerException If source is null
     */
    protected void copyMaterial(RenderableNode<T> source) {
        if (source.getMaterial() != null) {
            copyMaterial(source.getMaterial());
        }
    }

    /**
     * Copies the material into this node
     * 
     * @param source
     */
    protected void copyMaterial(Material source) {
        if (material != null) {
            material.copy(source);
        } else {
            material = new Material(source);
        }
    }

    /**
     * Copies the transform from the source to this class.
     * This will copy all values, creating the transform in this node if needed.
     * 
     * @param source The source transform to copy.
     */
    public void copyTransform(Transform source) {
        if (transform == null) {
            transform = new Transform(source);
        } else {
            transform.set(source);
        }
    }

    /**
     * Returns the resulting model matrix for this node.
     * It is updated with the concatenated model matrix for the node when it is rendered.
     * This will contain the sum of the model matrices of this nodes parents.
     * If object space collision shall be done this matrix can be used to transform the bounds.
     * 
     * @return The concatenated MVP from last rendered frame, if Node is not rendered the matrix will not be updated.
     * It will contain the values from the last frame it was processed/rendered
     */
    public float[] getModelMatrix() {
        return modelMatrix;
    }

    /**
     * Sets the optional projection for this node and child nodes.
     * If set this matrix will be used instead of the renderers projection matrix.
     * 
     * @param projection Projection matrix or null
     */
    public void setProjection(float[] projection) {
        this.projection = projection;
    }

    @Override
    public void setRenderPass(ArrayList<RenderPass> renderPass) {
        if (renderPass != null) {
            this.renderPass = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (RenderPass rp : renderPass) {
                if (rp.getId() != null && ids.contains(rp.getId())) {
                    throw new IllegalArgumentException("Already contains renderpass with id: " + rp.getId());
                }
                ids.add(rp.getId());
                this.renderPass.add(rp);
            }
        } else {
            this.renderPass = null;
        }
    }

    @Override
    public ArrayList<RenderPass> getRenderPass() {
        return renderPass;
    }

    @Override
    public float[] concatModelMatrix(float[] concatModel) {
        if (concatModel == null) {
            return transform != null ? Matrix.copy(transform.updateMatrix(), 0, modelMatrix, 0)
                    : Matrix.setIdentity(modelMatrix, 0);
        }
        Matrix.mul4(concatModel, transform != null ? transform.updateMatrix() : Matrix.IDENTITY_MATRIX,
                modelMatrix);
        return modelMatrix;
    }

    @Override
    public boolean isInside(float[] position) {
        if (bounds != null && (state == State.ON || state == State.ACTOR)) {
            bounds.transform(modelMatrix, 0);
            return bounds.isPointInside(position, 0);
        }
        return false;
    }

    @Override
    public void onCreated() {
        // Check if bounds should be created explicitly
        ViewFrustum vf = getViewFrustum();
        if (bounds != null && bounds.getBounds() == null) {
            // Bounds object defined in node but no bound values set.
            // try to calculate from viewfrustum.
            if (getProperty(EventHandler.EventType.POINTERINPUT.name(), Constants.FALSE)
                    .equals(Constants.TRUE)) {
                // Has pointer input so must have bounds
                vf = vf != null ? vf : getParentsView();
                if (vf == null) {
                    throw new IllegalArgumentException(
                            "Node " + getId()
                                    + " defines pointer input but does not have bounds and ViewFrustum defined in any parent");
                }
            }
            if (vf != null) {
                float[] values = vf.getValues();
                initBounds(new Rectangle(values[ViewFrustum.LEFT_INDEX], values[ViewFrustum.TOP_INDEX], vf.getWidth(),
                        vf.getHeight()));
            }
        }
        if (vf != null) {
            setProjection(vf.getMatrix());
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", " + meshes.size() + " meshes" + (renderPass != null ? ", has renderpass" : "");
    }

}
