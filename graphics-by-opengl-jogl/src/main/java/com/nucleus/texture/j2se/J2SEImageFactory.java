package com.nucleus.texture.j2se;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
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
        // TODO Auto-generated constructor stub
    }

    @Override
    public Image createImage(String name, float scaleX, float scaleY) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = null;
        BufferedImage img = null;
        try {
            stream = classLoader.getResourceAsStream(name);
            img = ImageIO.read(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        DataBuffer buffer = img.getRaster().getDataBuffer();
        Image image = new Image(img.getWidth(), img.getHeight(), ImageFormat.RGBA);
        copyPixels(buffer, image);

        return image;
    }

    /**
     * Copies the data from a {@link DataBuffer} source to the destination, all source data will be copied.
     * 
     * @param source
     * @param destination
     */
    public void copyPixels(DataBuffer source, Image destination) {
        if (source instanceof DataBufferInt) {
            copyPixels((DataBufferInt) source, destination);
        } else if (source instanceof DataBufferByte) {
            copyPixels((DataBufferByte) source, destination);
        } else {
            throw new IllegalArgumentException("Not implemented support for " + source);
        }
    }

    /**
     * Copies pixel data from the int databuffer to the destination.
     * This will copy all of the data (image)
     * 
     * @param source
     * @param destination
     */
    public void copyPixels(DataBufferInt source, Image destination) {
        Buffer buff = destination.getBuffer();
        destination.getBuffer().position(0);
        if (buff instanceof ByteBuffer) {
            ByteBuffer bytes = (ByteBuffer) buff;
            bytes.asIntBuffer().put(source.getData());
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    public void copyPixels(DataBufferByte source, Image destination) {
        Buffer buff = destination.getBuffer();
        if (buff instanceof ByteBuffer) {
            ByteBuffer bytes = (ByteBuffer) buff;
            bytes.put(source.getData());
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

}
