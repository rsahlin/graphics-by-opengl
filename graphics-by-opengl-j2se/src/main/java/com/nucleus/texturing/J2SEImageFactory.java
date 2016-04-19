package com.nucleus.texturing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import com.nucleus.texturing.Image.ImageFormat;

/**
 * Implementation of image factory using J2SE, in this implementation java.awt will be used.
 * TODO Consider moving image factory to a separate package.
 * TODO Rename to AWTImageFactory - this class is not J2SE only, it needs AWT in order to function
 * 
 * @author Richard Sahlin
 *
 */
public class J2SEImageFactory extends BaseImageFactory implements ImageFactory {

    private final static String NULL_PARAMETER = "Null parameter";
    private static final String ILLEGAL_PARAMETER = "Illegal parameter";

    public J2SEImageFactory() {
    }

    @Override
    public Image createImage(String name, Image.ImageFormat format, float scaleX, float scaleY) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = null;
        try {
            stream = classLoader.getResourceAsStream(name);
            BufferedImage img = ImageIO.read(stream);
            if (img.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
                img = createImage(img, BufferedImage.TYPE_4BYTE_ABGR);
            }
            Image image = new Image(img.getWidth(), img.getHeight(), format);
            copyPixels(img, image);
            if (scaleX != 1 || scaleY != 1) {
                return createScaledImage(image, (int) (scaleX * img.getWidth()), (int) (scaleY * img.getHeight()),
                        ImageFormat.RGBA);
            }
            return image;

        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Creates an image of specified type from the source, use this when the source type is not as desired.
     * 
     * @param source The source image
     * @param type destination image type
     * @return Copy of source image of specified type
     */
    public BufferedImage createImage(BufferedImage source, int type) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), type);
        while (!copy.createGraphics().drawImage(source, 0, 0, null)) {
            System.out.println("waiting");
        }
        return copy;
    }

    /**
     * Copies pixel data from the buffered image to the destination.
     * This will copy all of the data (image)
     * 
     * @param source
     * @param destination
     */
    public void copyPixels(BufferedImage source, Image destination) {
        ImageFormat sourceFormat = ImageFormat.getImageFormat(source.getType());
        copyPixels(source.getData().getDataBuffer(), sourceFormat, destination);
    }

    /**
     * Internal method, copies pixels from the DataBuffer source to the destination.
     * Pixels are of the specified type.
     * 
     * @param source Source pixels
     * @param sourceFormat The source type
     * @param destination The destination image
     */
    private void copyPixels(DataBuffer source, ImageFormat sourceFormat, Image destination) {
        if (source instanceof DataBufferByte) {
            copyPixels(((DataBufferByte) source).getData(), sourceFormat, destination);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    /**
     * Copies the pixels from the source to the destination doing a format conversion if needed.
     * This will copy the whole image in a packed manner, it will not take width or height into consideration.
     * 
     * @param source
     * @param sourceFormat
     * @param destination
     * @param destinationFormat
     */
    public void copyPixels(int[] source, PixelFormat sourceFormat, IntBuffer destination, ImageFormat destinationFormat) {

    }


}
