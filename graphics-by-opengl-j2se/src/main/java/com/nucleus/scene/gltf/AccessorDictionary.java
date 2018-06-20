package com.nucleus.scene.gltf;

import java.util.HashMap;

/**
 * Used to map from declared attributes to Accessor - use this to map from named attribute variables to Accessor.
 *
 */
public class AttributeDictionary {

    private HashMap<String, Accessor> attributes = new HashMap();

    /**
     * Adds a named accessor, mapping the name to an Accessor
     * 
     * @param name
     * @param accessor
     */
    public void addAttribute(String name, Accessor accessor) {
        attributes.put(name, accessor);
    }

    /**
     * Returns the Accessor for the named attribute that has been added with a call to
     * {@link #addAttribute(String, Accessor)}
     * 
     * @param name
     * @return
     */
    public Accessor get(String name) {
        return attributes.get(name);
    }

}
