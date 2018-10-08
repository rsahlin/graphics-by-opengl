package com.nucleus.renderer;

import com.nucleus.texturing.TextureUtils;

/**
 * Renderer configuration, this is the runtime configuration parameters.
 * 
 * @author Richard Sahlin
 *
 */
public class Configuration {

    private final static Configuration configuration = new Configuration();

    private boolean useVBO = true;
    private boolean generateMipMaps = true;

    /**
     * Returns the instance of the Configuration, this will always be the same.
     * 
     * @return The configuration
     */
    public static Configuration getInstance() {
        return configuration;
    }

    /**
     * Sets the VBO configuration, if true then buffer objects are used instead of passing
     * Buffer to glVertexAttribPointer()
     * Note that passing Buffer instead of allocated buffer objects, using glGenBuffers(), is considered
     * to be the _old_way.
     * Future versions of GL will remove this and rely on using VBO (or similar) - this has already
     * been done in JOGL.
     * 
     * @param useVBO True to use VBO
     */
    public void setUseVBO(boolean useVBO) {
        this.useVBO = useVBO;
    }

    /**
     * Returns the state of the VBO flag, if true then VBO should be used.
     * 
     * @return True to use VBO, false otherwise.
     */
    public boolean isUseVBO() {
        return useVBO;
    }

    /**
     * Enables or disable force generation of texture mipmaps.
     * Used by {@link TextureUtils}
     * 
     * @param generateMipMaps True to force generation of texture mipmaps
     */
    public void setForceMipMaps(boolean generateMipMaps) {
        this.generateMipMaps = generateMipMaps;
    }

    /**
     * Returns true if generation of mipmaps should be forced.
     * 
     * @return
     */
    public boolean isGenerateMipMaps() {
        return generateMipMaps;
    }

}
