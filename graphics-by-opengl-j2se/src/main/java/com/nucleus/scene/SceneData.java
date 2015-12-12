package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.texturing.Texture2D;

/**
 * Container for Nodes and resource for a scene
 * The SceneData can have multiple main (root) nodes to be rendered.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class SceneData {

    @SerializedName("instanceNodes")
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

    /**
     * Adds a texture as a resource to the scene, implementations must handle how textures are added
     * 
     * @param texture
     */
    public abstract void addResource(Texture2D texture);

    /**
     * Adds the mesh as s resource to the scene, implementations must handle how meshes are added
     * 
     * @param mesh
     */
    public abstract void addResource(Mesh mesh);

}
