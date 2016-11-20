package com.nucleus.exporter;

import com.nucleus.io.ExternalReference;

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
     * Returns the unique ID of the object, or null if not set.
     * 
     * @return The unique ID, this does not have to be globally unique, it shall be unique from a scene and context
     * perspective. Or null if not set.
     */
    public String getId();

    /**
     * Sets the object id, this shall be context unique but does not have to be globally unique.
     * It is up to the implementations to check if mulitple objects with the same id exists and decide what to do.
     * 
     * @param id The id of the object
     */
    public void setId(String id);

    /**
     * Sets the external reference for this object, ie the data is contained in a separate file
     * 
     * @param externalReference
     */
    public void setExternalReference(ExternalReference externalReference);

    /**
     * Returns the external reference for this object, or null if not set.
     * 
     * @return
     */
    public ExternalReference getExternalReference();
}
