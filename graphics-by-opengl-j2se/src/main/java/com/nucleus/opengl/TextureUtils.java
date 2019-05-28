package com.nucleus.opengl;

import com.nucleus.ErrorMessage;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.texturing.BufferImage.ColorModel;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.Texture2D;
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
