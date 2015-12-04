package com.nucleus.scene;

/**
 * Container for Nodes and resource for a scene
 * The SceneData can have multiple main (root) nodes to be rendered.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class SceneData {

    Node instanceNodes;

    /**
     * Sets a node instance in this scene, this is a nodetree that can be rendered.
     * 
     * @param node
     */
    public void setInstanceNode(Node node) {
        instanceNodes = node;
    }

    /**
     * Returns the instance node for this scene, or null if none is set.
     * 
     * @return Instance node (tree) or null.
     */
    public Node getInstanceNode() {
        return instanceNodes;
    }

}
