package com.nucleus.texture.j2se;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.nucleus.texturing.Image;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.ImageFactory;

public class J2SEImageFactory implements ImageFactory {

    public J2SEImageFactory() {
    }

    @Override
    public Image createImage(String name, float scaleX, float scaleY) throws IOException {
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

    /**
     * Creates a scaled copy of the image
     * 
     * @param source
     * @param width Width of scaled image
     * @param height Height of scaled image
     * @param type The type of image to return {@link BufferedImage#TYPE_INT_ARGB} or similar
     * @return Scaled copy of the source image
     */
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
     * Copies pixel data from the buffere image to the destination.
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

}
