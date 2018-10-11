package com.nucleus.io.gson;

/**
 * Return the type class of the rootnode implementation.
 *
 */
public interface NucleusRootDeserializer<T> extends NucleusDeserializer<T> {

    /**
     * Returns the type class of the rootnode implementation, this is used to allow changing tree implementation.
     */
    public java.lang.reflect.Type getRootNodeTypeClass();

}
