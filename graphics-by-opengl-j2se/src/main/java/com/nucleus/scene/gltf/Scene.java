package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

/**
 * The Scene as it is loaded using the glTF format.
 * 
 * scene
 * The root nodes of a scene.
 * 
 * Properties
 * 
 * Type Description Required
 * nodes integer [1-*] The indices of each root node. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public class Scene extends GLTFNamedValue implements RuntimeResolver {

    private static final String NODES = "nodes";

    @SerializedName(NODES)
    private int[] nodes;

    transient Node[] sceneNodes;

    /**
     * returns the nodes, as indexes, that make up this scene
     */
    public int[] getNodeIndexes() {
        return nodes;
    }

    /**
     * Returns the nodes that make up this scene
     * 
     * @return
     */
    public Node[] getNodes() {
        return sceneNodes;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        if (nodes != null && nodes.length > 0) {
            Node[] sources = asset.getNodes();
            sceneNodes = new Node[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                sceneNodes[i] = sources[i];
            }
        }
    }

}
