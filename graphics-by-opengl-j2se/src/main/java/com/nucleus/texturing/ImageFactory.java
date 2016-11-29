package com.nucleus.texturing;

import java.io.IOException;

import com.nucleus.texturing.Image.ImageFormat;

/**
 * Platform abstraction for creating images.
 * 
 * @author Richard Sahlin
 *
 */
public interface ImageFactory {

    /**
     * Definition of how pixels are stored for buffers that are used by the ImageFactory.
     * Used when images are loaded and are not in a known GL format {@link ImageFormat}
     * 
     * @author Richard Sahlin
     *
     */
    public enum PixelFormat {

        BYTE_BGR(),
        BYTE_ABGR(),
        BYTE_RGB(),
        BYTE_ARGB();

        private PixelFormat() {
        };

    }

    /**
     * Loads an image, the image must be in a format that is understood by the platform.
     * 
     * @param name The filename to load
     * @param format The image format of the created image (buffer)
     * @return The loaded image.
     * @throws IOException If there is an error loading the image.
     * @throws IllegalArgumentException If name is null
     */
    public Image createImage(String name, Image.ImageFormat format) throws IOException;

    /**
     * Creates a scaled copy of the image
     * 
     * @param source The source image
     * @param width Width of scaled image
     * @param height Height of scaled image
     * @param type The format of the image to create
     * @return Scaled copy of the source image in the specified format
     * @throws IllegalArgumentException If source or format is null, if width or height <= 0
     */
    public Image createScaledImage(Image source, int width, int height, ImageFormat format);

    /**
     * Creates an image with the specified size and format.
     * The returned images content will be undefined
     * 
     * @param width Width of image in pixels
     * @param height Height of image in pixels
     * @param format Bitmap format
     * @return The created and un-initialized (contents is undefined) image.
     * @throws IllegalArgumentException If format is null, if width or height <= 0
     */
    public Image createImage(int width, int height, ImageFormat format);

}
