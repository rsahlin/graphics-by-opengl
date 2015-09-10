package com.nucleus.texturing;


/**
 * A texture that can be used for tiling, ie using a texture atlas where many images/frames are stored
 * within the texture.
 * Initial support is only to store number of frames across / down.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledTexture2D extends Texture2D {

    protected int framesX;
    protected int framesY;

    /**
     * Default constructor
     */
    protected TiledTexture2D() {
        super();
    }

    /**
     * Setup the texture number of frames in x and y.
     * 
     * @param framesX Number of frames horizontally in texture
     * @param framesY Number of frames vertically in texture
     */
    protected void setupTiledSize(int framesX, int framesY) {
        this.framesX = framesX;
        this.framesY = framesY;
    }

    /**
     * Returns the number of frames horizontally, use when setting up U coordinates.
     * 
     * @return The number of frames horizontally in texture
     */
    public int getFramesX() {
        return framesX;
    }

    /**
     * Returns the number of frames vertically, use when setting up V coordinates.
     * 
     * @return The number of frames vertically in texture
     */
    public int getFramesY() {
        return framesY;
    }

}
