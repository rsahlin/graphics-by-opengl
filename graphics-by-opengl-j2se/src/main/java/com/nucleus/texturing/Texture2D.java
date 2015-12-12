package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.io.BaseReference;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * Texture object and Sampler info, this class holds the texture object ID, texture parameters and a reference to the
 * Image sources to the texture. This is needed so that the system can remove unused texture sources (bitmaps)
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class Texture2D extends BaseReference {

    public final static int TEXTURE_0 = 0;

    /**
     * This is the originating asset target resolution - the actual assets used may be scaled if the system
     * has a lower resolution.
     */
    @SerializedName("resolution")
    RESOLUTION resolution;
    @SerializedName("mipmap")
    private int mipmap;
    @SerializedName("externalReference")
    private ExternalReference externalReference;
    /**
     * Texture parameter values.
     */
    @SerializedName("textureParameters")
    protected TextureParameter texParameters = new TextureParameter();

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

    /**
     * Default constructor
     */
    protected Texture2D() {
        super();
    }

    /**
     * Creates a texture with the specified id
     * 
     * @param id The id of the texture, not the GL texture name.
     * @param resolution The target resolution for the texture
     * @param params Texture parameters, min/mag filter wrap s/t
     */
    protected Texture2D(String id, RESOLUTION resolution, TextureParameter params) {
        super(id);
        this.resolution = resolution;
        this.texParameters.setValues(params);
    }

    /**
     * Creates a new texture object with the specified external ref, target resolution and mipmap levels
     * 
     * @param externalReference
     * @param resolution
     * @param levels
     */
    protected Texture2D(ExternalReference externalReference, RESOLUTION resolution, int levels) {
        super();
        this.externalReference = externalReference;
        this.resolution = resolution;
        this.mipmap = levels;
    }

    /**
     * Creates a texture reference with name, width and height.
     * 
     * @param name Texture object name (OpenGL)
     * @param images One or more texture sources for mipmapping, if 3 are provided then 3 mipmap levels are used.
     * @param resolution The originating texture source resolution, not that the actual provided sources may be
     * scaled if the platform has lower resolution.
     */
    protected Texture2D(String id, int name, Image[] images, RESOLUTION resolution, TextureParameter params) {
        super(id);
        setup(name, images);
        this.resolution = resolution;
        this.texParameters.setValues(params);
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
     * Returns the external reference for this texture
     * 
     * @return
     */
    public ExternalReference getExternalReference() {
        return externalReference;
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
     * Sets the texture parameter values for this texture to OpenGL, call this to set the correct texture parameters
     * when rendering.
     * 
     * @param gles
     */
    public void uploadTexParameters(GLES20Wrapper gles) throws GLException {

        TexParameter[] values = texParameters.values;
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                values[TextureParameter.MIN_FILTER].value);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                values[TextureParameter.MAG_FILTER].value);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                values[TextureParameter.WRAP_S].value);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                values[TextureParameter.WRAP_T].value);
        GLUtils.handleError(gles, "glTexParameteri ");
    }

}
