package com.nucleus.io;

/**
 * For objects that can be referenced, either in the Node-tree, or when serializing.
 * This is to make it more visible that an object is referenced when serializing and to allow storing of the reference
 * ID.
 * 
 * @author Richard Sahlin
 *
 */
public interface Reference {

    /**
     * Returns the unique ID of the object.
     * 
     * @return The unique ID, this does not have to be globally unique, it shall be unique from a scene and context
     * perspective.
     */
    public String getId();

    /**
     * Sets the object id, this shall be context unique but does not have to be globally unique.
     * 
     * @param id The id of the object
     */
    public void setId(String id);

}
