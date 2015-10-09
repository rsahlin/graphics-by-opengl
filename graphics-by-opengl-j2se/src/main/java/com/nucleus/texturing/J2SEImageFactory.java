package com.nucleus.texturing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
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
public class J2SEImageFactory implements ImageFactory {

    private final static String NULL_PARAMETER = "Null parameter";
    private static final String ILLEGAL_PARAMETER = "Illegal parameter";

    public J2SEImageFactory() {
    }

    @Override
    public Image createImage(String name, float scaleX, float scaleY) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = null;
        BufferedImage img = null;
        try {
            stream = classLoader.getResourceAsStream(name);
            img = ImageIO.read(stream);
            if (scaleX != 1 || scaleY != 1) {
                img = createScaledImage(img, (int) (scaleX * img.getWidth()), (int) (scaleY * img.getHeight()),
                        BufferedImage.TYPE_4BYTE_ABGR);
            } else if (img.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
                img = createImage(img, BufferedImage.TYPE_4BYTE_ABGR);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        Image image = new Image(img.getWidth(), img.getHeight(), ImageFormat.RGBA);
        copyPixels(img, image);

        return image;
    }

    public BufferedImage createScaledImage(BufferedImage source, int width, int height, int type) {
        BufferedImage scaled = new BufferedImage(width, height, type);

        while (!scaled.createGraphics().drawImage(source, 0, 0, width, height, null)) {
            System.out.println("waiting");
        }
        return scaled;
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
        int type = source.getType();
        Buffer buff = destination.getBuffer();
        destination.getBuffer().position(0);
        if (buff instanceof ByteBuffer) {
            copyPixels(source.getData().getDataBuffer(), type, (ByteBuffer) buff);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    /**
     * Internal method, copies pixels from the DataBuffer source to the destination.
     * Pixels are of the specified type.
     * 
     * @param source Source pixels
     * @param type The source type
     * @param destination Destination, destination shall be RGBA
     */
    private void copyPixels(DataBuffer source, int type, ByteBuffer destination) {
        if (source instanceof DataBufferByte) {
            copyPixels(((DataBufferByte) source).getData(), type, destination);
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

    /**
     * Copies pixel data from the byte array source to the destination.
     * The type (format) is
     * 
     * @param source
     * @param type
     * @param destination
     */
    private void copyPixels(byte[] source, int type, ByteBuffer destination) {

        byte[] rgba = new byte[4];
        switch (type) {
        case BufferedImage.TYPE_4BYTE_ABGR:
            int length = source.length;
            for (int index = 0; index < length;) {
                rgba[3] = source[index++];
                rgba[2] = source[index++];
                rgba[1] = source[index++];
                rgba[0] = source[index++];
                destination.put(rgba, 0, 4);
            }
            break;
        default:
            throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public Image createScaledImage(Image source, int width, int height, ImageFormat format) {
        if (source == null || width <= 0 || height <= 0 || format == null) {
            throw new IllegalArgumentException(ILLEGAL_PARAMETER + source + ", " + width + ", " + height + " : "
                    + format);
        }

        Image result = new Image(width, height, format);
        scaleImage(source, result);
        return result;
    }

    public void scaleImage(Image source, Image destination) {

    }

    @Override
    public Image createImage(int width, int height, ImageFormat format) {
        if (width <= 0 || height <= 0 || format == null) {
            throw new IllegalArgumentException(ILLEGAL_PARAMETER + width + ", " + height + " : " + format);
        }
        return new Image(width, height, format);
    }

}
