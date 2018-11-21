package com.nucleus.scene.gltf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Used to map Accessors - use this to map from keys using generics, for instance to store Accessors using shader
 * variable name.
 *
 */
public class AccessorDictionary<T> {

    private HashMap<T, Accessor> toAccessors = new HashMap<>();

    /**
     * Adds a named accessor, putting the accessor using key.
     * 
     * @param key The key to map the accessor to
     * @param accessor
     */
    public void add(T key, Accessor accessor) {
        toAccessors.put(key, accessor);
    }

    /**
     * Returns the Accessor for key that has been added with a call to
     * {@link #add(T, Accessor)}
     * 
     * @param key
     * @return The Accessor or null if not matching any name keys added by calling
     * {@link #add(T, Accessor)}
     */
    public Accessor get(T key) {
        return toAccessors.get(key);
    }

    /**
     * Copies keys into a list and returns it.
     * 
     * @return
     */
    public ArrayList<T> getKeys() {
        ArrayList<T> keys = new ArrayList<>();
        Set<T> set = toAccessors.keySet();
        for (T t : set) {
            keys.add(t);
        }
        return keys;
    }

}
