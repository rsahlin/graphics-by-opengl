package com.nucleus.texturing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.nucleus.SimpleLogger;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.texturing.BufferImage.SourceFormat;

/**
 * Implementation of image factory using J2SE, in this implementation java.awt will be used.
 * 
 * @author Richard Sahlin
 *
 */
public class AWTImageFactory extends BaseImageFactory implements ImageFactory {

    private class IndexedToByte {

        /**
         * Creates a new IndexedToByte for the specified format
         * 
         * @param sourceFormat
         * @param data
         */
        IndexedToByte(SourceFormat sourceFormat, byte[] data) {
            this.sourceFormat = sourceFormat;
            this.resultData = data;
        }

        SourceFormat sourceFormat;
        byte[] resultData;
    }

    public AWTImageFactory() {
    }

    @Override
    public BufferImage createImage(String name, BufferImage.ImageFormat format) throws IOException {
        long start = System.currentTimeMillis();
        BufferedImage img = loadImage(name);
        BufferImage image = new BufferImage(img.getWidth(), img.getHeight(),
                format != null ? format : SourceFormat.getFromAwtFormat(img.getType()).imageFormat);
        copyPixels(img, image);
        FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_IMAGE, " " + name, start,
                System.currentTimeMillis());
        return image;
    }

    private BufferedImage loadImage(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        long start = System.currentTimeMillis();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = null;
        try {
            stream = classLoader.getResourceAsStream(name);
            if (stream == null) {
                throw new FileNotFoundException(name);
            }
            BufferedImage img = ImageIO.read(stream);
            int delta = (int) (System.currentTimeMillis() - start) + 1;
            int size = img.getWidth() * img.getHeight();
            SourceFormat sourceFormat = SourceFormat.getFromAwtFormat(img.getType());
            SimpleLogger.d(getClass(),
                    "Loaded image " + name + ", in format: " + sourceFormat + " " + img.getWidth() + " X "
                            + img.getHeight()
                            + " in " + delta + " millis [" + size / delta + "K/s]");
            return img;
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
            SimpleLogger.d(getClass(), "waiting");
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
    public void copyPixels(BufferedImage source, BufferImage destination) {
        SourceFormat sourceFormat = SourceFormat.getFromAwtFormat(source.getType());
        long start = System.currentTimeMillis();
        byte[] data = getImageData(source);
        if (sourceFormat == SourceFormat.TYPE_BYTE_INDEXED) {
            IndexedToByte result = handleByteIndexed(source, data);
            sourceFormat = result.sourceFormat;
            data = result.resultData;
        }
        copyPixels(data, sourceFormat, destination);
        SimpleLogger.d(getClass(), "copyPixels took " + (System.currentTimeMillis() - start) + " millis");
    }

    private byte[] getImageData(BufferedImage source) {
        DataBuffer dataBuffer = source.getData().getDataBuffer();
        if (dataBuffer instanceof DataBufferByte) {
            return ((DataBufferByte) dataBuffer).getData();
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    private IndexedToByte handleByteIndexed(BufferedImage source, byte[] data) {
        if ((source.getColorModel().hasAlpha())) {
            byte[] resultData = byteIndexedToRGBA((IndexColorModel) source.getColorModel(), data);
            IndexedToByte result = new IndexedToByte(SourceFormat.TYPE_RGBA, resultData);
            return result;
        } else {
            byte[] resultData = byteIndexedToBGR((IndexColorModel) source.getColorModel(), data);
            IndexedToByte result = new IndexedToByte(SourceFormat.TYPE_RGB, resultData);
            return result;
        }
    }

    private byte[] byteIndexedToBGR(IndexColorModel icm, byte[] data) {
        int length = data.length;
        byte[] result = new byte[length * 3];
        int mapSize = icm.getMapSize();
        byte[] r = new byte[mapSize];
        byte[] g = new byte[mapSize];
        byte[] b = new byte[mapSize];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        int index = 0;
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (data[i] & 0x0ff);
            result[index++] = r[value];
            result[index++] = g[value];
            result[index++] = b[value];
        }
        return result;
    }

    private byte[] byteIndexedToRGBA(IndexColorModel icm, byte[] data) {
        int length = data.length;
        byte[] result = new byte[length * 4];
        int mapSize = icm.getMapSize();
        byte[] a = new byte[mapSize];
        byte[] r = new byte[mapSize];
        byte[] g = new byte[mapSize];
        byte[] b = new byte[mapSize];
        icm.getAlphas(a);
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        int index = 0;
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (data[i] & 0x0ff);
            result[index++] = r[value];
            result[index++] = g[value];
            result[index++] = b[value];
            result[index++] = a[value];
        }
        return result;
    }

}
