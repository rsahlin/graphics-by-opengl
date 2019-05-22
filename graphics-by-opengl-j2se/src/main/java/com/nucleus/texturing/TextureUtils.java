package com.nucleus.texturing;

import java.io.IOException;

import com.nucleus.ErrorMessage;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.Constants;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.Configuration;
import com.nucleus.renderer.Window;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.texturing.BufferImage.ColorModel;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.Texture2D.Type;

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
     * If the value of {@link Texture2D#getLevels()} is > 1 and the texture parameters are set to support mipmap then
     * the mip levels are generated.
     * To automatically generate mipmaps, just set the texture parameters to support mipmap.
     * 
     * @param imageFactory ImageFactory to use when creating/scaling image
     * @param texture The texture source object
     * @return Array with an image for each mip-map level.
     */
    public static BufferImage[] loadTextureMIPMAP(ImageFactory imageFactory, Texture2D texture) {
        try {
            long start = System.currentTimeMillis();
            BufferImage image = loadTextureImage(imageFactory, texture);
            long loaded = System.currentTimeMillis();
            FrameSampler.getInstance()
                    .logTag(FrameSampler.Samples.CREATE_IMAGE, " " + texture.getExternalReference().getSource(), start,
                            loaded);
            int levels = texture.getLevels();
            if (levels == 0 || !texture.getTexParams().isMipMapFilter()) {
                levels = 1;
            }
            // Do not use levels, mipmaps created when textures are uploaded
            levels = 1;
            BufferImage[] images = new BufferImage[levels];
            images[0] = image;
            return images;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the texture image for the texture, fitting the result for the texture resolution bias and screen resolution
     * 
     * @param factory
     * @param texture
     * @return
     * @throws IOException
     */
    protected static BufferImage loadTextureImage(ImageFactory factory, Texture2D texture) throws IOException {
        SimpleLogger.d(TextureUtils.class, "Loading image " + texture.getExternalReference().getSource());
        float scale = (float) Window.getInstance().getHeight() / texture.resolution.lines;
        if (scale < 0.9) {
            RESOLUTION res = RESOLUTION.getResolution(Window.getInstance().getHeight());
            BufferImage img = factory.createImage(texture.getExternalReference().getSource(), scale, scale,
                    getImageFormat(texture), res);
            SimpleLogger.d(TextureUtils.class,
                    "Image scaled " + scale + " to " + img.getWidth() + ", " + img.getHeight()
                            + " for target resolution " + res);
            return img;
        }
        return factory.createImage(texture.getExternalReference().getSource(), getImageFormat(texture));

    }

    /**
     * Uploads the image(s) to the texture, checks if mipmaps should be created.
     * The size of the image will be set in the texture. Texture object must have texture name allocated.
     * 
     * @param gles GLES20Wrapper for GL calls
     * @param texture The texture object, shall have texture name set
     * @param textureImages Array with one or more images to send to GL. If more than
     * one image is specified then multiple mip-map levels will be set.
     * Level 0 shall be at index 0
     * @throws GLException If there is an error uploading the textures
     * @throws IllegalArgumentException If multiple mipmaps provided but texture min filter is not _MIPMAP_
     * @throws IllegalArgumentException If texture does not have a GL texture name
     */
    public static void uploadTextures(GLES20Wrapper gles, Texture2D texture, BufferImage[] textureImages)
            throws GLException {
        if (texture.getName() <= 0) {
            throw new IllegalArgumentException("No texture name for texture " + texture.getId());
        }
        long start = System.currentTimeMillis();
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getName());
        boolean isMipMapParams = texture.getTexParams().isMipMapFilter();
        if ((textureImages.length > 1 && !isMipMapParams) || (texture.getLevels() > 1 && !isMipMapParams)) {
            throw new IllegalArgumentException(
                    "Multiple mipmap images but wrong min filter "
                            + texture.getTexParams().getParameters()[TextureParameter.MIN_FILTER_INDEX]);
        }
        int level = 0;
        texture.setup(textureImages[0].width, textureImages[0].height);
        for (BufferImage textureImg : textureImages) {
            if (textureImg != null) {
                if (texture.getFormat() == null || texture.getType() == null) {
                    throw new IllegalArgumentException("Texture format or type is null for id " + texture.getId()
                            + " : " + texture.getFormat() + ", " + texture.getType());
                }
                gles.texImage(texture, textureImg, level);
                GLUtils.handleError(gles, "texImage2D");
                level++;
            } else {
                break;
            }
        }
        if (textureImages.length == 1 && texture.getTexParams().isMipMapFilter()
                || Configuration.getInstance().isGenerateMipMaps()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + texture.getId());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.UPLOAD_TEXTURE, texture.getId(), start,
                System.currentTimeMillis());
    }

    /**
     * Uploads the image(s) to the texture, checks if mipmaps should be created.
     * 
     * @param gles GLES20Wrapper for GL calls
     * @param image The glTF Image
     * @param true to generate mipmaps
     * @throws GLException If there is an error uploading the textures
     * @throws IllegalArgumentException If multiple mipmaps provided but texture min filter is not _MIPMAP_
     * @throws IllegalArgumentException If texture does not have a GL texture name
     */
    public static void uploadTextures(GLES20Wrapper gles, Image image, boolean generateMipmaps)
            throws GLException {
        if (image.getTextureName() <= 0) {
            throw new IllegalArgumentException("No texture name for texture " + image.getUri());
        }
        long start = System.currentTimeMillis();
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, image.getTextureName());
        Format format = gles.texImage(image, 0);
        GLUtils.handleError(gles, "texImage2D");
        SimpleLogger.d(TextureUtils.class,
                "Uploaded texture " + image.getUri() + " with format " + format);
        if (generateMipmaps || Configuration.getInstance().isGenerateMipMaps()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + image.getUri());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.UPLOAD_TEXTURE, " " + image.getUri(), start,
                System.currentTimeMillis());
    }

    /**
     * Activates texturing, binds the texture and sets texture parameters
     * Checks if texture is an id (dynamic) reference and sets the texture name if not present.
     * 
     * @paran gles
     * @param texture
     * @param unit The texture unit number to use, 0 and up
     */
    public static void prepareTexture(GLES20Wrapper gles, Texture2D texture, int unit) throws GLException {
        if (texture != null && texture.textureType != TextureType.Untextured) {
            gles.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            int textureID = texture.getName();
            if (textureID == Constants.NO_VALUE && texture.getExternalReference().isIdReference()) {
                // Texture has no texture object - and is id reference
                // Should only be used for dynamic textures, eg ones that depend on define in existing node
                AssetManager.getInstance().getIdReference(texture);
                textureID = texture.getName();
                gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
                gles.uploadTexParameters(texture.getTexParams());
                GLUtils.handleError(gles, "glBindTexture()");
            } else {
                gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
                gles.uploadTexParameters(texture.getTexParams());
                GLUtils.handleError(gles, "glBindTexture()");
            }
        }
    }

    /**
     * Activates texturing, binds the texture and sets texture parameters
     * Checks if texture is an id (dynamic) reference and sets the texture name if not present.
     * 
     * @paran gles
     * @param texture Texture to prepare or null
     * @param unit The texture unit number to use, 0 and up
     */
    public static void prepareTexture(GLES20Wrapper gles, Texture texture, int unit) throws GLException {
        if (texture != null) {
            gles.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            int textureID = texture.getImage().getTextureName();
            gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            gles.uploadTexParameters(texture);
            GLUtils.handleError(gles, "glBindTexture()");
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
            case RGBA:
            case RGB:
                return Type.UNSIGNED_BYTE;
            case ALPHA:
            case LUMINANCE:
            case LUMINANCE_ALPHA:
            case RG:
                return Type.UNSIGNED_BYTE;
            case RGB565:
                return Type.UNSIGNED_SHORT_5_6_5;
            case RGBA4:
                return Type.UNSIGNED_SHORT_4_4_4_4;
            case RGB5_A1:
                return Type.UNSIGNED_SHORT_5_5_5_1;
            case DEPTH_16:
                return Type.UNSIGNED_SHORT;
            case DEPTH_24:
                return Type.UNSIGNED_INT;
            case DEPTH_32F:
                return Type.FLOAT;

            default:
                throw new IllegalArgumentException("Not implemented for: " + format);
        }
    }

    /**
     * Return the GL format for the specified imageformat.
     * 
     * @param format Image pixel format.
     * @return GL internal format for the specified format, eg GL_ALPHA, GL_RGB, GL_RGBA
     */
    public static Texture2D.Format getFormat(ImageFormat format) {
        switch (format) {
            case RGBA4:
            case RGBA:
            case RGB5_A1:
                return Format.RGBA;
            case RG:
                return Format.RG;
            case R:
                return Format.R;
            case ALPHA:
                return Format.ALPHA;
            case LUMINANCE:
                return Format.LUMINANCE;
            case LUMINANCE_ALPHA:
                return Format.LUMINANCE_ALPHA;
            case RGB565:
            case RGB:
                return Format.RGB;
            case DEPTH_16:
            case DEPTH_24:
            case DEPTH_32F:
                return Format.DEPTH_COMPONENT;
            default:
                throw new IllegalArgumentException("Not implemented for: " + format);
        }
    }

    /**
     * Return the GL format for the imageformat and colormodel
     * 
     * @param format Image pixel format.
     * @param colorModel
     * @return Texture format
     */
    public static Texture2D.Format getFormat(ImageFormat format, ColorModel colorModel) {
        switch (colorModel) {
            case LINEAR:
                return getFormat(format);
            case SRGB:
                switch (format) {
                    case RGBA:
                        return Format.SRGBA;
                    case RGB:
                        return Format.SRGB;
                    default:
                        throw new IllegalArgumentException("Not valid for SRGB colormodel and " + format);
                }
            default:
                throw new IllegalArgumentException("Not implemented for " + colorModel);
        }
    }

    /**
     * Returns the internal format for the texture - only needed on GLES 3.0 and above
     * 
     * @param texture
     * @return The internal format
     */
    public static int getInternalFormat(Texture2D texture) {
        switch (texture.getFormat()) {
            case DEPTH_COMPONENT:
                return getDepthComponentFormat(texture.getType());
            default:
                return texture.getFormat().format;
        }
    }

    public static int getDepthComponentFormat(Texture2D.Type type) {
        switch (type) {
            case UNSIGNED_SHORT:
                return GLES20.GL_DEPTH_COMPONENT16;
            case UNSIGNED_INT:
                return GLES30.GL_DEPTH_COMPONENT24;
            case FLOAT:
                return GLES20.GL_FLOAT;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
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
