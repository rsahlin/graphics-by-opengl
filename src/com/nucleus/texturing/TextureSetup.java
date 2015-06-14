package com.nucleus.texturing;

import com.nucleus.io.DataSetup;
import com.nucleus.io.ExternalReference;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.types.DataType;

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
    public enum TextureMapping implements DataIndexer {
        SOURCENAME(0, DataType.STRING),
        TARGET_RESOLUTION(1, DataType.RESOLUTION),
        LEVELS(2, DataType.INT);

        private final int index;
        private final DataType type;

        private TextureMapping(int index, DataType type) {
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
     * Default constructor, used when importing data.
     * You must fill this class with data, either by calling a setup method or importing data.
     */
    public TextureSetup() {
        super();
    }

    /**
     * Creates a new texture setup with the external reference and based on the data in the texture.
     * This is the same as creating the class then calling {@link #setup(ExternalReference, Texture2D)}
     * 
     * @param externalRef
     * @param texture
     */
    public TextureSetup(ExternalReference externalRef, Texture2D texture) {
        super();
        setup(externalRef, texture);
    }

    /**
     * Creates a texture setup with name of source, target resolution and levels.
     * This is the same as creating the class then calling {@link #setup(ExternalReference, RESOLUTION, int)}
     * 
     * @param externalRef The texture source name, ie the image/data that shall be used for the texture.
     * @param targetResolution
     * @param levels
     */
    public TextureSetup(ExternalReference externalRef, RESOLUTION targetResolution, int levels) {
        super();
        setup(externalRef, targetResolution, levels);
    }

    /**
     * Sets setup data from an existing texture and sourceName
     * 
     * @param externalRef The external source for the texture (image)
     * @param texture
     */
    public void setup(ExternalReference externalRef, Texture2D texture) {
        this.levels = texture.images.length;
        this.targetResolution = texture.targetResolution;
        this.texParams = texture.textureParameters;
        this.sourceName = externalRef.getSourceName();
        setId(texture.getId());
    }

    /**
     * Sets texture setup with name of source, target resolution and levels.
     * 
     * @param externalRef The texture source name, ie the image/data that shall be used for the texture.
     * @param targetResolution
     * @param levels
     */
    public void setup(ExternalReference externalRef, RESOLUTION targetResolution, int levels) {
        this.sourceName = externalRef.getSourceName();
        this.targetResolution = targetResolution;
        this.levels = levels;
    }

    /**
     * Return the name of the image that is the source of this texture, normally a file such as 'image.png' or
     * a compressed texture file.
     * 
     * @return The name of the external resource (file) containing the texture data.
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
        targetResolution = RESOLUTION.valueOf(getString(data, offset, TextureMapping.TARGET_RESOLUTION));
        levels = getInt(data, offset, TextureMapping.LEVELS);
        return TextureMapping.values().length;
    }

    @Override
    public String exportDataAsString() {
        String d = DEFAULT_DELIMITER;
        return sourceName + d + targetResolution + d + toString(levels);
    }

}
