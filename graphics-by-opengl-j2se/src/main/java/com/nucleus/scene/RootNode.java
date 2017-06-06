package com.nucleus.scene;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ResourcesData;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.texturing.Texture2D;

/**
 * Starting point of a nodetree, the root has a collection of nodes the each represent a scene.
 * There shall only be one rootnode at any given time, the root node defines the possible resource that may be
 * needed for the tree.
 * A root node shall be self contained, reference textures and large data sets.
 * This class can be serialized using GSON
 * All childnodes added must have the same RootNode reference
 * 
 * @author Richard Sahlin
 *
 */
public abstract class RootNode {

    /**
     * Pre defined ids that can be used for scenes and make it more convenient find a scene.
     * 
     */
    public enum Scenes {
        credit(),
        select(),
        settings(),
        game(),
        about(),
    }

    @SerializedName("scene")
    private Node scene;

    /**
     * Sets the root scene node, this rootnode will be parent to scene.
     * 
     * @param node
     */
    public void setScene(Node scene) {
        this.scene = scene;
        scene.parent = null;
    }

    /**
     * Creates a new instance of RootNode, implement in RootNode subclasses to return the implementation instance.
     * 
     * @return A new instance of RootNode implementation
     */
    public abstract RootNode createInstance();

    public Node getScene() {
        return scene;
    }

    /**
     * Returns the resources in this scene, this is the referenced objects that are used to make up the nodes.
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
        System.out.println("texture: " + textures.getClass().getSimpleName() + ", size: " + textures.length);
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

    /**
     * Returns the first matching viewnode, this is a conveniance method to find node with view
     * 
     * @param layer Which layer the ViewNode to return belongs to.
     * @return The viewnode or null if not found
     */
    public LayerNode getViewNode(Layer layer) {
        if (scene == null) {
            return null;
        }
        return getViewNode(layer, scene);
    }

    private LayerNode getViewNode(Layer layer, Node node) {
        return getViewNode(layer, node.getChildren());
    }

    private LayerNode getViewNode(Layer layer, ArrayList<Node> children) {
        for (Node n : children) {
            if (n.getType().equals(NodeType.layernode.name())) {
                if (((LayerNode) n).getLayer() == layer) {
                    return (LayerNode) n;
                }
            }
        }
        // Search through children recusively
        for (Node n : children) {
            LayerNode view = getViewNode(layer, n);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

}
