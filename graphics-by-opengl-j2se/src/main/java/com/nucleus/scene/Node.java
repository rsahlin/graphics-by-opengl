package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.nucleus.bounds.Bounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.AttributeUpdater.Producer;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.MMIPointerEvent.Action;
import com.nucleus.properties.EventManager;
import com.nucleus.properties.EventManager.EventHandler;
import com.nucleus.properties.Property;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Transform;

/**
 * Point of interest in a scene. Normally represents a visual object (vertices) that will be rendered.
 * This shall be a 'dumb' node in that sense that it shall not contain logic or behavior other than the ability to
 * be rendered and serailized.
 * This class may be serialized using GSON
 * Before the node can be rendered one or more meshes must be added using {@link #addMesh(Mesh)}
 * 
 * If a node contains properties the {@linkplain EventManager#sendObjectEvent(Object, String, String)} is called
 * with the property/key and this class as object.
 * 
 * @author Richard Sahlin
 *
 */
public class Node extends BaseReference implements MMIEventListener {

    public static final String STATE = "state";
    public static final String TYPE = "type";

    /**
     * The states a node can be in, this controlls if node is rendered etc.
     * This can be used to skip nodes from being rendered or processed.
     * 
     * @author Richard Sahlin
     *
     */
    public enum State {

        /**
         * Node is on, rendered and actors processed
         */
        ON(1),
        /**
         * Node is off, not rendered and no actors processed
         */
        OFF(2),
        /**
         * Node is rendered, but no actors processed
         */
        RENDER(3),
        /**
         * Node is not rendered, but actors processed
         */
        ACTOR(4);

        private final int value;

        private State(int value) {
            this.value = value;
        }

    }

    @SerializedName(TYPE)
    private String type;
    @SerializedName("transform")
    protected Transform transform;
    @SerializedName("viewFrustum")
    private ViewFrustum viewFrustum;
    /**
     * The childnodes shall always be processed/rendered in the order they are defined.
     * This makes it possible to treat the children as a list that is rendered in a set order.
     * Rearranging the children will alter the render order.
     */
    @SerializedName("children")
    protected ArrayList<Node> children = new ArrayList<Node>();

    @SerializedName("bounds")
    private Bounds bounds;

    @SerializedName("material")
    private Material material;

    @SerializedName(STATE)
    private State state = State.ON;

    /**
     * Reference to texture, used when importing / exporting.
     * No runtime meaning
     */
    @SerializedName("textureRef")
    private ExternalReference textureRef;

    /**
     * Properties for this node
     */
    @SerializedName("properties")
    private Map<String, String> properties;

    /**
     * Optional projection Matrix for the node, this will affect all child nodes.
     */
    transient float[] projection;
    /**
     * The node concatenated model matrix at time of render, this is set when the node is rendered.
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     */
    transient float[] modelMatrix = Matrix.createMatrix();
    transient ArrayList<Mesh> meshes = new ArrayList<Mesh>();
    /**
     * Optional AttributeUpdate producer, used for instance by spritemesh nodes
     */
    transient Producer attributeProducer;

    /**
     * The parent node, this shall be set when node is added as child
     */
    transient Node parent;
    /**
     * The root node
     */
    transient protected RootNode rootNode;

    /**
     * Creates an empty node, add children and meshes as needed.
     */
    public Node() {
    }

    /**
     * Creates an empty node with unique (for the scene) Id.
     * The uniqueness of the id is NOT checked.
     * 
     * @param id
     */
    public Node(String id) {
        setId(id);
    }

    /**
     * Creates a new instance of this node.
     * This will be a new empty instance
     * 
     * @return New instance of this node
     * @throws IllegalArgumentException If root is null
     */
    public Node createInstance() {
        Node copy = new Node();
        return copy;
    }

    /**
     * Creates a new instance of this node, then copies this node into the copy.
     * This is a shallow copy, it does not copy children.
     * 
     * @return New copy of this node, transient values and children will not be copied.
     */
    public Node copy() {
        Node copy = createInstance();
        copy.set(this);
        return copy;
    }

    /**
     * Constructs a new Node with the specified mesh.
     * This node can be rendered.
     * 
     * @param mesh Containing the vertices, variables, program, textures to be rendered.
     */
    public Node(Mesh mesh) {
        meshes.add(mesh);
    }

