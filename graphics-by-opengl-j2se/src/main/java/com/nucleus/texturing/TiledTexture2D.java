package com.nucleus.texturing;

import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.vecmath.Axis;

/**
 * A texture that can be used for tiling, ie using a texture atlas where many images/frames are stored
 * within the texture.
 * Initial support is only to store number of frames across / down.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledTexture2D extends Texture2D {

    /**
     * Size of tiled texture, ie how many frames in x and y
     */
    private int[] tile;

    /**
     * Default constructor
     */
    protected TiledTexture2D() {
        super();
    }

    /**
     * Creates a texture with the specified id
     * 
     * @param id The id of the texture, not the GL texture name.
     * @param targetResolution
     * @param params Texture parameters, min/mag filter wrap s/t
     * @param dimension Number of frames in x and y
     */
    protected TiledTexture2D(String id, RESOLUTION targetResolution, TextureParameter params, int[] dimension) {
        super(id, targetResolution, params);
        tile = new int[2];
        tile[Axis.WIDTH.index] = dimension[Axis.WIDTH.index];
        tile[Axis.HEIGHT.index] = dimension[Axis.HEIGHT.index];
    }

    /**
     * Setup the texture number of frames in x and y.
     * 
     * @param width Number of frames horizontally in texture
     * @param height Number of frames vertically in texture
     */
    protected void setupTiledSize(int framesX, int framesY) {
        tile[Axis.WIDTH.index] = width;
        tile[Axis.HEIGHT.index] = height;
    }

    /**
     * Returns the number of frames horizontally, use when setting up U coordinates.
     * 
     * @return The number of frames horizontally in texture
     */
    public int getTileWidth() {
        return tile[Axis.WIDTH.index];
    }

    /**
     * Returns the number of frames vertically, use when setting up V coordinates.
     * 
     * @return The number of frames vertically in texture
     */
    public int getTileHeight() {
        return tile[Axis.HEIGHT.index];
    }

    /**
     * Returns the dimension of the tile, ie number of frames in x and y.
     * 
     * @return Number of frames in x and y
     */
    public int[] getTileDimension() {
        return tile;
    }

}
