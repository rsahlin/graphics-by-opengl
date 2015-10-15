package com.nucleus.texturing;

import java.nio.Buffer;
import java.nio.ByteBuffer;

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
         * Image format RGBA 4 bits per pixel and component, ie 16 bit format.
         */
        RGBA4(0x8056, 2),
        /**
         * Image format RGB 555 + 1 bit alpha, 16 bit format.
         */
        RGB5_A1(0x8057, 2),
        /**
         * Image format RGB 565, 16 bit format.
         */
        RGB565(0x8D62, 2),
        /**
         * Image format RGB 888, 8 bits per component, 24 bit format.
         */
        RGB(0x1907, 3),
        /**
         * Image format RGBA 8888, 8 bits per component, 32 bit format.
         */
        RGBA(0x1908, 4);

        public final int format;
        public final int size;

        ImageFormat(int format, int size) {
            this.format = format;
            this.size = size;
        }

        /**
         * Returns the int format.
         * 
         * @return
         */
        public int getFormat() {
            return format;
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
            sizeInBytes = width * height * 2;
            buffer = ByteBuffer.allocateDirect(sizeInBytes);
            break;
        case RGB:
            sizeInBytes = width * height * 3;
            buffer = ByteBuffer.allocateDirect(sizeInBytes);
            break;
        case RGBA:
            sizeInBytes = width * height * 4;
            buffer = ByteBuffer.allocateDirect(sizeInBytes);
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
