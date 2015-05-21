package com.nucleus.texturing;

import com.nucleus.io.BaseReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES20Wrapper.GLES20;

/**
 * Texture object and Sampler info, this class holds the texture object ID, texture parameters but should
 * NOT keep the texture data.
 * 
 * @author Richard Sahlin
 *
 */
public class Texture2D extends BaseReference {

    public final static int TEXTURE_0 = 0;

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
     * Creates a texture reference with name, width and height.
     * 
     * @param name
     * @param width
     * @param height
     */
    public Texture2D(int name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
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
    public void uploadTexParameters(GLES20Wrapper gles) {

        int[] values = textureParameters.values;
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                values[TextureParameter.MIN_FILTER]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                values[TextureParameter.MAG_FILTER]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                values[TextureParameter.WRAP_S]);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                values[TextureParameter.WRAP_T]);
    }

}
