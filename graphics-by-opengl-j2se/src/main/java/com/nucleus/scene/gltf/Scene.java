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
     * Not implemented
     */
    // private Object extensions;
    // private Object extras;
    public int[] getNodes() {
        return nodes;
    }

    public String getName() {
        return name;
    }

}
