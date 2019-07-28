package com.nucleus;

import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * The low level drawing API backend wrapper
 *
 */
public abstract class Backend {

    /**
     * Creates backend implementations, this is used to create the render api implementation.
     *
     */
    public interface BackendFactory {
        /**
         * Creates the backend instance
         * 
         * @param version
         * @param window Optional window
         * @param context Optional context
         * @return
         */
        public Backend createBackend(Renderers version, Object window, Object context);
    }

    protected static Backend backend;

    public Backend(Renderers version) {
        if (version == null) {
            throw new IllegalArgumentException("Renderer version is null");
        }
        this.version = version;
        createInstance(version);
    }

    private void createInstance(Renderers version) {
        if (backend == null) {
            backend = this;
        }
    }

    /**
     * Returns the singleton backend instance - this can be used to check what version/type of backend that is used.
     * This is created when the GLES/GL/Vulkan wrapper is created.
     * 
     * TODO Maybe this method shall be protected/hidden so that only relevant packeges can access it?
     * 
     * @return The backend that is running - or null if not created, ie the current render wrapper is not created yet
     */
    public static Backend getInstance() {
        return backend;
    }

    private final Renderers version;

    /**
     * The drawmodes
     *
     */
    public enum DrawMode {
        POINTS(),
        LINE_STRIP(),
        LINE_LOOP(),
        TRIANGLE_STRIP(),
        TRIANGLE_FAN(),
        TRIANGLES(),
        LINES();

        /**
         * Returns the number of primitives for the number of indices with the current mode
         * 
         * @param indices
         * @return
         */
        public int getPrimitiveCount(int indices) {
            switch (this) {
                case LINE_LOOP:
                    return indices;
                case LINE_STRIP:
                    return indices - 1;
                case LINES:
                    return indices << 1;
                case POINTS:
                    return indices;
                case TRIANGLE_FAN:
                    return indices - 2;
                case TRIANGLE_STRIP:
                    return indices - 2;
                case TRIANGLES:
                    return indices / 3;
                default:
                    throw new IllegalArgumentException("Invalid mode " + this);
            }
        }
    }

    /**
     * Returns the version of the backend
     * 
     * @return
     */
    public Renderers getVersion() {
        return version;
    }

    /**
     * Destroys the backend instance - call this when application exits to release render API instance.
     * If not initialized or already destroyed then this method does nothing.
     * - Do NOT make calls to backend after calling this method.
     */
    public abstract void destroy();

}
