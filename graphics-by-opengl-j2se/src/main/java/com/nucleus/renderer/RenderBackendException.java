package com.nucleus.renderer;

/**
 * If an error is raised or found in whilst processing render backend calls, this is related to texture or buffer object
 * creation, compiling/linking of programs or rendering.
 *
 */
public class RenderBackendException extends Throwable {

    /**
     * Creates a new render backend exception with the specified message
     * 
     * @param message
     */
    public RenderBackendException(String message) {
        super(message);
    }

}
