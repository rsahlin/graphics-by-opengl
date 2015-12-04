package com.nucleus.texturing;

import com.nucleus.io.BaseReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * Texture object and Sampler info, this class holds the texture object ID, texture parameters and a reference to the
 * Image sources to the texture. This is needed so that the system can remove unused texture sources (bitmaps)
 * 
 * @author Richard Sahlin
 *
 */
public class Texture2D extends BaseReference {

    public final static int TEXTURE_0 = 0;

    /**
     * Texture sources, one for each used mip-map level
     */
    Image[] images;

    /**
     * This is the originating asset target resolution - the actual assets used may be scaled if the system
     * has a lower resolution.
     */
    RESOLUTION targetResolution;

    /**
     * The texture name, this is a loose reference to the allocated texture name.
     * It is up to the caller to allocate and release texture names.
     */
    protected int name;
    /**
     * Width of texture in pixels
     */
    protected int width;
    /**
     * Height of texture in pixels
     */
    protected int height;
    /**
     * Texture parameter values.
     */
    protected TextureParameter textureParameters = new TextureParameter();

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
     * @param targetResolution
     * @param params Texture parameters, min/mag filter wrap s/t
     */
    protected Texture2D(String id, RESOLUTION targetResolution, TextureParameter params) {
        super(id);
        this.targetResolution = targetResolution;
        this.textureParameters.setValues(params);
    }

    /**
     * Creates a texture reference with name, width and height.
     * 
     * @param name Texture object name (OpenGL)
     * @param images One or more texture sources for mipmapping, if 3 are provided then 3 mipmap levels are used.
     * @param targetResolution The originating texture source resolution, not that the actual provided sources may be
     * scaled if the platform has lower resolution.
     */
    protected Texture2D(String id, int name, Image[] images, RESOLUTION targetResolution, TextureParameter params) {
        super(id);
        setup(name, images);
        this.targetResolution = targetResolution;
        this.textureParameters.setValues(params);
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
        return textureParameters;
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
     * Sets the texture parameter values for this texture to OpenGL, call this to set the correct texture parameters
     * when rendering.
     * 
     * @param gles
     */
    public void uploadTexParameters(GLES20Wrapper gles) throws GLException {

        int[] values = textureParameters.values;
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                values[TextureParameter.MIN_FILTER]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                values[TextureParameter.MAG_FILTER]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                values[TextureParameter.WRAP_S]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                values[TextureParameter.WRAP_T]);
        GLUtils.handleError(gles, "glTexParameteri ");
    }

}
