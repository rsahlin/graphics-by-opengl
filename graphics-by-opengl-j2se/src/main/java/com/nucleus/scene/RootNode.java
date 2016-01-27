package com.nucleus.scene;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ResourcesData;
import com.nucleus.texturing.Texture2D;

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
    ArrayList<Node> scenes;

    /**
     * Adds a node instance in this scene, this is a nodetree that can be rendered.
     * 
     * @param node
     */
    public void addScene(Node node) {
        if (scenes == null) {
            scenes = new ArrayList<Node>();
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
    public Node getScene(String id) {
        for (Node n : scenes) {
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
    public Node getScene(Scenes scene) {
        return getScene(scene.name());
    }

    /**
     * Returns all scene nodes in the root.
     * 
     * @return
     */
    public ArrayList<Node> getScenes() {
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

}
