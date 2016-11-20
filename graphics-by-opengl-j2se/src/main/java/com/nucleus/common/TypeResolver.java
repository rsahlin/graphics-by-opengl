package com.nucleus.common;

/**
 * Used to find implementing class, use with eg enum
 * 
 * @author Richard Sahlin
 * @param <T>
 *
 */
public interface TypeResolver<T> {

    /**
     * Returns the Class of the type
     * @return
     */
    public Class<T> getTypeClass();

}
