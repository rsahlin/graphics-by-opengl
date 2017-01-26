package com.nucleus.io;

import com.google.gson.annotations.SerializedName;
import com.nucleus.exporter.Reference;

/**
 * Base reference implementation
 * This is the default implementation for objects that can be referenced using an id.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class BaseReference implements Reference {

    @SerializedName("id")
    private String id;

    @SerializedName("externalReference")
    private ExternalReference externalReference = new ExternalReference("");

    /**
     * Default constructor
     */
    public BaseReference() {
    }

    /**
     * Creates a new base reference from the specified source, this will copy values
     * 
     * @param source
     */
    public BaseReference(BaseReference source) {
        set(source);
    }

    /**
     * Creates a new base reference with the specified id
     * 
     * @param id
     */
    public BaseReference(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the id from the source reference, this will copy the id.
     * If source is null nothing is done
     * 
     * @param source The source reference to copy
     */
    public void set(Reference source) {
        if (source != null) {
            setId(source.getId());
        }
        setExternalReference(source.getExternalReference());
    }

    /**
     * Returns the external reference for this texture
     * 
     * @return
     */
    @Override
    public ExternalReference getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the external reference for this texture
     * 
     * @param ref
     */
    @Override
    public void setExternalReference(ExternalReference ref) {
        externalReference = ref;
    }

}
