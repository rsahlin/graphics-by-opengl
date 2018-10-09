package com.nucleus.io.gson;

/**
 * Return the type class of the rootnode implementation.
 *
 */
public abstract class NucleusRootDeserializer<T> extends NucleusDeserializer<T> {

    /**
     * Returns the type class of the rootnode implementation, this is used to allow changing tree implementation.
     */
    public abstract java.lang.reflect.Type getRootNodyTypeClass();

}
