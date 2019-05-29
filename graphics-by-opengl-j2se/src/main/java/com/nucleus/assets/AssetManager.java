package com.nucleus.assets;

import com.nucleus.Backend;
import com.nucleus.opengl.assets.GLAssetManager;

/**
 * Loading and unloading assets, mainly textures - this is the main entrypoint for loading of textures and programs.
 * Clients shall only use this class - avoid calling methods to load assets (program/texture etc)
 * Singleton instance that must be initialized for the renderbackend currently running.
 */
public class AssetManager {

    /**
     * Creates the Assets for the specified backend
     * 
     * @param backend
     * @throws IllegalArgumentException If backend is not supported or null
     */
    public static Assets createInstance(Backend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("Backend is null");
        }
        switch (backend.getVersion()) {
            case GLES20:
            case GLES30:
            case GLES31:
            case GLES32:
                return new GLAssetManager(backend);
            default:
                throw new IllegalArgumentException("Not implemented for " + backend.getVersion());
        }
    }

}
