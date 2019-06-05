package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.Constants;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.renderer.Window;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.vecmath.Rectangle;

/**
 * Texture object and Sampler info, this class holds the texture object ID, texture parameters and a reference to the
 * Image sources to the texture. This is needed so that the system can remove unused texture sources (bitmaps)
 * This class can be serialized using GSON
 * Texture objects shall be treated as immutable
 * 
 * @author Richard Sahlin
 *
 */
public class Texture2D extends BaseReference {

    public static final String RESOLUTION = "resolution";
    public static final String MIPMAP = "mipmap";
    /**
     * Name of the serialized field textureType
     */
    public static final String TEXTURETYPE = "textureType";
    public static final String TEXPARAMETERS = "texParameters";
    public static final String FORMAT = "format";
    public static final String TYPE = "type";
    public static final String FLIPV = "flipv";

    /**
     * The GL texture formats
     * 
     */
    public enum Format {
        ALPHA(0x1906, 0x1906, 1),
        RGB(0x1907, 0x1907, 1),
        RGBA(0x1908, 0x1908, 4),
        RG(0x8227, 0x822B, 2),
        R(0x1903, 0x8229, 1),
        SRGB(0x1907, 0x8C41, 1),
        SRGBA(0x1908, 0x8C43, 4),
        LUMINANCE(0x1909, 0x1909, 1),
        LUMINANCE_ALPHA(0x190A, 0x190A, 2),
        /**
         * Depends on what the depth format is
         */
        DEPTH_COMPONENT(0x1902, 0x1902, 1);

        public final int format;
        public final int internalFormat;
        /**
         * Pixel row alignment in bytes
         */
        public final int rowAlignment;

        private Format(int format, int internalFormat, int rowAlignment) {
            this.format = format;
            this.internalFormat = internalFormat;
            this.rowAlignment = rowAlignment;
        }
    }

    /**
     * The GL texture types
     * 
     * @author Richard Sahlin
     *
     */
    public enum Type {
        UNSIGNED_BYTE(0x1401),
        UNSIGNED_SHORT_5_6_5(0x8363),
        UNSIGNED_SHORT_4_4_4_4(0x8033),
        UNSIGNED_SHORT_5_5_5_1(0x8034),
        UNSIGNED_SHORT(0x1403),
        UNSIGNED_INT(0x1405),
        FLOAT(0x1406);

        public final int type;

        private Type(int type) {
            this.type = type;
        }

    }

    public final static int TEXTURE_0 = 0;

    /**
     * This is the originating asset target resolution - the actual assets used may be scaled if the system
     * has a lower resolution.
     */
    @SerializedName(RESOLUTION)
    RESOLUTION resolution;
    @SerializedName(MIPMAP)
    private int levels;

    /**
     * Texture parameter values.
     */
    @SerializedName(TEXPARAMETERS)
    protected TextureParameter texParameters;
    /**
     * The texture format
     */
    @SerializedName(FORMAT)
    private Format format;

    /**
     * The texture type
     */
    @SerializedName(TYPE)
    private Type type;

    /**
     * Set to true to flip texture V, ie flipping the Y axis.
     * Used to display framebuffer target rendered to texture properly.
     */
    @SerializedName(FLIPV)
    private boolean flipV = false;

    /**
     * The texture name, this is a loose reference to the allocated texture name.
     * It is up to the caller to allocate and release texture names.
     */
    transient protected int name = Constants.NO_VALUE;
    /**
     * Width of texture in pixels
     */
    transient protected int width;
    /**
     * Height of texture in pixels
     */
    transient protected int height;

    // TODO Make this private
    @SerializedName(value = TEXTURETYPE)
    public final TextureType textureType;

    /**
     * Default constructor
     */
    protected Texture2D() {
        super();
        textureType = TextureType.valueOf(getClass().getSimpleName());
    }

    /**
     * Creates a copy of the specified texture, this can be used if more than one instance of the same
     * texture is needed.
     * The external reference and images will be referenced
     * A new instance of texture parameters will be created.
     * 
     * @param source
     */
    protected Texture2D(Texture2D source) {
        textureType = source.getTextureType();
        set(source);
    }

    /**
     * Returns the texture implementation type
     * 
     * @return
     */
    public TextureType getTextureType() {
        return textureType;
    }

    /**
     * Copies the values from the source texture into this
     * 
     * @param source
     */
    public void set(Texture2D source) {
        super.set(source);
        resolution = source.resolution;
        if (source.getTexParams() != null) {
            texParameters = new TextureParameter(source.getTexParams());
        } else {
            texParameters = null;
        }
        levels = source.levels;
        name = source.name;
        width = source.width;
        height = source.height;
        type = source.type;
        format = source.format;
        flipV = source.flipV;
    }

    /**
     * Sets the reference to texture parameters, this will not upload texture parameters
     * 
     * @param parameters Reference to texture parameters
     */
    public void set(TextureParameter parameters) {
        this.texParameters = parameters;
    }

