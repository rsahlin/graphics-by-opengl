package com.nucleus.texturing;

import com.nucleus.common.StringUtils;
import com.nucleus.io.ExternalReference;
import com.nucleus.types.DataType;

/**
 * Setup for a tiled texture, adds number of frames horizontally and vertically.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledTextureSetup extends TextureSetup {

    /**
     * Provides the mapping between serialized data and the data in this class.
     * 
     * @author Richard Sahlin
     *
     */
    public enum TiledTextureMapping implements DataIndexer {
        FRAMES_X(0, DataType.INT),
        FRAMES_Y(1, DataType.INT);

        private final int index;
        private final DataType type;

        private TiledTextureMapping(int index, DataType type) {
            this.index = index;
            this.type = type;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public DataType getType() {
            return type;
        }

    }

    int framesX;
    int framesY;

    /**
     * Default constructor
     */
    public TiledTextureSetup() {
    }

    /**
     * Creates a new setup with external reference and tiledtexture.
     * 
     * @param externalRef
     * @param texture
     */
    public TiledTextureSetup(ExternalReference externalRef, TiledTexture2D texture) {
        super(externalRef, texture);
        framesX = texture.getFramesX();
        framesY = texture.getFramesY();
    }

    /**
     * Returns the number of frames horizontally
     * 
     * @return Number of frames on x axis
     */
    public int getFramesX() {
        return framesX;
    }

    /**
     * Returns the number of frames vertically.
     * 
     * @return Number of frames on y axis.
     */
    public int getFramesY() {
        return framesY;
    }

    @Override
    public int importData(String[] data, int offset) {
        int read = super.importData(data, offset);
        offset += read;
        framesX = getInt(data, offset, TiledTextureMapping.FRAMES_X);
        framesY = getInt(data, offset, TiledTextureMapping.FRAMES_Y);
        return read + TiledTextureMapping.values().length;
    }

    @Override
    public String exportDataAsString() {
        String[] strArray = new String[TiledTextureMapping.values().length];
        setData(strArray, TiledTextureMapping.FRAMES_X, framesX);
        setData(strArray, TiledTextureMapping.FRAMES_Y, framesY);
        return super.exportDataAsString() + DEFAULT_DELIMITER + StringUtils.getString(strArray);
    }

}
