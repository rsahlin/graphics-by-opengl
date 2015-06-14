package com.nucleus.texturing;

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
    public enum TiledMapping implements DataIndexer {
        FRAMES_X(0, DataType.INT),
        FRAMES_Y(1, DataType.INT);

        private final int index;
        private final DataType type;

        private TiledMapping(int index, DataType type) {
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
        framesX = getInt(data, offset, TiledMapping.FRAMES_X);
        framesY = getInt(data, offset, TiledMapping.FRAMES_Y);
        return read + TiledMapping.values().length;
    }

    @Override
    public String exportDataAsString() {
        return super.exportDataAsString() + DEFAULT_DELIMITER +
                toString(framesX) + DEFAULT_DELIMITER + toString(framesY);
    }

}
