package com.nucleus.io;

import java.io.InputStream;

import com.google.gson.annotations.SerializedName;

/**
 * An external reference that is also a Reference within the node tree.
 * This is for assets that are loaded, for instance the source image for textures.
 * This class may be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class ExternalReference {

    /**
     * Name of the source for this external reference, for instance the name of an image for a texture.
     */
    @SerializedName("source")
    private String source;

    /**
     * @param source Name of the external reference, for instance a file.
     */
    public ExternalReference(String source) {
        this.source = source;
    }

    /**
     * Returns the name of the source for this external reference, for instance the name of an image.
     * 
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the source as an InputStream
     * 
     * @return
     */
    public InputStream getAsStream() {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream(getSource());
    }

}