    /**
     * Creates a texture with the specified id, external ref, target resolution and mipmap levels
     * 
     * @param id The id of the texture, not the GL texture name.
     * @param externalReference The texture image reference
     * @param resolution The target resolution for the texture
     * @param params Texture parameters, min/mag filter wrap s/t
     * @param levels Number of mipmap levels
     * @param format The texture format
     * @param type The texture type
     */
    protected Texture2D(String id, ExternalReference externalReference, RESOLUTION resolution,
            TextureParameter params, int levels, Format format, Type type) {
        super(id);
        textureType = TextureType.valueOf(getClass().getSimpleName());
        setExternalReference(externalReference);
        setup(resolution, params, levels, format, type);
    }

    /**
     * Sets the resolution, filter, mipmap format and type, use this when constructing an empty texture object and
     * filling it with data.
     * 
     * @param resolution
     * @param params
     * @param levels
     * @param format
     * @param type
     */
    protected void setup(RESOLUTION resolution, TextureParameter params, int levels, Format format, Type type) {
        this.resolution = resolution;
        texParameters = new TextureParameter(params);
        this.levels = levels;
        this.format = format;
        this.type = type;
    }

    /**
     * Sets the texture size.
     * The texture(s) will not be uploaded to GL.
     * Use this to set the size and texture name after image has been loaded.
     * 
     * @param width
     * @param height
     */
    public void setup(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the texture object name (for GL)
     * The texture(s) will not be uploaded to GL.
     * 
     * @param name
     */
    public void setTextureName(int name) {
        this.name = name;
    }

    /**
     * Copies the transient values (texture object name, width, height) and the texture format values into this class.
     * Use this to copy an instance of an existing texture but to a new type or with different texture parameters.
     * 
     * @param source
     */
    protected void copyInstance(Texture2D source) {
        this.name = source.name;
        this.width = source.width;
        this.height = source.height;
        this.format = source.format;
        this.type = source.type;
    }

    /**
     * Returns the texture parameters to use with this texture.
     * 
     * @return
     */
    public TextureParameter getTexParams() {
        return texParameters;
    }

    /**
     * Returns the resolution of the texture, ie what resolution the texture is targeted at.
     * 
     * @return The texture resolution
     */
    public RESOLUTION getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution of the texture, this should only be changed if images are scaled due to screensize.
     * 
     * @param resolution
     */
    public void setResolution(RESOLUTION resolution) {
        this.resolution = resolution;
    }

    /**
     * Returns the texture name
     * 
     * @return
     */
    public int getName() {
        return name;
    }

    /**
     * Returns the width, in pixels, of the texture
     * 
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height, pixels, of the texture
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the number of mipmap levels, used when serializing this texture.
     * 
     * @return
     */
    public int getLevels() {
        return levels;
    }

    /**
     * Returns the GL texture type, number of bits per pixel.
     * 
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the texture format
     * 
     * @return
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Returns true if the texture V coordinate shall be flipped for this texture.
     * If true this will flip the texture on Y axis.
     * 
     * @return
     */
    public boolean isFlipV() {
        return flipV;
    }

    /**
     * Returns the number of frames defined in the texture - subclasses must override this
     * if they support multiple frames. For instance tiled texture
     * 
     * @return Number of frames in the texture
     */
    public int getFrameCount() {
        return 1;
    }

    @Override
    public String toString() {
        return (textureType + " " + (getId() != null ? getId() : "") + " : " + width + "," + height + " : " + format
                + " : " + type
                + ", "
                + getLevels() + " levels");
    }

    /**
     * Calculates a normalized rectangle that covers one frame of this texture, based on size of the Window
     * and the source resolution of this texture.
     * 
     * @param frame
     * @return A normalized rectangle, based on window width and height, using aspect.
     * The rectangle will be centered horizontally and vertically
     */
    public Rectangle calculateRectangle(int frame) {
        Window w = Window.getInstance();
        float aspect = (float) w.getWidth() / w.getHeight();
        float scaleFactor = (float) w.getHeight() / getResolution().lines;
        float normalizedWidth = ((getWidth() * scaleFactor) / w.getWidth()) * aspect;
        float normalizedHeight = (getHeight() * scaleFactor) / w.getHeight();
        Rectangle rect = new Rectangle(-normalizedWidth / 2, normalizedHeight / 2, normalizedWidth, normalizedHeight);
        return rect;
    }

    /**
     * Releases any resource held by this texture, this will not delete texture from GL, this has to be done separately.
     * If additional data is allocated by the texture, other than the uploaded texture, these resources shall be
     * released.
     * 
     */
    public void destroy() {
        // Release any allocated resources other than the uploaded texture.
    }

    /**
     * Checks the texture parameters and mipmap levels for consistensy
     * TODO - Maybe skip checking mipmap params and texture filter, instead if texture filter is mapmap then
     * have gl create mipmaps if they are not specified.
     * 
     * @return
     */
    public boolean validateTextureParameters() {
        // If untextured return true;
        if (textureType == TextureType.Untextured) {
            return true;
        }
        boolean isMipMapParams = getTexParams().isMipMapFilter();
        if ((levels > 1 && !isMipMapParams) || (levels < 2 && isMipMapParams)) {
            return false;
        }
        return true;
    }

}
