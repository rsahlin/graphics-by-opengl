package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

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
public class Scene {

    private static final String NODES = "nodes";
    private static final String NAME = "name";

    @SerializedName(NODES)
    private int[] nodes;
    @SerializedName(NAME)
    private String name;

    /**
     * returns the nodes that make up this scene
     */
    public int[] getNodes() {
        return nodes;
    }

    /**
     * Returns the name of this scene or null if not defined.
     * @return
     */
    public String getName() {
        return name;
    }

}
