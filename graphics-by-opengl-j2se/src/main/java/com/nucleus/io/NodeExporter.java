package com.nucleus.io;

import com.nucleus.scene.Node;
import com.nucleus.scene.SceneData;

/**
 * Methods for exporting nodes, this is used by scene factory implementations when exporting a scene.
 * This will not serialize the nodes but prepare and collect the necesary data so that the Node can be serialized.
 * 
 * @author Richard Sahlin
 *
 */
public interface NodeExporter {

    /**
     * Take the source node and returns a Node that can be exported.
     * Any data needed by the node must be put in sceneData or within the source tree
     * Implementations of this method may call the {@link #exportObject(Object, SceneData)} method to export
     * data within the node.
     * 
     * @param source
     * @param sceneData
     */
    public Node exportNode(Node source, SceneData sceneData);

    /**
     * Exports the object, this shall collect any data needed to export the specific object.
     * For instance used for texture objects.
     * 
     * @param object
     * @param sceneData
     */
    public void exportObject(Object object, SceneData sceneData);
}
