package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.ExternalReference;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.vecmath.Axis;
import com.nucleus.vecmath.Rectangle;

/**
 * A texture that can be used for tiling, ie using a texture atlas where many images/frames are stored
 * within the texture.
 * Initial support is only to store number of frames across / down.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class TiledTexture2D extends Texture2D {

    public final static String FRAME = "frame";
    public final static String TILE = "tile";

    /**
     * Size of tiled texture, ie how many frames in x and y
     */
    @SerializedName(TILE)
    private int[] tile;

    /**
     * Default constructor
     */
    protected TiledTexture2D() {
        super();
    }

    /**
     * Creates a copy of the texture
     * 
     * @param source
     */
    protected TiledTexture2D(TiledTexture2D source) {
        set(source);
    }

    /**
     * Creates a texture with the specified id
     * 
     * @param id The id of the texture, not the GL texture name.
     * @param externalReference The texture image reference
     * @param targetResolution
     * @param params Texture parameters, min/mag filter wrap s/t
     * @param mipmap
     * @param size Number of frames in x and y
     * @param format
     * @param type
     */
    protected TiledTexture2D(String id, ExternalReference externalReference, RESOLUTION targetResolution,
            TextureParameter params, int mipmap, int[] size, Format format, Type type) {
        super(id, externalReference, targetResolution, params, mipmap, format, type);
        tile = new int[2];
        tile[Axis.WIDTH.index] = size[Axis.WIDTH.index];
        tile[Axis.HEIGHT.index] = size[Axis.HEIGHT.index];
    }

    /**
     * Copies data from the source texture into this.
     * 
     * @param source
     */
    protected void set(TiledTexture2D source) {
        super.set(source);
        setTileSize(source.tile);
    }

    /**
     * Copies the tilesize from the array, creating the tile array if needed.
     * 
     * @param size
     */
    protected void setTileSize(int[] size) {
        if (tile == null) {
            tile = new int[2];
        }
        tile[Axis.WIDTH.index] = size[Axis.WIDTH.index];
        tile[Axis.HEIGHT.index] = size[Axis.HEIGHT.index];
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
     * Returns the size of the tile, ie number of frames in x and y.
     * 
     * @return Number of frames in x and y
     */
    public int[] getTileSize() {
        return tile;
    }

    @Override
    public int getFrameCount() {
        return tile[0] * tile[1];
    }

    @Override
    public Rectangle calculateRectangle(int frame) {
        Rectangle rect = super.calculateRectangle(frame);
        float scaleX = 1f / getTileWidth();
        float scaleY = 1f / getTileHeight();
        float[] values = rect.getValues();
        values[Rectangle.X] *= scaleX;
        values[Rectangle.Y] *= scaleY;
        values[Rectangle.WIDTH] *= scaleX;
        values[Rectangle.HEIGHT] *= scaleY;
        return rect;
    }

}
