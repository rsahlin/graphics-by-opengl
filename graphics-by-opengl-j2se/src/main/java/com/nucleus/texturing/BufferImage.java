package com.nucleus.texturing;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.nucleus.ErrorMessage;
import com.nucleus.geometry.BufferObject;
import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * Container for pixel based images that can be used for texturing in OpenGL, this is for a straightforward
 * image. It does not contain LOD information.
 * 
 * @author Richard Sahlin
 *
 */
public class BufferImage extends BufferObject {

    /**
     *
     * Image pixel formats
     */
    public enum ImageFormat {

    /**
     * From java.awt.image.BufferedImage
     */
    ABGR4(06, 4),
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
    ALPHA(0x1906, 1),
    /**
     * Depth texture 16
     */
    DEPTH_16(0x1403, 2),
    /**
     * Depth texture 24 bits
     */
    DEPTH_24(0x1405, 3),
    /**
     * Float depth texture
     */
    DEPTH_32F(0x1406, 4);

        public final int type;
        /**
         * The size in bytes of each pixel
         */
        public final int size;

        ImageFormat(int type, int size) {
            this.type = type;
            this.size = size;
        }

        /**
         * Returns the enum for the specified int value, if found
         * 
         * @param type image type
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
     * The resolution the image was created for, if specified.
     */
    RESOLUTION resolution;

    /**
     * Allocates the buffer to match the specified image size and format.
     * The image is ready to be filled with data.
     * 
     * @param width
     * @param height
     * @param format
     * @throws NullPointerException If format is null
     */
    public BufferImage(int width, int height, ImageFormat format, RESOLUTION resolution) {
        super(width * height * format.size);
        create(width, height, format, resolution);
    }

    /**
     * Allocates the buffer to match the specified image size and format.
     * The image is ready to be filled with data.
     * 
     * @param width
     * @param height
     * @param format
     * @throws NullPointerException If format is null
     */
    public BufferImage(int width, int height, ImageFormat format) {
        super(width * height * format.size);
        create(width, height, format, null);
    }

    protected void create(int width, int height, ImageFormat format, RESOLUTION resolution) {
        this.format = format;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
        switch (format) {

            case RGB565:
            case RGB5_A1:
            case RGBA4:
            case RGB:
            case RGBA:
            case LUMINANCE_ALPHA:
            case LUMINANCE:
            case ALPHA:
                buffer = ByteBuffer.allocateDirect(sizeInBytes).order(ByteOrder.nativeOrder());
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

    /**
     * Returns the resolution of the image, if specified.
     * 
     * @return Image resolution or null
     */
    public RESOLUTION getResolution() {
        return resolution;
    }

    /**
     * Calls destroy() on the array of images
     * 
     * @param images
     */
    public static void destroyImages(BufferImage[] images) {
        for (BufferImage image : images) {
            image.destroy();
        }
    }

    /**
     * Release all resources allocated by this image
     */
    public void destroy() {
        format = null;
        buffer = null;
        width = 0;
        height = 0;
    }

}
