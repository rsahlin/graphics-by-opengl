package com.nucleus.io;

/**
 * An external reference that is also a Reference within the node tree.
 * This is for assets that are loaded, for instance the source image for textures.
 * 
 * @author Richard Sahlin
 *
 */
public class ExternalReference extends BaseReference {

    /**
     * Name of the source for this external reference, for instance the name of an image for a texture.
     */
    private String sourceName;

    /**
     * @param sourceName Name of the external reference, for instance a file.
     */
    public ExternalReference(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * Returns the name of the source for this external reference, for instance the name of an image.
     * 
     * @return
     */
    public String getSourceName() {
        return sourceName;
    }

}
