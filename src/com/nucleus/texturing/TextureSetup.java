package com.nucleus.texturing;

import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * The data needed to create a new Texture, use this object with serialization or file loading.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureSetup {

    public TextureSetup(String source, RESOLUTION resolution, int levels) {
        sourceName = source;
        targetResolution = resolution;
        this.levels = levels;
    }

    String sourceName;
    int levels;
    RESOLUTION targetResolution;
    /**
     * Optional texture object name, -1 means not allocated
     */
    int textureName = -1;
    /**
     * Texture parameters
     */
    TextureParameter texParams = new TextureParameter();

    /**
     * Return the name of the image that is the source of this texture.
     * 
     * @return
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Sets the texture min/mag filter and texture wrap s/t
     * 
     * @param parameters
     */
    public void setTextureParameter(TextureParameter parameters) {
        texParams = parameters;
    }

    /**
     * Returns the target resolution for the texture, this is to make it possible to calculate if
     * texture shall be biased (reduced)
     * 
     * @return
     */
    public RESOLUTION getResolution() {
        return targetResolution;
    }

    /**
     * Return the number of mip-map levels for this texture.
     * 
     * @return
     */
    public int getLevels() {
        return levels;
    }

}
