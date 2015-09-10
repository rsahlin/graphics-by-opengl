package com.nucleus.io;

/**
 * Base reference implementation
 * This is the default implementation for objects that can be referenced using an id.
 * 
 * @author Richard Sahlin
 *
 */
public class BaseReference implements Reference {

    private String id;

    /**
     * Default constructor
     */
    public BaseReference() {
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

}
