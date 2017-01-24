package com.nucleus.common;

/**
 * Used to find implementing class, use with eg enum, this will create a binding between a type name and implementing
 * class.
 * Use this when there is a need to specify a class using a string, instead of serializing the classname (that may
 * change)
 * 
 * @author Richard Sahlin
 * @param <T>
 *
 */
public interface Type<T> {

    /**
     * Returns the Class of the type, ie the implementing class
     * 
     * @return
     */
    public Class<T> getTypeClass();

    /**
     * Returns the name of the type, use this for instance when serializing.
     * 
     * @return Name of the type, an identifier for this type.
     */
    public String getName();
}
