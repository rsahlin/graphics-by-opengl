package com.nucleus.scene;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ResourcesData;
import com.nucleus.texturing.Texture2D;

/**
 * Container for serialized Nodes and resource for a scene
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
     * Returns the resources in this scene, this is the objects that are used to make up the nodes.
     * 
     * @return
     */
    public abstract ResourcesData getResources();

    /**
     * Adds the mesh as s resource to the scene, implementations must handle how meshes are added
     * 
     * @param mesh
     */
    public void addResource(Mesh mesh) {
        addResource(mesh.getTextures());
    }

    /**
     * Adds textures as a resource to the scene
     * 
     * @param textures
     */
    public void addResource(Texture2D[] textures) {
        System.out.println("texture: " + textures.getClass().getSimpleName());
        getResources().addTextures(textures);
    }

    /**
     * Adds the texture as a resource to the scene.
     * 
     * @param texture
     */
    public void addResource(Texture2D texture) {
        System.out.println("texture: " + texture.getClass().getSimpleName());
        getResources().addTexture(texture);
    }

}
