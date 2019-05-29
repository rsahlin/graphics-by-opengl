package com.nucleus;

/**
 * If an error is raised or found in whilst processing render backend calls, this is related to texture or buffer object
 * creation, compiling/linking of programs or rendering.
 *
 */
public class BackendException extends Throwable {

    /**
     * Creates a new render backend exception with the specified message
     * 
     * @param message
     */
    public BackendException(String message) {
        super(message);
    }

}
