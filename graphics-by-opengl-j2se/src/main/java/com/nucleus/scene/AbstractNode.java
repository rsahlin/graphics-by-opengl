package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.nucleus.bounds.Bounds;
import com.nucleus.bounds.CircularBounds;
import com.nucleus.bounds.RectangularBounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.common.Type;
import com.nucleus.event.EventManager;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.geometry.Material;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.VariableIndexer.Indexer;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Rectangle;
import com.nucleus.vecmath.Transform;

/**
 * Point of interest in a scene. 
 * This shall be a 'dumb' node in that sense that it shall not contain logic or behavior other than the ability to
 * be traversed and possibly rendered.
 * This class may be serialized using GSON, however since TypeAdapter is used to find implementation class of node
 * it is currently not possible to deserialize (vanilla) Node (due to recursion of deserialization)
 * 
 * If a node contains properties the {@linkplain EventManager#sendObjectEvent(Object, String, String)} is called
 * with the property/key and this class as object.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class AbstractNode extends BaseReference implements Node {

    public final static String NULL_PROGRAM_STRING = "Program is null";

    /**
     * Known node types
     */
    public enum NodeTypes implements Type<Node> {

        layernode(LayerNode.class),
        switchnode(SwitchNode.class),
        linedrawernode(LineDrawerNode.class),
        componentnode(ComponentNode.class),
        meshnode(MeshNode.class),
        rootnode(BaseRootNode.class),
        gltfnode(GLTFNode.class);

        private final Class<?> theClass;

        private NodeTypes(Class<?> theClass) {
            this.theClass = theClass;
        }

        /**
         * Returns the class to instantiate for the different types
         * 
         * @return
         */
        @Override
        public Class<?> getTypeClass() {
            return theClass;
        }

        @Override
        public String getName() {
            return name();
        }

    }

    public static final String STATE = "state";
    public static final String TYPE = "type";
    public static final String CHILDREN = "children";
    public static final String TEXTUREREF = "textureRef";
    public static final String PROPERTIES = "properties";
    public static final String PASS = "pass";

    public enum MeshIndex {
        /**
         * Main mesh
         */
        MAIN(0),
        /**
         * Extra mesh for ui/editing purposes
         */
        UI(1);

        public final int index;

        MeshIndex(int index) {
            this.index = index;
        }

    }

    @SerializedName(TYPE)
    private String type;
    @SerializedName(Transform.TRANSFORM)
    protected Transform transform;
    @SerializedName(ViewFrustum.VIEWFRUSTUM)
    protected ViewFrustum viewFrustum;
    /**
     * The childnodes shall always be processed/rendered in the order they are defined.
     * This makes it possible to treat the children as a list that is rendered in a set order.
     * Rearranging the children will alter the render order.
     */
    @SerializedName(CHILDREN)
    protected ArrayList<Node> children = new ArrayList<Node>();

    @SerializedName(Bounds.BOUNDS)
    private Bounds bounds;

    @SerializedName(Material.MATERIAL)
    protected Material material;

    @SerializedName(RenderPass.RENDERPASS)
    private ArrayList<RenderPass> renderPass;

    @SerializedName(STATE)
    private State state = State.ON;

    /**
     * Reference to texture, used when importing / exporting.
     * No runtime meaning
     */
    @SerializedName(TEXTUREREF)
    protected ExternalReference textureRef;

    /**
     * Properties for this node
     */
    @SerializedName(PROPERTIES)
    private Map<String, String> properties;

    /**
     * One or more passes that this Node should be used in.
     */
    @SerializedName(PASS)
    private Pass pass = Pass.ALL;

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
    transient protected ShaderProgram program;
    transient protected Indexer indexer;

    /**
     * The parent node, this shall be set when node is added as child
     */
    transient Node parent;
    /**
     * The root node
     */
    transient private RootNode rootNode;

    /**
     * Used by GSON and {@link #createInstance(RootNode)} method - do NOT call directly
     */
    protected AbstractNode() {
    }

    /**
     * Creates an empty node, add children and meshes as needed.
     * 
     * @param root
     * @param type
     */
    protected AbstractNode(RootNode root, Type<Node> type) {
        setRootNode(root);
        setType(type);
    }

    /**
     * Creates an empty node with unique (for the scene) Id.
     * The uniqueness of the id is NOT checked.
     * 
     * @param id
     */
    public AbstractNode(String id) {
        setId(id);
    }

    /**
     * Creates a new, empty, instance of the specified nodeType. The type will be set.
     * Do not call this method directly, use {@link NodeFactory}
     * 
     * @param nodeType
     * @paran root The root of the created instance
     * @return
     * @throws IllegalArgumentException If nodeType or root is null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Node createInstance(Type<Node> nodeType, RootNode root)
            throws InstantiationException, IllegalAccessException {
        if (nodeType == null || root == null) {
            throw new IllegalArgumentException("Null parameter:" + nodeType + ", " + root);
        }
        Node node = (Node) nodeType.getTypeClass().newInstance();
        node.setType(nodeType);
        node.setRootNode(root);
        return node;
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
    protected void copyMaterial(AbstractNode source) {
        if (source.material != null) {
            copyMaterial(source.material);
        }
    }

    public Material getMaterial() {
        return material;
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
     * Copies the transform from the source node, if the transform in the source is null then this nodes transform
     * is set to null as well.
     * 
     * @param source The node to copy the transform from.
     */
    public void copyTransform(Node source) {
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

    /**
     * Returns the parent of this node, or null if this is the root
     * 
     * @return
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Returns the first (closest) parent node that has defined ViewFrustum
     * 
     * @return Closest parent node that has defined ViewFrustum, or null if not found
     */
    @Override
    public Node getParentView() {
        Node parent = getParent();
        if (parent != null) {
            if (parent.getViewFrustum() != null) {
                return parent;
            }
            return parent.getParentView();
        }
        return null;
    }

    /**
     * Returns the first (closest from this node) {@linkplain LayerNode} parent.
     * The search starts with the parent node of this node, if that is not a {@linkplain LayerNode} that nodes parent
     * is checked, continuing until root node.
     * 
     * @return The parent layer of this node, or null if none could be found
     */
    @Override
    public LayerNode getParentLayer() {
        if (parent == null) {
            return null;
        }
        if (NodeTypes.layernode.name().equals(parent.getType())) {
            return (LayerNode) parent;
        }
        return parent.getParentLayer();
    }

    /**
     * Returns the root node for this node, this is the document root.
     * 
     * @return The document root.
     */
    @Override
    public RootNode getRootNode() {
        return rootNode;
    }

    /**
     * Adds a child at the end of the list of children.
     * The child node's parent will be set to this node.
     * 
     * @param child The child to add to this node.
     * @throws IllegalArgumentException If child does not have the root node, or id set, or if a child already has been
     * added
     * with the same id
     */
    @Override
    public void addChild(Node child) {
        if (child.getRootNode() == null || child.getId() == null) {
            throw new IllegalArgumentException("Null parameter, root=" + child.getRootNode() + ", id=" + child.getId());
        }
        children.add(child);
        child.setParent(this);
        registerChild(child);
    }

    /**
     * Registers the node as a child in the rootnode
     * 
     * @param child
     * @throws IllegalArgumentException If a node with the same ID is already added to the nodetree
     */
    protected void registerChild(Node child) {
        rootNode.registerChild(child);
    }

    /**
     * Unregisters the node as child in the rootnode
     * 
     * @param child
     */
    protected void unregisterChild(Node child) {
        rootNode.unregisterChild(child);
    }

    /**
     * Removes the child from this node if it is present.
     * 
     * @param child The child to remove from this node.
     * @return True if the child was present in the list of children.
     */
    protected boolean removeChild(Node child) {
        if (children.contains(child)) {
            children.remove(child);
            unregisterChild(child);
            return true;
        }
        return false;
    }

    /**
     * Returns the list of children for this node.
     * Any modifications done to the returned list will be reflected here.
     * The childnodes shall always be processed/rendered in the order they are defined.
     * This makes it possible to treat the children as a list that is rendered in a set order.
     * Rearranging the children will alter the render order.
     * 
     * This method is deprecated - TODO use custom Enumerator or Iterator instead
     * 
     * @return The list of children.
     */
    @Override
    @Deprecated
    public ArrayList<Node> getChildren() {
        return children;
    }

    /**
     * Sets (copies) the data from the source
     * Note! This will not copy children or the transient values.
     * Call {@link #create()} to set transient values
     * 
     * @param source
     * @throws ClassCastException If source node is not same class as this.
     */
    protected void set(AbstractNode source) {
        super.set(source);
        type = source.getType();
        textureRef = source.textureRef;
        state = source.state;
        this.pass = source.pass;
        setRenderPass(source.getRenderPass());
        copyTransform(source);
        copyViewFrustum(source);
        copyMaterial(source);
        copyBounds(source);
        setProperties(source);
    }

    /**
     * Sets the rootnode for this node, this shall normally not be changed after it has been set.
     * This method shall not be called, it is used when a new instance is created using
     * {@link #createInstance(RootNode)}
     * 
     * @param root
     * @throws IllegalArgumentException If root is null
     */
    @Override
    public void setRootNode(RootNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Document root can not be null");
        }
        this.rootNode = root;
    }

    /**
     * Copies the properties from the source node to this
     * 
     * @param source
     */
    protected void setProperties(AbstractNode source) {
        if (source.properties == null || source.properties.size() == 0) {
            return;
        }
        if (properties == null) {
            properties = new HashMap<>();
        }
        for (String key : source.properties.keySet()) {
            properties.put(key, source.properties.get(key));
        }
    }

    /**
     * Returns the property for the key, if the key has no value then defaultValue is returned.
     * 
     * @param key
     * @param defaultValue The value to return if key is not set, may be null.
     * @return The property value for key, or defaultValue if not set.
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        String val = properties.get(key);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }

    @Override
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Returns node with matching id, searching through this node and recursively searching through children.
     * Children will be searched by calling {@link #getChildren()} excluding nodes that are switched off.
     * 
     * @param id Id of node to return
     * @return First instance of node with matching id, or null if none found
     */
    @Override
    public Node getNodeById(String id) {
        if (id.equals(getId())) {
            return this;
        }
        for (Node child : getChildren()) {
            Node result = child.getNodeById(id);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the first matching viewnode, this is a conveniance method to find node with view
     * 
     * @param layer Which layer the Node to return belongs to, or null to return first found LayerNode
     * @return The layernode or null if not found
     */
    public LayerNode getLayerNode(Layer layer) {
        for (Node node : children) {
            LayerNode layerNode = getLayerNode(layer, node);
            if (layerNode != null) {
                return layerNode;
            }
        }
        return null;
    }

    private LayerNode getLayerNode(Layer layer, Node node) {
        if (node.getType().equals(NodeTypes.layernode.name())) {
            if (layer == null || ((LayerNode) node).getLayer() == layer) {
                return (LayerNode) node;
            }
        }
        return getLayerNode(layer, node.getChildren());
    }

    private LayerNode getLayerNode(Layer layer, ArrayList<Node> children) {
        for (Node n : children) {
            if (n.getType().equals(NodeTypes.layernode.name())) {
                if (((LayerNode) n).getLayer() == layer || layer == null) {
                    return (LayerNode) n;
                }
            }
            LayerNode view = getLayerNode(layer, n);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    /**
     * Returns the first node with matching type, or null if none found.
     * This method will search through the active children.
     * 
     * @param type
     * @return
     */
    @Override
    public Node getNodeByType(String type) {
        if (type.equals(this.type)) {
            return this;
        }
        for (Node child : getChildren()) {
            Node result = child.getNodeByType(type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Searches through the scene children and looks for the first node with matching type.
     * 
     * @param type
     * @return
     */
    @Override
    public Node getNodeByType(Type<Node> type) {
        for (Node node : children) {
            Node n = node.getNodeByType(type.getName());
            if (n != null) {
                return n;
            }
        }
        return null;
    }

    /**
     * Returns the child node with matching id from this node, children are not searched recursively.
     * TODO Shall this method call getChildren() which will return only on-switched nodes?
     * 
     * @param id
     * @return The child from this node with matching id, or null if not found.
     */
    @Override
    public Node getChildById(String id) {
        for (Node n : children) {
            if (n.getId().equals(id)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Node '" + getId() + "', " + children.size() + " children, pass=" + pass
                + ", state=" + state
                + (renderPass != null ? ", has renderpass" : "") + (bounds != null ? ", has bounds" : "");
    }

    @Override
    public String getType() {
        return type;
    }


    @Override
    public void setType(Type<Node> type) {
        this.type = type.getName();
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
        for (Node n : children) {
            n.setState(state);
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
    protected void copyViewFrustum(Node source) {
        if (source.getViewFrustum() != null) {
            copyViewFrustum(source.getViewFrustum());
        } else {
            viewFrustum = null;
        }
    }

    /**
     * Copies the bounds from the source node into this node.
     * If the bounds in the source is null, the bounds in this node is set to null
     * 
     * @param source
     * @throws NullPointerException If source is null
     */
    protected void copyBounds(Node source) {
        if (source.getBounds() != null) {
            copyBounds(source.getBounds());
        } else {
            setBounds(null);
        }
    }

    /**
     * Copies the bounds
     * 
     * @param source
     */
    public void copyBounds(Bounds source) {
        bounds = Bounds.create(source.getType(), source.getBounds());
    }

    /**
     * Sets the bounds reference
     * 
     * @param bounds Reference to bounds, values are not copied.
     */
    @Override
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
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
                                    + " defines pointer input but does not have bounds and ViewFrustum not defined in any parent");
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

    /**
     * Look for ViewFrustum in parents nodes, stopping when ViewFrustum is found or when at root.
     * 
     * @return ViewFrustom from a parent node, or null if not defined.
     */
    protected ViewFrustum getParentsView() {
        Node viewparent = getParentView();
        return viewparent != null ? viewparent.getViewFrustum() : null;
    }

    @Override
    public boolean isInside(float[] position) {
        if (bounds != null && (state == State.ON || state == State.ACTOR)
                && getProperty(EventHandler.EventType.POINTERINPUT.name(), Constants.FALSE)
                        .equals(Constants.TRUE)) {
            bounds.transform(modelMatrix, 0);
            return bounds.isPointInside(position, 0);
        }
        return false;
    }

    /**
     * Set bounds from the specified bounds, if bounds exist but are not set.
     * If bounds is null or already set then nothing is done.
     * 
     * @param bounds
     */
    public void initBounds(Bounds sourceBounds) {
        Bounds bounds = getBounds();
        if (bounds != null && bounds.getBounds() == null) {
            bounds.setBounds(sourceBounds.getBounds());
        }
    }

    /**
     * Sets bounds from the rectangle, if bounds exist but are not set.
     * If bounds is null or already set then nothing is done.
     * 
     * @param rectangle
     */
    public void initBounds(Rectangle rectangle) {
        Bounds bounds = getBounds();
        if (bounds != null && bounds.getBounds() == null) {
            bounds.setBounds(rectangle);
        }

    }

    /**
     * Multiply the concatenated model matrix with this nodes transform matrix and store in this nodes model matrix
     * If this node does not have a transform an identity matrix is used.
     * 
     * @param concatModel The concatenated model matrix
     * @return The node matrix - this nodes transform * concatModel
     */
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
    public void destroy(NucleusRenderer renderer) {
        transform = null;
        viewFrustum = null;
        children.clear();
        bounds = null;
        properties = null;
        parent = null;
        rootNode = null;
    }

    @Override
    public boolean cullNode(Bounds cullBounds, Pass pass) {
        boolean cull = false;
        if (bounds != null) {
            switch (getBounds().getType()) {
                case CIRCULAR:
                    cull = !cullBounds.isCircularInside((CircularBounds) bounds);
                case RECTANGULAR:
                    cull = !cullBounds.isRectangleInside((RectangularBounds) bounds);
                default:
                    throw new IllegalArgumentException("Not implemented for bounds " + bounds.getType());
            }
        }
        return cull;
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
    public Pass getPass() {
        return pass;
    }

    @Override
    public void setPass(Pass pass) {
        this.pass = pass;
    }

    @Override
    public ArrayList<RenderPass> getRenderPass() {
        return renderPass;
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }

}
