package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.nucleus.bounds.Bounds;
import com.nucleus.bounds.CircularBounds;
import com.nucleus.bounds.RectangularBounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.event.EventManager;
import com.nucleus.io.BaseReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.Pass;
import com.nucleus.vecmath.Rectangle;

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

    /**
     * Known node types
     */
    public enum NodeTypes implements Type<Node> {

    layernode(LayerNode.class),
    switchnode(SwitchNode.class),
    linedrawernode(LineDrawerNode.class),
    componentnode(ComponentNode.class),
    meshnode(MeshNode.class),
    // rootnode(RootNodeImpl.class),
    gltfnode(GLTFNode.class);

        public final Class<? extends Node> theClass;

        private NodeTypes(Class<? extends Node> theClass) {
            this.theClass = theClass;
            RootNodeImpl r = null;
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
    /**
     * The childnodes shall always be processed/rendered in the order they are defined.
     * This makes it possible to treat the children as a list that is rendered in a set order.
     * Rearranging the children will alter the render order.
     */
    @SerializedName(CHILDREN)
    protected ArrayList<Node> children = new ArrayList<Node>();

    @SerializedName(Bounds.BOUNDS)
    protected Bounds bounds;

    @SerializedName(STATE)
    protected State state = State.ON;

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
     * Returns the parent of this node, or null if this is the root
     * 
     * @return
     */
    public Node getParent() {
        return parent;
    }

    @Override
    public RenderableNode<?> getParentView() {
        Node parent = getParent();
        if (parent != null && parent instanceof RenderableNode<?>) {
            if (((RenderableNode<?>) parent).getViewFrustum() != null) {
                return (RenderableNode<?>) parent;
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
     * Call {@link #createTransient()} to set transient values
     * 
     * @param source
     * @throws ClassCastException If source node is not same class as this.
     */
    protected void set(AbstractNode source) {
        super.set(source);
        type = source.getType();
        state = source.state;
        this.pass = source.pass;
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

    @Override
    public <T extends Node> T getNodeById(String id, Class<T> type) {
        if (id.equals(getId())) {
            return (T) this;
        }
        for (Node child : getChildren()) {
            T result = child.getNodeById(id, type);
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

    @Override
    public <T extends Node> T getNodeByType(String name, Class<T> type) {
        if (name.equals(this.type)) {
            return (T) this;
        }
        for (Node child : getChildren()) {
            T result = child.getNodeByType(name, type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Node '" + getId() + "', " + children.size() + " children, pass=" + pass
                + ", state=" + state + (bounds != null ? ", has bounds" : "");
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

    @Override
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    /**
     * Look for ViewFrustum in parents nodes, stopping when ViewFrustum is found or when at root.
     * 
     * @return ViewFrustom from a parent node, or null if not defined.
     */
    protected ViewFrustum getParentsView() {
        RenderableNode<?> viewparent = getParentView();
        return viewparent != null ? viewparent.getViewFrustum() : null;
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

    @Override
    public void destroy(NucleusRenderer renderer) {
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
    public Pass getPass() {
        return pass;
    }

    @Override
    public void setPass(Pass pass) {
        this.pass = pass;
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public void onCreated() {
    }

}
