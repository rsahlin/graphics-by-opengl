package com.nucleus.texturing;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Window;
import com.nucleus.resource.ResourceBias;

/**
 * Used to create texture objects, constructor data is abstracted in a separate class to make it easy to de-couple file
 * loading.
 * 
 * @author Richard Sahlin
 *
 */
public class TextureFactory {

    /**
     * Creates a texture for the specified image, the texture will be scaled according to target resolution (for the
     * image) and current height of screen.
     * The texture will be uploaded to GL using the specified texture object name, if several mip-map levels are
     * supplied they will be used.
     * If the device current resolution is lower than the texture target resolution then a lower mip-map level is used.
     * 
     * @return
     */
    public static Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory, TextureSetup source) {
        Texture2D texture = null;
        if (source instanceof TiledTextureSetup) {
            texture = new TiledTexture2D();
            prepareTiledTexture(gles, imageFactory, (TiledTextureSetup) source, (TiledTexture2D) texture);
        } else {
            texture = new Texture2D();
            prepareTexture(gles, imageFactory, source, texture);
        }
        return texture;

    }

    /**
     * Internal method to create a texture based on the texture setup source, the texture will be set with data from
     * texture setup and uploaded to GL.
     * 
     * @param gles
     * @param imageFactory
     * @param source
     * @param texture The texture destination
     */
    private static void prepareTexture(GLES20Wrapper gles, ImageFactory imageFactory, TextureSetup source,
            Texture2D texture) {
        int[] textures = new int[1];
        gles.glGenTextures(1, textures, 0);

        int textureID = textures[0];
        Window window = Window.getInstance();
        Image[] textureImg = TextureUtils.loadTextureMIPMAP(imageFactory, source.getSourceName(),
                ResourceBias.getScaleFactorLandscape(window.getWidth(), window.getHeight(),
                        source.getResolution().lines), source.getLevels());

        try {
            TextureUtils.uploadTextures(gles, GLES20.GL_TEXTURE0, textureID, textureImg);
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }
        texture.setup(textureID, textureImg, source.targetResolution);
        texture.setId(source.getId());
        texture.textureParameters = source.texParams;
    }

    /**
     * Internal method to prepare a tiled texture
     * 
     * @param gles
     * @param imageFactory
     * @param source
     * @param texture
     */
    private static void prepareTiledTexture(GLES20Wrapper gles, ImageFactory imageFactory, TiledTextureSetup source,
            TiledTexture2D texture) {
        prepareTexture(gles, imageFactory, source, texture);
        texture.setupTiledSize(source.getFramesX(), source.getFramesY());
    }

}
