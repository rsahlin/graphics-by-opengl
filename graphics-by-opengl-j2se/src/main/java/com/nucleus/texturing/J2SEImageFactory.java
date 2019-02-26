package com.nucleus.texturing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
            SimpleLogger.d(getClass(), "Loaded image " + name + ", in format: " + sourceFormat);
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
        if (source.getData().getDataBuffer() instanceof DataBufferByte) {
            switch (sourceFormat) {
                case TYPE_BYTE_INDEXED:
                    // Make sure no alpha in source - not supported
                    if ((source.getColorModel().hasAlpha())) {
                        throw new IllegalArgumentException("Alpha not supported in " + sourceFormat);
                    }
            }
            copyPixels(((DataBufferByte) source.getData().getDataBuffer()).getData(), sourceFormat, destination);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

}
