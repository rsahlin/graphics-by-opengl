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
     * 
     * @return
     */
    public static Texture2D createTexture(GLES20Wrapper gles, ImageFactory imageFactory, TextureSetup source) {
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
        Texture2D texture = new Texture2D(textureID, textureImg[0].getWidth(), textureImg[0].getHeight());
        texture.textureParameters = source.texParams;
        return texture;

    }

}
