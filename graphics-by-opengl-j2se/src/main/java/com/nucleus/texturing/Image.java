package com.nucleus.texturing;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.nucleus.ErrorMessage;
import com.nucleus.geometry.BufferObject;

/**
 * Container for pixel based images that can be used for texturing in OpenGL, this is for a straightforward
 * image. It does not contain LOD information.
 * 
 * @author Richard Sahlin
 *
 */
public class Image extends BufferObject {

    /**
     *
     * Image pixel formats
     */
    public enum ImageFormat {

        /**
         * From awt BufferedImage
         */
        TYPE_4BYTE_ABGR(06, 4),
        /**
         * Image type RGBA 4 bits per pixel and component, ie 16 bit format.
         */
        RGBA4(0x8056, 2),
        /**
         * Image type RGB 555 + 1 bit alpha, 16 bit format.
         */
        RGB5_A1(0x8057, 2),
        /**
         * Image type RGB 565, 16 bit format.
         */
        RGB565(0x8D62, 2),
        /**
         * Image type RGB 888, 8 bits per component, 24 bit format.
         */
        RGB(0x1907, 3),
        /**
         * Image type RGBA 8888, 8 bits per component, 32 bit format.
         */
        RGBA(0x1908, 4),
        /**
         * 8 Bits luminance, 8 bits alpha, 16 bit format.
         */
        LUMINANCE_ALPHA(0x1909, 2),
        /**
         * 8 Bit luminance, 8 bit format
         */
        LUMINANCE(0x190A, 1),
        /**
         * 8 bit alpha format
         */
        ALPHA(0x1906, 1);

        public final int type;
        public final int size;

        ImageFormat(int type, int size) {
            this.type = type;
            this.size = size;
        }

        /**
         * Returns the enum for the specified int value, if found
         * 
         * @param type {@link BufferedImage} image type
         * @return The image format enum, or null if not found.
         */
        public static ImageFormat getImageFormat(int type) {
            for (ImageFormat imgFormat : ImageFormat.values()) {
                if (imgFormat.type == type) {
                    return imgFormat;
                }
            }
            return null;
        }

    }

    ImageFormat format;
    Buffer buffer;
    int width;
    int height;

    /**
     * Allocates the buffer to match the specified image size and format.
     * The image is ready to be filled with data.
     * 
     * @param width
     * @param height
     * @param format
     * @throws NullPointerException If format is null
     */
    public Image(int width, int height, ImageFormat format) {
        this.format = format;
        this.width = width;
        this.height = height;
        switch (format) {

        case RGB565:
        case RGB5_A1:
        case RGBA4:
        case RGB:
        case RGBA:
        case LUMINANCE_ALPHA:
        case LUMINANCE:
        case ALPHA:
            sizeInBytes = width * height * format.size;
            buffer = ByteBuffer.allocateDirect(sizeInBytes);
            break;
        default:
            throw new IllegalArgumentException(ErrorMessage.INVALID_TYPE + ", " + format);
        }
    }

    /**
     * Returns the buffer for image pixel data.
     * 
     * @return The buffer containing pixel data.
     */
    public Buffer getBuffer() {
        return buffer;
    }

    /**
     * Returns the ImageFormat of this image.
     * 
     * @return
     */
    public ImageFormat getFormat() {
        return format;
    }

    /**
     * Returns the width, in pixels of this image.
     * 
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height, in pixels of this image.
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }

}
