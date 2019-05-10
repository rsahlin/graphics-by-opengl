package com.nucleus.texturing;

import java.awt.image.BufferedImage;
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
 * TODO Consider moving image factory to a separate package.
 * TODO Rename to AWTImageFactory - this class is not J2SE only, it needs AWT in order to function
 * 
 * @author Richard Sahlin
 *
 */
public class J2SEImageFactory extends BaseImageFactory implements ImageFactory {

    public J2SEImageFactory() {
    }

    @Override
    public BufferImage createImage(String name, BufferImage.ImageFormat format) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stream = null;
        try {
            long start = System.currentTimeMillis();
            stream = classLoader.getResourceAsStream(name);
            if (stream == null) {
                throw new FileNotFoundException(name);
            }
            BufferedImage img = ImageIO.read(stream);
            SourceFormat sourceFormat = SourceFormat.getFromAwtFormat(img.getType());
            int delta = (int) (System.currentTimeMillis() - start);
            delta = delta > 0 ? delta : 1;
            int size = img.getWidth() * img.getHeight();
            SimpleLogger.d(getClass(),
                    "Loaded image " + name + ", in format: " + sourceFormat + " " + img.getWidth() + " X "
                            + img.getHeight()
                            + " in " + delta + " millis [" + size / delta + "K/s]");
            BufferImage image = new BufferImage(img.getWidth(), img.getHeight(),
                    format != null ? format : sourceFormat.imageFormat);
            copyPixels(img, sourceFormat, image);
            FrameSampler.getInstance().logTag(FrameSampler.Samples.CREATE_IMAGE, " " + name, start,
                    System.currentTimeMillis());
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
            SimpleLogger.d(getClass(), "waiting");
        }
        return copy;
    }

    /**
     * Copies pixel data from the buffered image to the destination.
     * This will copy all of the data (image)
     * 
     * @param source
     * @oaram sourceFormat
     * @param destination
     */
    public void copyPixels(BufferedImage source, SourceFormat sourceFormat, BufferImage destination) {
        long start = System.currentTimeMillis();
        if (source.getData().getDataBuffer() instanceof DataBufferByte) {
            byte[] data = ((DataBufferByte) source.getData().getDataBuffer()).getData();
            switch (sourceFormat) {
                case TYPE_BYTE_INDEXED:
                    // Make sure no alpha in source - not supported
                    if ((source.getColorModel().hasAlpha())) {
                        data = byteIndexedToRGBA((IndexColorModel) source.getColorModel(), data);
                        sourceFormat = SourceFormat.TYPE_RGBA;
                    } else {
                        data = byteIndexedToBGR((IndexColorModel) source.getColorModel(), data);
                        sourceFormat = SourceFormat.TYPE_3BYTE_BGR;
                    }
                    break;
                default:
                    break;
            }
            copyPixels(data, sourceFormat, destination);
            SimpleLogger.d(getClass(), "copyPixels took " + (System.currentTimeMillis() - start) + " millis");
        } else {
            throw new IllegalArgumentException("Not implemented");
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
            result[index++] = b[value];
            result[index++] = g[value];
            result[index++] = r[value];
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
