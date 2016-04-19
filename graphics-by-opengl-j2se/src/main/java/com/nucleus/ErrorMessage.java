package com.nucleus;

/**
 * Common enum for errors
 * 
 * @author Richard Sahlin
 *
 */
public enum Error {

    /**
     * Not implemented
     */
    NOT_IMPLEMENTED("Not implemented: "),
    /**
     * An object is of wrong type/class, some other parameter type was expected.
     */
    INVALID_TYPE("Invalid type: ");

    /**
     * The error message
     */
    public final String message;

    private Error(String message) {
        this.message = message;
    }

}
