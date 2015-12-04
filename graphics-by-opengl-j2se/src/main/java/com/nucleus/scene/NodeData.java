package com.nucleus.scene;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.vecmath.Transform;

public class NodeData {

    private String id;
    private Transform transform;
    private String type;
    private String reference;
    private NodeData[] children;
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
     * Returns the children to this node, or null if no children.
     * 
     * @return
     */
    public NodeData[] getChildren() {
        return children;
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

    public void setChildren(NodeData[] children) {
        this.children = children;
    }

    public void setViewFrustum(ViewFrustum viewFrustum) {
        this.viewFrustum = viewFrustum;
    }

}
