package com.nucleus.scene;

import com.nucleus.geometry.Mesh;
import com.nucleus.vecmath.Transform;

/**
 * Point of interest in a scene. Normally represents a visual object (vertices) that will be rendered.
 * 
 * @author Richard Sahlin
 *
 */
public class Node {

    final Transform transform = new Transform();
    /**
     * Optional projection for the node, this will affect all child nodes.
     */
    float[] projection;
    Mesh mesh;

    /**
     * Constructs a new Node with the specified mesh.
     * This node can be rendered.
     * 
     * @param mesh Containing the vertices, variables, program, textures to be rendered.
     */
    public Node(Mesh mesh) {
        this.mesh = mesh;
    }

    /**
     * Retuns the mesh for this node.
     * 
     * @return
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Sets the mesh to be rendered with this node
     * 
     * @param mesh
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
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
     * Fetches the projection matrix, if set.
     * 
     * @return Projection matrix for this node and childnodes, or null
     */
    public float[] getProjection() {
        return projection;
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

}
