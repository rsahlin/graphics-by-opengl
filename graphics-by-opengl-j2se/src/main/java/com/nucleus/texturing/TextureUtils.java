package com.nucleus.texturing;

import java.io.IOException;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;

/**
 * Texture utilities, loading of texture(s)
 * 
 * @author Richard Sahlin
 *
 */
public class TextureUtils {

    public final static String INVALID_FORMAT_ERROR = "Invalid format: ";

    /**
     * Loads an image into several mip-map levels, the same image will be scaled to produce the
     * different mip-map levels.
     * TODO: Add method to ImageFactory to scale existing image - currently re-loads image and scales.
     * 
     * @param imageFactory ImageFactory to use when creating/scaling image
     * @param imageName Name of image to load
     * @param scale The source image will be scaled by this, second mip-map will be 1/2 scale, third will be 1/4 scale
     * @param levels Number of mip-map levels
     * @return Array with an image for each mip-map level.
     */
    public static Image[] loadTextureMIPMAP(ImageFactory imageFactory, String imageName, float scale, int levels) {

        Image[] images = new Image[levels];
        try {
            for (int i = 0; i < levels; i++) {
                images[i] = imageFactory.createImage(imageName, scale, scale);
                scale = scale * 0.5f;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return images;
    }

    /**
     * Sets the active texture, binds texName and calls glTexImage2D on the images in the array where
     * mip-map level will be same as the image index.
     * 
     * @param gles GLES20Wrapper for GL calls
     * @param texture Texture unit number (active texture)
     * @param texName Name of texture object
     * @param textureImages Array with one or more images to send to GL. If more than
     * one image is specified then multiple mip-map levels will be set.
     * Level 0 shall be at index 0
     * @throws GLException If there is an error uploading the textures.
     */
    public static void uploadTextures(GLES20Wrapper gles, int texture, int texName, Image[] textureImages)
            throws GLException {
        gles.glActiveTexture(texture);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texName);
        int level = 0;
        int format;
        for (Image textureImg : textureImages) {
            if (textureImg != null) {
                format = textureImg.getFormat().format;
                gles.glTexImage2D(GLES20.GL_TEXTURE_2D, level, format,
                        textureImg.getWidth(),
                        textureImg.getHeight(), 0, format, getTypeFromFormat(format),
                        textureImg.getBuffer().position(0));
                GLUtils.handleError(gles, "texImage2D");
            }
            level++;
        }

    }

    /**
     * Return the texture type for the specified format.
     * 
     * @param format
     * @return GL datatype for the specified format, eg GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5
     * @throws IllegalArgumentException If format is not one of: GL_RGB, GL_RGBA, GL_RGB565, GL_RGBA4, GL_RGB5_A1
     */
    public static int getTypeFromFormat(int format) {

        switch (format) {
        case GLES20.GL_RGB:
        case GLES20.GL_RGBA:
            return GLES20.GL_UNSIGNED_BYTE;
        case GLES20.GL_RGB565:
            return GLES20.GL_UNSIGNED_SHORT_5_6_5;
        case GLES20.GL_RGBA4:
            return GLES20.GL_UNSIGNED_SHORT_4_4_4_4;
        case GLES20.GL_RGB5_A1:
            return GLES20.GL_UNSIGNED_SHORT_5_5_5_1;
        default:
            throw new IllegalArgumentException(INVALID_FORMAT_ERROR + format);
        }

    }
}
