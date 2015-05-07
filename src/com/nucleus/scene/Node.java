package com.nucleus.scene;

import java.util.ArrayList;

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
    ArrayList<Mesh> meshes = new ArrayList<Mesh>();
    ArrayList<Node> children = new ArrayList<Node>();

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
     * Retuns the meshes for this node.
     * 
     * @return List of added meshes
     */
    public ArrayList<Mesh> getMeshes() {
        return meshes;
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

    /**
     * Adds a child at the end of the list of children.
     * 
     * @param child The child to add to this node.
     */
    public void addChild(Node child) {
        children.add(child);
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

}
