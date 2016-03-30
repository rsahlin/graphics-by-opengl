package com.nucleus.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.nucleus.bounds.Bounds;
import com.nucleus.bounds.BoundsFactory;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.AttributeUpdater.Producer;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.BaseReference;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.properties.PropertyManager;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Transform;

/**
 * Point of interest in a scene. Normally represents a visual object (vertices) that will be rendered.
 * This shall be a 'dumb' node in that sense that it shall not contain logic or behavior other than the ability to
 * be rendered and serailized.
 * This class may be serialized using GSON
 * Before the node can be rendered one or more meshes must be added using {@link #addMesh(Mesh)}
 * 
 * @author Richard Sahlin
 *
 */
public class Node extends BaseReference implements MMIEventListener {

    @SerializedName("type")
    private String type;
    @SerializedName("state")
    private NodeState state;
    @SerializedName("transform")
    protected Transform transform;
    @SerializedName("viewFrustum")
    private ViewFrustum viewFrustum;
    @SerializedName("children")
    ArrayList<Node> children = new ArrayList<Node>();

    @SerializedName("bounds")
    private Bounds bounds;

    /**
     * Reference to mesh, used when importing / exporting.
     * No runtime meaning
     */
    @SerializedName("meshRef")
    private String meshRef;

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
     * The node concatenated MVP matrix at time of render, this is set when the node is rendered.
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     */
    transient float[] mvp = Matrix.createMatrix();
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
     * This will be a new empty instance.
     * 
     * @return New instance of this node
     */
    public Node createInstance() {
        Node copy = new Node();
        copy.set(this);
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
     * Sets the attribute producer, this is for instance used by sprite nodes where the sprites have data that needs
     * to be updated to the mesh attributes.
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
     * Fetches the projection matrix, if set.
     * 
     * @return Projection matrix for this node and childnodes, or null
     */
    public float[] getProjection() {
        return projection;
    }

    /**
     * Returns the MVP matrix, this updated the the concatenated MVP matrix for the node when it is rendered.
     * DO NOT MODIFY THESE VALUES, CHANGES WILL BE OVERWRITTEN WHEN NODE IS RENDERED.
     * 
     * @return The concatenated MVP from last rendered frame, if Node is not rendered the MVP will not be updated.
     */
    public float[] getMVP() {
        return mvp;
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
     * Returns the parent of this node, or null if this is the root
     * 
     * @return
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Insert a child at the specified index
     * 
     * @param index
     * @param child
     * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index > size())
     */
    public void addChild(int index, Node child) {
        children.add(index, child);
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
     */
    public void set(Node source) {
        super.set(source);
        type = source.type;
        meshRef = source.meshRef;
        if (source.getTransform() != null) {
            if (transform == null) {
                transform = new Transform(source.getTransform());
            } else {
                transform.set(source.getTransform());
            }
        } else {
            transform = null;
        }
        if (source.viewFrustum != null) {
            setViewFrustum(new ViewFrustum(source.viewFrustum));
        }
        if (source.bounds != null) {
            this.bounds = BoundsFactory.create(source.bounds.getType(), source.bounds.getBounds());
        } else {
            this.bounds = null;
        }
        setProperties(source);
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

    public void copyTo(Node target) {
        target.set(this);
    }

    /**
     * Returns node with matching id, searching through this node and recursively searching through children.
     * 
     * @param id Id of node to return
     * @return First instance of node with matching id, or null if none found
     */
    public Node getNodeById(String id) {
        if (id.equals(getId())) {
            return this;
        }
        for (Node child : children) {
            Node result = child.getNodeById(id);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the first node with matching type, or null if none found
     * 
     * @param type
     * @return
     */
    public Node getNodeByType(String type) {
        if (type.equals(this.type)) {
            return this;
        }
        for (Node child : children) {
            Node result = child.getNodeByType(type);
            if (result != null) {
                return result;
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
    public NodeState getState() {
        return state;
    }


    /**
     * Returns the name of the mesh for this node, this is used when importing
     * 
     * @return
     */
    public String getMeshRef() {
        return meshRef;
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
     * Called by factory method when node has been created, do not call childrens {@link #onCreated()} recursively from
     * this method.
     * Implement in subclasses to perform actions when the node has been created, this will be called after all children
     * of this node has been created.
     */
    public void onCreated() {
        setObjectProperties();
    }

    /**
     * Internal method, sets all properties to the {@linkplain PropertyManager} with the node as object
     * 
     */
    private void setObjectProperties() {
        if (properties != null) {
            for (String key : properties.keySet()) {
                PropertyManager.getInstance().setObjectProperty(this, key, properties.get(key));
            }
        }
    }

    @Override
    public void inputEvent(MMIPointerEvent event) {
        System.out.println(event.getPointerData().getCurrentPosition()[0]);
        // Default behavior is to check if node has bounds, if it has the pointer event is checked against it.
        // Children are called recursively.
        if (bounds != null) {
            // bounds.isPointInside(event.getPointerData().getCurrentPosition(), 0);
        }
    }

}
