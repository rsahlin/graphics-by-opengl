package com.nucleus.texturing;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.nucleus.ErrorMessage;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.BufferImage.SourceFormat;
import com.nucleus.texturing.Convolution.Kernel;

/**
 * Implementation of platform agnostic image methods using the Nucleus {@link BufferImage} class;
 * Some platforms will provide native functions for these methods.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class BaseImageFactory implements ImageFactory {

    protected final static String ILLEGAL_PARAMETER = "Illegal parameter: ";
    protected final static String NULL_PARAMETER = "Null parameter";
    private final static String INVALID_SCALE = "Invalid scale";
    protected final static String NO_FACTORY = "No ImageFactory instance set - call ";

    protected static ImageFactory factory;

    /**
     * Sets the ImageFactory instance, must be called before calling {@link #getInstance()}
     * 
     * @param factory
     */
    public static void setFactory(ImageFactory factory) {
        BaseImageFactory.factory = factory;
    }

    /**
     * Returns the ImageFactory - an image factory instance must be set before calling this method.
     * 
     * @return
     * @throws IllegalArgumentException If no factory instance has been set.
     */
    public static ImageFactory getInstance() {
        if (factory == null) {
            throw new IllegalArgumentException(NO_FACTORY);
        }
        return factory;
    }

    @Override
    public BufferImage createScaledImage(BufferImage source, int width, int height, ImageFormat format,
            RESOLUTION resolution) {
        if (format == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(ILLEGAL_PARAMETER + width + ", " + height);
        }

        int scale = (source.getWidth() / width + source.getHeight() / height) / 2;
        Convolution c = null;
        switch (scale) {
            case 1:
            case 2:
                c = new Convolution(Kernel.SIZE_2X2);
                c.set(new float[] { 1, 1, 1, 1 }, 0, 0, Kernel.SIZE_2X2.size);
                break;
            case 3:
                c = new Convolution(Kernel.SIZE_3X3);
                c.set(new float[] { 1, 4, 1, 4, 4, 4, 1, 4, 1 }, 0, 0, Kernel.SIZE_3X3.size);
                break;
            case 4:
                c = new Convolution(Kernel.SIZE_4X4);
                c.set(new float[] { 1, 1, 1, 1,
                        1, 3, 2, 1,
                        1, 2, 3, 1,
                        1, 1, 1, 1 }, 0, 0, Kernel.SIZE_4X4.size);
                break;
            case 5:
            case 6:
            case 7:
                c = new Convolution(Kernel.SIZE_5X5);
                c.set(new float[] { 1, 2, 3, 2, 1,
                        2, 3, 4, 3, 2,
                        3, 4, 5, 4, 3,
                        2, 3, 4, 3, 2,
                        1, 2, 3, 2, 1 }, 0, 0, Kernel.SIZE_5X5.size);
                break;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                c = new Convolution(Kernel.SIZE_8X8);
                c.set(new float[] {
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1,
                }, 0, 0, Kernel.SIZE_8X8.size);
                break;
            case 16:
                c = new Convolution(Kernel.SIZE_16X16);
                c.set(new float[] {
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
                }, 0, 0, Kernel.SIZE_16X16.size);
                break;
            default:
                c = new Convolution(Kernel.SIZE_16X16);

        }

        c.normalize(false);
        BufferImage destination = new BufferImage(width, height, source.getFormat(), resolution);
        c.process(source, destination);
        // if (scale >= 2) {
        // return sharpen(destination);
        // }
        return destination;
    }

    @Override
    public BufferImage createImage(String name, float scaleX, float scaleY, ImageFormat format, RESOLUTION resolution)
            throws IOException {
        if (name == null || format == null) {
            throw new IllegalArgumentException(NULL_PARAMETER);
        }
        if (scaleX <= 0 || scaleY <= 0) {
            throw new IllegalArgumentException(INVALID_SCALE);
        }
        BufferImage image = createImage(name, format);
        return createScaledImage(image, (int) (image.width * scaleX), (int) (image.height * scaleY), format,
                resolution);
    }

    private BufferImage sharpen(BufferImage source) {
        Convolution c = new Convolution(Kernel.SIZE_3X3);
        c.set(new float[] { 0.1f, -0.2f, 0.1f, -0.2f, 1.8f, -0.2f, 0.1f, -0.2f, 0.1f }, 0, 0, Kernel.SIZE_3X3.size);
        c.normalize(false);
        return c.process(source);
    }

    @Override
    public BufferImage createImage(int width, int height, ImageFormat format) {
        if (width <= 0 || height <= 0 || format == null) {
            throw new IllegalArgumentException(ILLEGAL_PARAMETER + width + ", " + height + " : " + format);
        }
        return new BufferImage(width, height, format);
    }

    /**
     * Copies pixel data from the byte array source to the destination.
     * The type (format) is
     * 
     * @param source
     * @param sourceFormat The source type
     * @param destination
     */
    protected void copyPixels(byte[] source, SourceFormat sourceFormat, BufferImage destination) {
        ByteBuffer buffer = (ByteBuffer) destination.getBuffer().rewind();
        switch (sourceFormat) {
            case TYPE_4BYTE_ABGR:
                switch (destination.getFormat()) {
                    case RGBA:
                        copyPixels_4BYTE_ABGR_TO_RGBA(source, buffer);
                        break;
                    case LUMINANCE_ALPHA:
                        copyPixels_4BYTE_ABGR_TO_LUMINANCE_ALPHA(source, buffer);
                        break;
                    case RGB565:
                        copyPixels_4BYTE_ABGR_TO_RGB565(source, buffer);
                        break;
                    case RGB5_A1:
                        copyPixels_4BYTE_ABGR_TO_RGB5551(source, buffer);
                        break;
                    case RGB:
                        copyPixels_4BYTE_ABGR_TO_RGB(source, buffer, destination.getWidth(), destination.getHeight());
                        break;
                    default:
                        throw new IllegalArgumentException(
                                ErrorMessage.NOT_IMPLEMENTED.message + destination.getFormat());
                }
                break;
            case TYPE_INT_ARGB:
                switch (destination.getFormat()) {
                    case RGBA:
                        copyPixels_4BYTE_RGBA_TO_RGBA(source, buffer);
                        break;
                    case RGB:
                        copyPixels_4BYTE_RGBA_TO_RGB(source, buffer);
                        break;
                    case LUMINANCE_ALPHA:
                        copyPixels_4BYTE_RGBA_TO_LUMINANCE_ALPHA(source, buffer);
                        break;
                    case RGB565:
                        copyPixels_4BYTE_RGBA_TO_RGB565(source, buffer);
                        break;
                    case RGB5_A1:
                        copyPixels_4BYTE_RGBA_TO_RGB5551(source, buffer);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                ErrorMessage.NOT_IMPLEMENTED.message + destination.getFormat());
                }
                break;
            case TYPE_3BYTE_BGR:
                switch (destination.getFormat()) {
                    case RGB:
                        copyPixels_3BYTE_BGR_TO_RGB(source, buffer);
                        break;
                    case RGBA:
                        throw new IllegalArgumentException(
                                "Can't convert from sourceformat " + sourceFormat + " to destination "
                                        + destination.getFormat());
                    default:
                        throw new IllegalArgumentException(
                                ErrorMessage.NOT_IMPLEMENTED.message + destination.getFormat());
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + sourceFormat);
        }
    }

    /**
     * Copies the 4 byte RGBA to 16 bit luminance alpha
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_RGBA_TO_LUMINANCE_ALPHA(byte[] source, ByteBuffer destination) {
        byte[] la = new byte[2];
        int length = source.length;
        for (int index = 0; index < length;) {
            la[0] = source[index++];
            la[1] = source[index++];
            index += 2;
            destination.put(la, 0, 2);
        }
    }

    /**
     * Copies the 4 byte RGBA to 16 bit RGB565
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_RGBA_TO_RGB565(byte[] source, ByteBuffer destination) {
        byte[] rgb = new byte[2];
        int length = source.length;
        int r, g, b, a;
        int rgbint;
        for (int index = 0; index < length;) {
            r = (source[index++] & 0x0ff);
            g = (source[index++] & 0x0ff);
            b = (source[index++] & 0x0ff);
            a = (source[index++] & 0x0ff);
            rgbint = (b >> 3) | ((g >> 2) << 5) | ((r >> 3) << 11);
            rgb[0] = (byte) (rgbint & 0xff);
            rgb[1] = (byte) (rgbint >> 8);
            destination.put(rgb, 0, 2);
        }
    }

    protected void copyPixels_4BYTE_RGBA_TO_RGB5551(byte[] source, ByteBuffer destination) {
        byte[] rgb = new byte[2];
        int length = source.length;
        int r, g, b, a;
        int rgbint;
        for (int index = 0; index < length;) {
            r = (source[index++] & 0x0ff);
            g = (source[index++] & 0x0ff);
            b = (source[index++] & 0x0ff);
            a = source[index++] == 0 ? 0 : 1;
            rgbint = (b >> 3) << 1 | ((g >> 3) << 6) | ((r >> 3) << 11) | a;
            rgb[0] = (byte) (rgbint & 0xff);
            rgb[1] = (byte) (rgbint >> 8);
            destination.put(rgb, 0, 2);
        }
    }

    /**
     * Straight copy from source to destination
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_RGBA_TO_RGBA(byte[] source, ByteBuffer destination) {
        destination.position(0);
        destination.put(source);
    }

    /**
     * Straight copy from source to destination
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_RGBA_TO_RGB(byte[] source, ByteBuffer destination) {
        int length = source.length;
        byte[] rgb = new byte[(int) (length * 0.75)];
        int index = 0;
        int write = 0;
        while (index < length) {
            rgb[write++] = source[index++];
            rgb[write++] = source[index++];
            rgb[write++] = source[index++];
            index++;
        }
        destination.position(0);
        destination.put(rgb);
    }

    /**
     * Straight copy from source to destination
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_ABGR_TO_RGBA(byte[] source, ByteBuffer destination) {
        byte[] rgba = new byte[source.length];
        int length = source.length;
        int index = 0;
        while (index < length) {
            rgba[index + 3] = source[index];
            rgba[index + 2] = source[index + 1];
            rgba[index + 1] = source[index + 2];
            rgba[index] = source[index + 3];
            index += 4;
        }
        destination.position(0);
        destination.put(rgba);
    }

    /**
     * Straight copy from source to destination - just swap ABGR to RGBA
     * 
     * @param source
     * @param destination
     * @param width
     * @param height
     */
    protected void copyPixels_4BYTE_ABGR_TO_RGB(byte[] source, ByteBuffer destination, int width, int height) {
        byte[] rgb = new byte[3 * width];
        int length = rgb.length;
        int s = 0;
        for (int y = 0; y < height; y++) {
            int d = 0;
            for (int x = 0; x < width; x++) {
                s++;
                rgb[d + 2] = source[s++];
                rgb[d + 1] = source[s++];
                rgb[d] = source[s++];
                d += 3;
            }
            destination.put(rgb, 0, length);
        }
    }

    /**
     * Straight copy from source to destination - just swap BGR to RGB
     * Source will be overwritten
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_3BYTE_BGR_TO_RGB(byte[] source, ByteBuffer destination) {
        int d = 0;
        int length = source.length;
        byte r;
        byte g;
        byte b;
        for (int s = 0; s < length; s += 3) {
            b = source[s];
            g = source[s + 1];
            r = source[s + 2];
            source[d++] = r;
            source[d++] = g;
            source[d++] = b;
        }
        destination.put(source, 0, length);
    }

    /**
     * Copies the 4 byte ABGR to 16 bit luminance alpha
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_ABGR_TO_LUMINANCE_ALPHA(byte[] source, ByteBuffer destination) {
        byte[] la = new byte[2];
        int length = source.length;
        for (int index = 0; index < length;) {
            la[0] = source[index++];
            la[1] = source[index++];
            index += 2;
            destination.put(la, 0, 2);
        }
    }

    /**
     * Copies the 4 byte ABGR to 16 bit RGB565
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_ABGR_TO_RGB565(byte[] source, ByteBuffer destination) {
        byte[] rgb = new byte[2];
        int length = source.length;
        int r, g, b, a;
        int rgbint;
        for (int index = 0; index < length;) {
            a = (source[index++] & 0x0ff);
            r = (source[index++] & 0x0ff);
            g = (source[index++] & 0x0ff);
            b = (source[index++] & 0x0ff);
            rgbint = (r >> 3) | ((g >> 2) << 5) | ((b >> 3) << 11);
            rgb[0] = (byte) (rgbint & 0xff);
            rgb[1] = (byte) (rgbint >> 8);
            destination.put(rgb, 0, 2);
        }
    }

    protected void copyPixels_4BYTE_ABGR_TO_RGB5551(byte[] source, ByteBuffer destination) {
        byte[] rgb = new byte[2];
        int length = source.length;
        int r, g, b, a;
        int rgbint;
        for (int index = 0; index < length;) {
            a = source[index++] == 0 ? 0 : 1;
            r = (source[index++] & 0x0ff);
            g = (source[index++] & 0x0ff);
            b = (source[index++] & 0x0ff);
            rgbint = (r >> 3) << 1 | ((g >> 3) << 6) | ((b >> 3) << 11) | a;
            rgb[0] = (byte) (rgbint & 0xff);
            rgb[1] = (byte) (rgbint >> 8);
            destination.put(rgb, 0, 2);
        }
    }
}
