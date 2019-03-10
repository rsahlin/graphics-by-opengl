package com.nucleus.texturing;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.nucleus.ErrorMessage;
import com.nucleus.common.BufferUtils;
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

    public enum ColorModel {
        LINEAR(),
        SRGB();
    }

    /**
     *
     * Image pixel formats
     */
    public enum ImageFormat {

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

    }

    /**
     * Loaded image formats
     *
     */
    public enum SourceFormat {
        /**
         * From java.awt.image.BufferedImage
         */
        TYPE_4BYTE_ABGR(06, 4, ImageFormat.RGBA),
        TYPE_3BYTE_BGR(05, 3, ImageFormat.RGB),
        TYPE_INT_ARGB(02, 4, ImageFormat.RGBA),
        // TODO - use ImageFormat.RGB565 instead?
        TYPE_BYTE_INDEXED(13, 1, ImageFormat.RGB),
        /**
         * Bitmap with ARGB 8888 (eg Android)
         */
        TYPE_RGBA(-1, 4, ImageFormat.RGBA),
        /**
         * Bitmap with RGB 565 (eg Android)
         */
        TYPE_RGB565(-1, 2, ImageFormat.RGB565);

        public final int type;
        /**
         * The size in bytes of each pixel
         */
        public final int size;

        /**
         * The most closely matching imageformat that can be used when loading
         */
        public final ImageFormat imageFormat;

        SourceFormat(int type, int size, ImageFormat imageFormat) {
            this.type = type;
            this.size = size;
            this.imageFormat = imageFormat;
        }

        /**
         * Returns the enum for the specified awt buffered image int value, if found
         * 
         * @param type image type
         * @return The image format enum, or null if not found.
         */
        public static SourceFormat getFromAwtFormat(int type) {
            for (SourceFormat format : SourceFormat.values()) {
                if (format.type == type) {
                    return format;
                }
            }
            return null;
        }

    }

    ImageFormat format;
    ColorModel colorModel = ColorModel.LINEAR;
    ByteBuffer buffer;
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

    /**
     * Sets the colormodel, default is LINEAR
     * 
     * @param colorModel
     */
    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    /**
     * Returns the colormodel for the pixels in the bufferimage
     * 
     * @return
     */
    public ColorModel getColorModel() {
        return colorModel;
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
                buffer = BufferUtils.createByteBuffer(sizeInBytes);
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

    @Override
    public String toString() {
        return "Size " + width + ", " + height + ", " + resolution + ", " + format + "\n"
                + BufferUtils.getContentAsString(0, 100, buffer);
    }

}