    /**
     * Creates the transient values needed in runtime - implement in subclasses
     */
    public void create() {
    }

    /**
     * Retuns the meshes for this node.
     * 
     * @return List of added meshes
     */
    public ArrayList<Mesh> getMeshes() {
        return meshes;
    }

    /**
     * Returns the attribute producer, or null if not set
     * 
     * @return
     */
    public Producer getAttributeProducer() {
        return attributeProducer;
    }

    /**
     * Sets the attribute producer, this is used by nodes where the attribute data needs to be updated before rendering.
     * The producer will be called by the renderer before the mesh is rendered, but after the completion of the
     * previous frame.
     * 
     * @param producer
     */
    public void setAttributeProducer(Producer producer) {
        this.attributeProducer = producer;
    }

    /**
     * Adds a mesh to be rendered with this node.
     * 
     * @param mesh
     */
    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
    }

    /**
     * Removes the mesh from this node, if present.
     * If many meshes are added this method may have a performance impact.
     * 
     * @param mesh The mesh to remove from this Node.
     */
    public void removeMesh(Mesh mesh) {
        meshes.remove(mesh);
    }

    /**
     * Returns the transform for this node.
     * 
     * @return
     */
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
    public void copyMaterial(Node source) {
        if (source.material != null) {
            copyMaterial(source.material);
        }
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
     * Copies the material into this node
     * 
     * @param source
     */
    public void copyMaterial(Material source) {
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
            setTransform(null);
        }

    }

    /**
     * Sets the source transform as a referrence.
     * 
     * @param source The transform reference, may be null.
     */
    public void setTransform(Transform source) {
        this.transform = source;
    }

    /**
     * Sets the material reference.
     * 
     * @param source
     */
    public void setMaterial(Material source) {
        this.material = source;
    }

    /**
     * Fetches the projection matrix, if set.
     * 
     * @return Projection matrix for this node and childnodes, or null
     */
    public float[] getProjection() {
        return projection;
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
     * Returns the first (closest from this node) {@linkplain ViewNode} parent.
     * The search starts with the parent node of this node, if that is not a {@linkplain ViewNode} that nodes parent
     * is checked, continuing until root node.
     * 
     * @return The view parent of this node, or null if none could be found
     */
    public ViewNode getViewParent() {
        if (parent == null) {
            return null;
        }
        if (NodeType.viewnode.name().equals(parent.getType())) {
            return (ViewNode) parent;
        }
        return parent.getViewParent();
    }

    /**
     * Returns the root node for this node, this is the document root.
     * 
     * @return The document root.
     */
    public RootNode getRootNode() {
        return rootNode;
    }

    /**
     * Adds a child at the end of the list of children.
     * The child node's parent will be set to this node.
     * 
     * @param child The child to add to this node.
     */
    public void addChild(Node child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * Removes the child from this node if it is present.
     * 
     * @param child The child to remove from this node.
     * @return True if the child was present in the list of children.
     */
    public boolean removeChild(Node child) {
        return children.remove(child);
    }

    /**
     * Returns the list of children for this node.
     * Any modifications done to the returned list will be reflected here.
     * The childnodes shall always be processed/rendered in the order they are defined.
     * This makes it possible to treat the children as a list that is rendered in a set order.
     * Rearranging the children will alter the render order.
     * 
     * @return The list of children.
     */
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
    public void set(Node source) {
        super.set(source);
        type = source.type;
        textureRef = source.textureRef;
        state = source.state;
        copyTransform(source);
        copyViewFrustum(source);
        copyMaterial(source);
        copyBounds(source);
        setProperties(source);
    }

    /**
     * Sets the rootnode for this node, this shall normally not be changed
     * 
     * @param root
     */
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
    public void setProperties(Node source) {
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
     * Returns the properties for this node, or null if not set.
     * 
     * @return
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns the property for the specified key if set, or null.
     * 
     * @param key
     * @return The property value for the key, or null
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Returns the property for the key, if the key has no value then defaultValue is returned.
     * 
     * @param key
     * @param defaultValue
     * @return The property value for key, or defaultValue if not set.
     */
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

    public void copyTo(Node target) {
        target.set(this);
    }

    /**
     * Returns node with matching id, searching through this node and recursively searching through children.
     * Children will be searched by calling {@link #getChildren()} excluding nodes that are switched off.
     * 
     * @param id Id of node to return
     * @return First instance of node with matching id, or null if none found
     */
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
     * Returns the first node with matching type, or null if none found.
     * This method will search through the active children.
     * 
     * @param type
     * @return
     */
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
     * Returns the child node with matching id from this node, children are not searched recursively.
     * TODO Shall this method call getChildren() which will return only on-switched nodes?
     * 
     * @param id
     * @return The child from this node with matching id, or null if not found.
     */
    public Node getChildById(String id) {
        for (Node n : children) {
            if (n.getId().equals(id)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Returns the mesh by the given id from this Node, if a mesh with matching id is not present in the list of meshes
     * then null is returned.
     * 
     * @param id
     * @return The mesh with matching id or null
     */
    public Mesh getMeshById(String id) {
        for (Mesh m : meshes) {
            if (id.equals(m.getId())) {
                return m;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Node '" + getId() + "', " + meshes.size() + " meshes, " + children.size() + " children";
    }

    /**
     * Returns the type of node, this is a String representation that must be understood by the implementation
     * This may not be defined.
     * 
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the bounds for this node if set, otherwise null
     * 
     * @return
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Returns the state of the node, the specifies if the node is on or off.
     * 
     * @return The state, or null if not set
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the state of this node
     * 
     * @param state
     */
    public void setState(State state) {
        this.state = state;
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
     * Returns the viewfrustum if defined.
     * 
     * @return View frustum or null
     */
    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    /**
     * Sets the viewfrustum as a reference to the specified source
     * Note this will reference the source {@link ViewFrustum} any changes will be reflected here
     * The viewfrustum matrix will be set in the projection for this node, call {@link #getProjection()} to
     * get the matrix
     * 
     * @param source The frustum reference
     */
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
    public void copyViewFrustum(Node source) {
        if (source.viewFrustum != null) {
            copyViewFrustum(source.viewFrustum);
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
    public void copyBounds(Node source) {
        if (source.bounds != null) {
            copyBounds(source.bounds);
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
    public void setBounds(Bounds bounds) {
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
     * Called by factory method when node has been created, do not call childrens {@link #onCreated()} recursively from
     * this method.
     * Implement in subclasses to perform actions when the node has been created, this will be called after all children
     * of this node has been created.
     */
    public void onCreated() {
        setObjectProperties();
    }

    /**
     * Internal method, sets all properties with a call for each property to
     * {@linkplain EventManager#sendObjectEvent(Object, String, String)} with the node as object
     * This shall be called from the {@link #onCreated()} method
     * 
     */
    private void setObjectProperties() {
        if (properties != null) {
            for (String key : properties.keySet()) {
                EventManager.getInstance().sendObjectEvent(this, key, properties.get(key));
            }
        }
    }

    @Override
    public void inputEvent(MMIPointerEvent event) {
        if (event.getAction() == Action.ACTIVE || event.getAction() == Action.MOVE) {
            checkNode(event);
        }
    }

    /**
     * Checks this node and children for pointer event.
     * 
     * @param event
     * @return True if there was an event that was inside a node, ie a 'hit'
     */
    protected boolean checkNode(MMIPointerEvent event) {
        if (bounds != null
                && getProperty(EventHandler.Type.POINTERINPUT.name(), EventManager.FALSE).equals(EventManager.TRUE)) {
            ViewNode viewNode = getViewParent();
            // If ViewNode parent does not exist the identitymatrix is used
            float[] mv = Matrix.createMatrix();
            if (viewNode != null) {
                // In order to do pointer intersections the model and view matrix is needed.
                // For this to work it is important that the view keeps the same orientation of axis as OpenGL (right
                // and up)
                Matrix.mul4(viewNode.getView().getMatrix(), modelMatrix, mv);
            } else {
                Matrix.setIdentity(mv, 0);
            }
            bounds.transform(mv, 0);
            if (bounds.isPointInside(event.getPointerData().getCurrentPosition(), 0)) {
                System.out.println("HIT");
                String onclick = getProperty("onclick");
                if (onclick != null) {
                    Property p = Property.create(onclick);
                    EventManager.getInstance().sendObjectEvent(this, p.getKey(), p.getValue());
                }
                return true;
            }
        }
        for (Node n : getChildren()) {
            if (n.checkNode(event)) {
                return true;
            }
        }
        return false;
    }

}
