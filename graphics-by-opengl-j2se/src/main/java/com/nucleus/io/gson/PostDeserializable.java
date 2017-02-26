package com.nucleus.io.gson;

/**
 * Interface for objects that can be post processed, ie they need to have some functionallity run AFTER json
 * deserialization
 *
 */
public interface PostDeserializable {

    /**
     * Method to be called after json deserialization, do what is needed to init object after deserialization.
     */
    public void postDeserialize();
}
