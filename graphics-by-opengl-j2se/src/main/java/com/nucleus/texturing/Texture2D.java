package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.texturing.TextureParameter.Name;

/**
 * Texture object and Sampler info, this class holds the texture object ID, texture parameters and a reference to the
 * Image sources to the texture. This is needed so that the system can remove unused texture sources (bitmaps)
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class Texture2D extends BaseReference {

    /**
     * Name of the serialized field textureType
     */
    public final static String TEXTURETYPE = "textureType";

    /**
     * The GL texture formats
     * 
     * @author Richard Sahlin
     *
     */
    public enum Format {
        ALPHA(0x1906),
        RGB(0x1907),
        RGBA(0x1908),
        LUMINANCE(0x1909),
        LUMINANCE_ALPHA(0x190A);

        public final int format;

        private Format(int format) {
            this.format = format;
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
        UNSIGNED_SHORT_5_5_5_1(0x8034);

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
    @SerializedName("resolution")
    RESOLUTION resolution;
    @SerializedName("mipmap")
    private int mipmap;

    /**
     * Texture parameter values.
     */
    @SerializedName("texParameters")
    protected TextureParameter texParameters;
    /**
     * The texture format
     */
    @SerializedName("format")
    private Format format;

    /**
     * The texture type
     */
    @SerializedName("type")
    private Type type;

    /**
     * The texture name, this is a loose reference to the allocated texture name.
     * It is up to the caller to allocate and release texture names.
     */
    transient protected int name;
    /**
     * Width of texture in pixels
     */
    transient protected int width;
    /**
     * Height of texture in pixels
     */
    transient protected int height;
    /**
     * Texture sources, one for each used mip-map level
     */
    transient Image[] images;

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
    protected void set(Texture2D source) {
        super.set(source);
        resolution = source.resolution;
        if (source.getTexParams() != null) {
            texParameters = new TextureParameter(source.getTexParams());
        } else {
            texParameters = null;
        }
        mipmap = source.mipmap;
        name = source.name;
        width = source.width;
        height = source.height;
        images = source.images;
        type = source.type;
        format = source.format;
    }

    /**
     * Creates a texture with the specified id, external ref, target resolution and mipmap levels
     * 
     * @param id The id of the texture, not the GL texture name.
     * @param externalReference The texture image reference
     * @param resolution The target resolution for the texture
     * @param params Texture parameters, min/mag filter wrap s/t
     * @param mipmap Number of mipmap levels
     * @param format The texture format
     * @param type The texture type 
     */
    protected Texture2D(String id, ExternalReference externalReference, RESOLUTION resolution,
            TextureParameter params, int mipmap, Format format, Type type) {
        super(id);
        textureType = TextureType.valueOf(getClass().getSimpleName());
        setExternalReference(externalReference);
        this.resolution = resolution;
        texParameters = new TextureParameter(params);
        this.texParameters.setValues(params);
        this.mipmap = mipmap;
        this.format = format;
        this.type = type;
    }


    /**
     * Sets the texture object name (for GL), the images (buffers) to use and the resolution of textures.
     * The texture(s) will not be uploaded to GL.
     * 
     * @param name
     * @param images
     */
    protected void setup(int name, Image[] images) {
        this.name = name;
        this.images = images;
        this.width = images[0].width;
        this.height = images[0].height;
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
        return mipmap;
    }

    /**
     * Returns the texture type
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
     * Sets the texture parameter values for this texture to OpenGL, call this to set the correct texture parameters
     * when rendering.
     * 
     * @param gles
     */
    public void uploadTexParameters(GLES20Wrapper gles) throws GLException {

        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                texParameters.getValue(Name.MIN_FILTER).value);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                texParameters.getValue(Name.MAG_FILTER).value);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                texParameters.getValue(Name.WRAP_S).value);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                texParameters.getValue(Name.WRAP_T).value);
        GLUtils.handleError(gles, "glTexParameteri ");
    }

    @Override
    public String toString() {
        return (textureType + " " + (getId() != null ? getId() : "") + " : " + width + "," + height + " : " + format
                + " : " + type
                + ", "
                + (images != null ? images.length : 0) + " levels");
    }
}
