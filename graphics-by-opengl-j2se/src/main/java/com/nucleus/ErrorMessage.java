package com.nucleus;

/**
 * Common enum for error messages
 * 
 * @author Richard Sahlin
 *
 */
public enum ErrorMessage {

    /**
     * Not implemented
     */
    NOT_IMPLEMENTED("Not implemented: "),
    /**
     * An object is of wrong type/class, some other parameter type was expected.
     */
    INVALID_TYPE("Invalid type: "),
    /**
     * A value/object is invalid format.
     */
    INVALID_FORMAT("Invalid format: ");

    /**
     * The error message
     */
    public final String message;

    private ErrorMessage(String message) {
        this.message = message;
    }

}
