package com.nucleus.renderer;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer.MatrixEngine;
import com.nucleus.texturing.ImageFactory;

/**
 * Creates an implementation of the nucleus renderer interface.
 * 
 * @author Richard Sahlin
 *
 */
public class RendererFactory {

    private final static String NOT_IMPLEMENTED_ERROR = "Not implemented support for: ";

    /**
     * The supported renderers
     * 
     * @author Richard Sahlin
     *
     */
    public enum Renderers {
        GLES20();

        private Renderers() {
        };
    }

    public static NucleusRenderer getRenderer(Renderers version, Object gles, ImageFactory imageFactory,
            MatrixEngine matrixEngine) {
        NucleusRenderer renderer = null;
        switch (version) {
        case GLES20:
            renderer = new BaseRenderer((GLES20Wrapper) gles, imageFactory, matrixEngine);
            break;
        default:
            throw new IllegalArgumentException(NOT_IMPLEMENTED_ERROR + version);
        }
        return renderer;
    }
}
