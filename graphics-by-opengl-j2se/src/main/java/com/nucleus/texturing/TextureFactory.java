package com.nucleus.texturing;

import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * Used to create texture objects.
 * Texture objects created using the same image (external reference) will share texture data, through the texture object
 * id.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureFactory {

    /**
     * Creates a texture for the specified image,
     * The texture will be uploaded to GL using the specified texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @return A new texture object containing the texture image.
     */
    public static Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory, Texture2D source) {
        Texture2D texture = createTexture(source);
        prepareTexture(gles, texture, imageFactory, source.getExternalReference(), source.getLevels());
        return texture;
    }

    /**
     * Creates a texture for the specified image,
     * The texture will be uploaded to GL using the specified texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @return A new texture object containing the texture image.
     */
    public static Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory,
            ExternalReference externalReference,
            RESOLUTION resolution) {
        Texture2D source = new Texture2D(externalReference, resolution, 1);
        return createTexture(gles, imageFactory, source);
    }

    /**
     * Creates a copy of the texture contents (image)
     * 
     * @param source The source texture, this holds the texture data
     * @return Texture with same contents as the source
     */
    public static Texture2D createTexture(Texture2D source) {
        switch (source.type) {
        case Texture2D:
            return new Texture2D(source);
        case TiledTexture2D:
            return new TiledTexture2D((TiledTexture2D) source);
        case UVTexture2D:
            return new UVTexture2D((UVTexture2D) source);
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates a new empty texture object of the specified type
     * 
     * @param type The texture type
     * @return The texture
     */
    public static Texture2D createTexture(TextureType type) {
        switch (type) {
        case Texture2D:
            return new Texture2D();
        case TiledTexture2D:
            return new TiledTexture2D();
        case UVTexture2D:
            return new UVTexture2D();
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Internal method to create a texture based on the texture setup source, the texture will be set with data from
     * texture setup and uploaded to GL.
     * 
     * @param gles
     * @param texture The texture destination
     * @param imageFactory
     * @param textureSource The texture image to load
     * @param mipmaps Number of mipmap levels
     */
    private static void prepareTexture(GLES20Wrapper gles, Texture2D texture, ImageFactory imageFactory,
            ExternalReference textureSource, int mipmaps) {
        int[] textures = new int[1];
        gles.glGenTextures(1, textures, 0);

        int textureID = textures[0];
        Image[] textureImg = TextureUtils
                .loadTextureMIPMAP(imageFactory, textureSource.getSource(), 1, mipmaps);

        try {
            TextureUtils.uploadTextures(gles, GLES20.GL_TEXTURE0, textureID, textureImg);
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }
        // TODO Do not need to keep images after texture is uploaded
        texture.setup(textureID, textureImg);
    }

    /**
     * Copies the texture data, ie the texture name and image data to the destination.
     * 
     * @param source
     * @param destination
     */
    public static void copyTextureData(Texture2D source, Texture2D destination) {
        destination.setup(source.getName(), source.images);
    }

}
