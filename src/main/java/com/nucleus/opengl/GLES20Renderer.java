package com.nucleus.opengl;

/**
 * Common rendering functions in a platform agnostic manner.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class GLES20Renderer {

    public final static String NULL_GLES_WRAPPER = "GLES20 Wrapper is null.";

    final GLES20Wrapper gles20;

    /**
     * Creates a new GLES20 compiler using the specified wrapper.
     * 
     * @param gles20
     * @throws IllegalArgumentException If gles20 is null.
     */
    protected GLES20Renderer(GLES20Wrapper gles20) {
        if (gles20 == null) {
            throw new IllegalArgumentException(NULL_GLES_WRAPPER);
        }
        this.gles20 = gles20;
    }

}
