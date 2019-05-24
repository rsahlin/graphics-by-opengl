package com.nucleus.assets;

import com.nucleus.opengl.assets.GLAssetManager;
import com.nucleus.renderer.Backend;

/**
 * Loading and unloading assets, mainly textures - this is the main entrypoint for loading of textures and programs.
 * Clients shall only use this class - avoid calling methods to load assets (program/texture etc)
 * Singleton instance that must be initialized for the renderbackend currently running.
 */
public class AssetManager {

    protected static Assets assetManager = null;

    /**
     * Returns the Assets instance that is initialized for a renderbackend by calling
     * Call {@link #createInstance(Backend)} to create an instance before calling this method.
     * 
     * @return The Assets
     */
    public static Assets getInstance() {
        if (assetManager == null) {
            throw new IllegalArgumentException(
                    "Assetmanager not initialized - must call #createInstance() before calling this method");
        }
        return assetManager;
    }

    /**
     * Creates the Assets for the specified backend
     * 
     * @param backend
     * @throws IllegalArgumentException If backend is not supported or null
     */
    public static void createInstance(Backend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("Backend is null");
        }
        switch (backend.getVersion()) {
            case GLES20:
            case GLES30:
            case GLES31:
            case GLES32:
                assetManager = new GLAssetManager(backend);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + backend.getVersion());
        }
    }

}
