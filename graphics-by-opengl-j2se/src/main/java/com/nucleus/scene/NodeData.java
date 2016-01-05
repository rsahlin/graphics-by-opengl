package com.nucleus.scene;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.vecmath.Transform;

/**
 * 
 * Node data for a serializable node, this node is used when importing and exporting.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class NodeData {

    @SerializedName("id")
    private String id;
    @SerializedName("transform")
    private Transform transform;
    @SerializedName("type")
    private String type;
    @SerializedName("reference")
    private String reference;
    @SerializedName("children")
    private ArrayList<NodeData> children = new ArrayList<NodeData>();
    @SerializedName("viewFrustum")
    private ViewFrustum viewFrustum;

    /**
     * Default constructor
     */
    public NodeData() {

    }

    /**
     * 
     * @param id The id of the node
     */
    public NodeData(String id) {
        this.id = id;
    }

    /**
     * Creates a copy of the node for export
     * This will NOT copy the children
     * 
     * @param source
     */
    public NodeData(Node source) {
        set(source);
    }

    /**
     * Sets this node to the the source that can be referenced
     * 
     * @param source
     */
    public void toReference(Node source) {
        this.id = source.getReference();
        if (source.getTransform() != null) {
            this.transform = new Transform(source.getTransform());
        }
        if (source.getViewFrustum() != null) {
            this.viewFrustum = new ViewFrustum(source.getViewFrustum());
        }
    }

    /**
     * Sets the data in this class from the Node source
     * This will NOT copy the children
     * 
     * @param source The node source to set data from, child nodes will not be set
     */
    public void set(Node source) {
        this.id = source.getId();
        this.reference = source.getReference();
        this.type = source.getType();
        if (source.getTransform() != null) {
            this.transform = new Transform(source.getTransform());
        }
        if (source.getViewFrustum() != null) {
            this.viewFrustum = new ViewFrustum(source.getViewFrustum());
        }
    }

    /**
     * Returns the id of this node, or null if not set
     * 
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the transform for this node
     * This may not be defined.
     * 
     * @return
     */
    public Transform getTransform() {
        return transform;
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
     * Returns a reference to the data for the type, this may not be defined
     * 
     * @return
     */
    public String getReference() {
        return reference;
    }

    /**
     * Returns the viewfrustum if defined.
     * 
     * @return View frustum or null
     */
    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Sets the viewfrustum as a reference
     * 
     * @param viewFrustum
     */
    public void setViewFrustum(ViewFrustum viewFrustum) {
        this.viewFrustum = viewFrustum;
    }

}
