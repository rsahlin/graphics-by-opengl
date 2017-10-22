package com.nucleus.io;

import java.io.InputStream;

import com.google.gson.annotations.SerializedName;
import com.nucleus.renderer.RenderPass;

/**
 * An external reference that is also a Reference within the node tree.
 * This is for assets that are loaded, for instance the source image for textures.
 * This class may be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class ExternalReference {

    public static final String SOURCE = "source";
    /**
     * Append external reference with this to flag that it is a reference to an already existing ID within the scene.
     * For instance used to locate dynamic textures created using {@link RenderPass}
     */
    public static final String ID_LOOKUP = "@";
    
    /**
     * Name of the source for this external reference, for instance the name of an image for a texture.
     */
    @SerializedName(SOURCE)
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

    /**
     * Returns true if this ref is an id lookup
     * @return
     */
    public boolean isIdReference() {
        return source != null && source.startsWith(ID_LOOKUP);
    }
    
    /**
     * Returns the id reference if this is an id lookup, otherwise null
     * @return The id reference or null if this is not an id lookup ref.
     */
    public String getIdReference() {
        if (isIdReference()) {
            return source.substring(ID_LOOKUP.length());
        }
        return null;
    }
    
}
