package com.nucleus.renderer;

import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * The low level drawing API backend wrapper
 *
 */
public abstract class Backend {

    public Backend(Renderers version) {
        if (version == null) {
            throw new IllegalArgumentException("Renderer version is null");
        }
        this.version = version;
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

}
