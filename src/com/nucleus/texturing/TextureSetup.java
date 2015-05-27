package com.nucleus.texturing;

import com.nucleus.io.DataSetup;
import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * The data needed to create a new Texture, use this object with serialization or file loading.
 * This class can be used with serialization to decouple io from implementation
 * 
 * @author Richard Sahlin
 *
 */
public class TextureSetup extends DataSetup {

    /**
     * Provides the mapping between serialized data and the data in this class.
     * 
     * @author Richard Sahlin
     *
     */
    public enum TextureMapping implements Indexer {
        SOURCENAME(0),
        TARGET_RESOLUTION(1),
        LEVELS(2);

        private final int index;

        private TextureMapping(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    /**
     * Default constructor, use importData() to fill this class with data.
     */
    public TextureSetup() {
        super();
    }

    public TextureSetup(String sourceName, RESOLUTION targetResolution, int levels) {
        super();
        this.sourceName = sourceName;
        this.targetResolution = targetResolution;
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

    @Override
    public int importData(String[] data, int offset) {
        sourceName = getString(data, offset, TextureMapping.SOURCENAME);
        levels = getInt(data, offset, TextureMapping.LEVELS);
        targetResolution = RESOLUTION.valueOf(getString(data, offset, TextureMapping.TARGET_RESOLUTION));
        return TextureMapping.values().length;
    }

    @Override
    public String exportDataAsString() {
        // TODO Auto-generated method stub
        return null;
    }

}
