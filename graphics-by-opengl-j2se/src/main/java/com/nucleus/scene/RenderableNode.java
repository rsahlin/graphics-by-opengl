package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.renderer.NucleusRenderer.NodeRenderer;

/**
 * 
 *
 * @param <T> The mesh type
 */
public interface RenderableNode<T> {

    /**
     * Retuns the meshes for this node, current meshes are copied into the list
     * 
     * @return List of added meshes
     */
    public ArrayList<T> getMeshes(ArrayList<T> list);

    /**
     * Returns the instance of node renderer to be used to render this node instance.
     * Default behavior is to create in Node {@link #onCreated()} method if the node renderer is not already set.
     * 
     * @return Node renderer to use for this node instance
     */
    public NodeRenderer<?> getNodeRenderer();

}
