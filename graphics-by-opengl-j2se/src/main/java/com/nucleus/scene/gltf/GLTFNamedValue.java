package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for GLTF named value that is serialized from JSON
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class GLTFNamedValue {

    private static final String NAME = "name";

    @SerializedName(NAME)
    protected String name;

    /**
     * Returns the user-defined name of this object or null if not specified.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

}
