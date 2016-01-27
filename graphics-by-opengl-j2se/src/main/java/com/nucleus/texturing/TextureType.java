package com.nucleus.texturing;

/**
 * Enumeration for the different texture types.
 * 
 * @author Richard Sahlin
 *
 */
public enum TextureType {
    /**
     * 2D Texture - note this name must be the same as {@link Texture2D}
     */
    Texture2D(),
    /**
     * A Tiled 2D texture, all frames has the same size, texture defines number of frames in x and y - note this name
     * must be the same as {@link TiledTexture2D}
     */
    TiledTexture2D(),
    /**
     * A 2D texture with frames defined in the texture, frames can have different sizes - note this name must be the
     * same as {@link UVTexture2D}
     */
    UVTexture2D();

}
