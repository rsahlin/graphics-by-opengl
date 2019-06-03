package com.nucleus.renderer;

import com.nucleus.Backend;
import com.nucleus.opengl.GLESBaseRenderer;
import com.nucleus.vulkan.VulkanBaseRenderer;

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
     * Creates a new nucleus renderer for the backend
     * 
     * @param backend The API backend to create renderer for
     * @return New instance of nucleus renderer
     * @throws IllegalArgumentException If backend is null or an instance that is not supported
     */
    public static NucleusRenderer getRenderer(Backend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("GLESWrapper is null");
        }
        NucleusRenderer renderer = null;
        switch (backend.getVersion()) {
            case GLES20:
            case GLES30:
            case GLES31:
            case GLES32:
                return new GLESBaseRenderer(backend);
            case VULKAN11:
                return new VulkanBaseRenderer(backend);
            default:
                throw new IllegalArgumentException("Not implemented for " + backend.getVersion());
        }
    }
}
