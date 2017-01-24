package com.nucleus.texturing;

import java.io.IOException;

import com.nucleus.ErrorMessage;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.Texture2D.Type;
import com.nucleus.texturing.TextureParameter.Name;

/**
 * Texture utilities, loading of texture(s)
 * 
 * @author Richard Sahlin
 *
 */
public class TextureUtils {

    /**
     * Loads an image into several mip-map levels, the same image will be scaled to produce the
     * different mip-map levels.
     * TODO: Add method to ImageFactory to scale existing image - currently re-loads image and scales.
     * 
     * @param imageFactory ImageFactory to use when creating/scaling image
     * @param texture The texture source object
     * @return Array with an image for each mip-map level.
     */
    public static Image[] loadTextureMIPMAP(ImageFactory imageFactory, Texture2D texture) {

        try {
            ImageFormat imageFormat = getImageFormat(texture);
            Image image = imageFactory.createImage(texture.getExternalReference().getSource(), imageFormat);
            int width = image.getWidth();
            int height = image.getHeight();
            int levels = texture.getLevels();
            if (levels == 0 || !texture.getTexParams().isMipMapFilter()) {
                levels = 1;
            }
            if (levels > 1) {
                levels = (int) Math.floor(Math.log((Math.max(width, height))) / Math.log(2)) + 1;
            }
            Image[] images = new Image[levels];
            images[0] = image;
            if (levels > 1) {
                // levels = 1 + (int) Math.floor(Math.log(Math.max(scaledWidth, scaledHeight)));
                for (int i = 1; i < levels; i++) {
                    // max(1, floor(w_t/2^i)) x max(1, floor(h_t/2^i))
                    int scaledWidth = (int) Math.max(1, Math.floor(width / Math.pow(2, i)));
                    int scaledHeight = (int) Math.max(1, Math.floor(height / Math.pow(2, i)));
                    images[i] = imageFactory.createScaledImage(images[0], scaledWidth,
                            scaledHeight, imageFormat);
                }
            }
            return images;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the active texture, binds texName and calls glTexImage2D on the images in the array where
     * mip-map level will be same as the image index.
     * 
     * @param gles GLES20Wrapper for GL calls
     * @param unit Texture unit number (active texture)
     * @param texture The texture object
     * @param texName Name of texture object
     * @param textureImages Array with one or more images to send to GL. If more than
     * one image is specified then multiple mip-map levels will be set.
     * Level 0 shall be at index 0
     * @throws GLException If there is an error uploading the textures
     * @throws IllegalArgumentException If multiple mipmaps provided but texture min filter is not _MIPMAP_
     */
    public static void uploadTextures(GLES20Wrapper gles, int unit, Texture2D texture, int texName,
            Image[] textureImages)
            throws GLException {
        if (textureImages.length > 1 && !texture.getTexParams().isMipMapFilter()) {
            throw new IllegalArgumentException(
                    "Multiple mipmap images but wrong min filter " + texture.getTexParams().getValue(Name.MIN_FILTER));
        }
        int level = 0;
        int format;
        int type;
        gles.glActiveTexture(unit);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texName);
        for (Image textureImg : textureImages) {
            if (textureImg != null) {
                if (texture.getFormat() == null || texture.getType() == null) {
                    throw new IllegalArgumentException("Texture format or type is null for id " + texture.getId()
                            + " : " + texture.getFormat() + ", " + texture.getType());
                }
                format = texture.getFormat().format;
                type = texture.getType().type;
                gles.glTexImage2D(GLES20.GL_TEXTURE_2D, level, format,
                        textureImg.getWidth(),
                        textureImg.getHeight(), 0, format, type,
                        textureImg.getBuffer().position(0));
                GLUtils.handleError(gles, "texImage2D");
                level++;
            } else {
                break;
            }
        }
        if (textureImages.length == 1 && texture.getTexParams().isMipMapFilter()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + texture.getId());
        }
    }

    /**
     * Return the GL texture type for the specified format.
     * 
     * @param format Image pixel format.
     * @return GL datatype for the specified format, eg GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5
     */
    public static Texture2D.Type getType(ImageFormat format) {

        switch (format) {
            case ABGR4:
            case RGBA:
            case RGB:
            return Type.UNSIGNED_BYTE;
            case ALPHA:
            case LUMINANCE:
            case LUMINANCE_ALPHA:
            return Type.UNSIGNED_BYTE;
            case RGB565:
            return Type.UNSIGNED_SHORT_5_6_5;
            case RGBA4:
            return Type.UNSIGNED_SHORT_4_4_4_4;
            case RGB5_A1:
            return Type.UNSIGNED_SHORT_5_5_5_1;
            default:
            throw new IllegalArgumentException("Not implemented for: " + format);
        }
    }

    /**
     * Return the GL internal format for the specified format.
     * 
     * @param format Image pixel format.
     * @return GL internal format for the specified format, eg GL_ALPHA, GL_RGB, GL_RGBA
     */
    public static Texture2D.Format getFormat(ImageFormat format) {
        switch (format) {
        case ABGR4:
        case RGBA4:
        case RGBA:
        case RGB5_A1:
            return Format.RGBA;
        case ALPHA:
            return Format.ALPHA;
        case LUMINANCE:
            return Format.LUMINANCE;
        case LUMINANCE_ALPHA:
            return Format.LUMINANCE_ALPHA;
        case RGB565:
        case RGB:
            return Format.RGB;
        default:
            throw new IllegalArgumentException("Not implemented for: " + format);
        }
    }

    /**
     * Returns the ImageFormat that fits for the texture format and type.
     * If format or type is not defined then the default value is choosen, normally RGBA 32 bit.
     * 
     * @param texture
     * @return The imageformat that is suitable for the texture format and type
     */
    public static ImageFormat getImageFormat(Texture2D texture) {
        Format format = texture.getFormat();
        Type type = texture.getType();
        if (format == null || type == null) {
            return ImageFormat.RGBA;
        }

        switch (format) {
        case RGBA:
            switch (type) {
            case UNSIGNED_BYTE:
                return ImageFormat.RGBA;
            case UNSIGNED_SHORT_4_4_4_4:
                return ImageFormat.RGBA4;
            case UNSIGNED_SHORT_5_5_5_1:
                return ImageFormat.RGB5_A1;
                default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_TYPE.message + type);
            }
        case RGB:
            switch (type) {
            case UNSIGNED_BYTE:
                return ImageFormat.RGB;
            case UNSIGNED_SHORT_5_6_5:
                return ImageFormat.RGB565;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_TYPE.message + type);
            }
        case ALPHA:
            switch (type) {
            case UNSIGNED_BYTE:
                return ImageFormat.ALPHA;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_TYPE.message + type);
            }
        case LUMINANCE:
            switch (type) {
            case UNSIGNED_BYTE:
                return ImageFormat.LUMINANCE;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_TYPE.message + type);
            }
        case LUMINANCE_ALPHA:
            switch (type) {
            case UNSIGNED_BYTE:
                return ImageFormat.LUMINANCE_ALPHA;
            case UNSIGNED_SHORT_5_5_5_1:
                return ImageFormat.RGB5_A1;
            case UNSIGNED_SHORT_4_4_4_4:
                return ImageFormat.RGBA4;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_TYPE.message + type);
            }
            default:
            throw new IllegalArgumentException(ErrorMessage.INVALID_FORMAT.message);
        }

    }


}
