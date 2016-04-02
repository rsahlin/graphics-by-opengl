package com.nucleus.scene;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ResourcesData;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.vecmath.Transform;

/**
 * Starting point of a nodetree, the root has a collection of nodes the each represent a scene.
 * This class can be serialized using GSON
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

    @SerializedName("scenes")
    ArrayList<LayerNode> scenes;

    /**
     * The current view location, this is translated into the view transform that is set into
     * the renderer.
     * Use this to move the location of the view.
     */
    @SerializedName("view")
    private Transform view;

    /**
     * The current layer to be processed, or null to process all.
     */
    transient private Layer layer;

    /**
     * Adds a node instance in this scene, this is a nodetree that can be rendered.
     * 
     * @param node
     */
    public void addScene(LayerNode node) {
        if (scenes == null) {
            scenes = new ArrayList<LayerNode>();
        }
        scenes.add(node);
    }

    /**
     * Returns the base scene node with matching id, or null if none is set.
     * This will only check base nodes (scenes), it will not recurse into children.
     * 
     * @id Id of scene node to return
     * @return scene node (tree) or null.
     */
    public LayerNode getScene(String id) {
        for (LayerNode n : scenes) {
            if (n.getId().equals(id)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Returns the base scene node with matching id, or null if none is set.
     * This will only check base nodes (scenes), it will not recurse into children.
     * 
     * @id scene The scene node to return
     * @return scene node (tree) or null.
     */
    public LayerNode getScene(Scenes scene) {
        return getScene(scene.name());
    }

    /**
     * Returns all scene nodes in the root.
     * 
     * @return
     */
    public ArrayList<LayerNode> getScenes() {
        return scenes;
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
     * Sets the layer to be processed and rendered.
     * Set to null to process and render all layers
     * 
     * @param layer
     */
    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    /**
     * Returns the current layer to process
     * 
     * @return The layer to process and render, or null for all layers.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Returns the node for the specified layer. Take care when updating this in order not to break ongoing rendering.
     * This will return the node added with a call to {@link #setNode(LayerNode)}
     * 
     * @param layer the layer to return the node for
     * @return The node at the specified layer, or null
     */
    public Node getNode(Layer layer) {
        for (LayerNode node : getScenes()) {
            if (node.getLayer() == layer) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the view transform
     * 
     * @return The view transform, or null if not set
     */
    public Transform getView() {
        return view;
    }

    /**
     * Sets the view transform for this rootnode, this is normally done by defining it in the scene.
     * 
     * @param view
     */
    public void setView(Transform view) {
        this.view = view;
    }

}
