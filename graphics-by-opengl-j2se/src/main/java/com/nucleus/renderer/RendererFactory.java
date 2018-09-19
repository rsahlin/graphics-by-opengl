package com.nucleus.renderer;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;

/**
 * Creates an implementation of the nucleus renderer interface.
 * 
 * @author Richard Sahlin
 *
 */
public class RendererFactory {

    private final static String WRONG_GLES = "GLES is wrong class: ";
    private final static String NOT_IMPLEMENTED_ERROR = "Not implemented support for: ";

    /**
     * Creates a new nucleus renderer with the specified version.
     * Currently only supports GLES20
     * 
     * @param gles The GLESWrapper for the specified version, {@link GLES20Wrapper} for GLES20
     * @return New instance of nucleus renderer
     * @throws IllegalArgumentException If gles is not matching for the renderer version.
     */
    public static NucleusRenderer getRenderer(GLESWrapper gles) {
        NucleusRenderer renderer = null;
        if (!(gles instanceof GLES20Wrapper)) {
            throw new IllegalArgumentException(WRONG_GLES + gles.getClass().getName());
        }
        renderer = new BaseRenderer((GLES20Wrapper) gles);
        return renderer;
    }
}
