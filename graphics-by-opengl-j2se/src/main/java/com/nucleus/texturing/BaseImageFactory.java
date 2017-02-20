package com.nucleus.texturing;

import java.nio.ByteBuffer;

import com.nucleus.ErrorMessage;
import com.nucleus.texturing.Convolution.Kernel;
import com.nucleus.texturing.Image.ImageFormat;

/**
 * Implementation of platform agnostic image methods using the Nucleus {@link Image} class;
 * Some platforms will provide native functions for these methods.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class BaseImageFactory implements ImageFactory {

    protected final static String ILLEGAL_PARAMETER = "Illegal parameter: ";
    protected final static String NULL_PARAMETER = "Null parameter";

    @Override
    public Image createScaledImage(Image source, int width, int height, ImageFormat format) {
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
            // c = new Convolution(Kernel.SIZE_4X4);
            // c.set(new float[] { 1, 1, 1, 1,
            // 1, 3, 2, 1,
            // 1, 2, 3, 1,
            // 1, 1, 1, 1 }, 0, 0, Kernel.SIZE_4X4.size);
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
            c = new Convolution(Kernel.SIZE_5X5);
            c.set(new float[] { 1, 2, 3, 2, 1,
                    2, 3, 4, 3, 2,
                    3, 4, 5, 4, 3,
                    2, 3, 4, 3, 2,
                    1, 2, 3, 2, 1 }, 0, 0, Kernel.SIZE_5X5.size);
            break;
        case 6:
        case 7:
        case 8:
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
            c = new Convolution(Kernel.SIZE_8X8);

        }

        c.normalize(false);
        Image destination = new Image(width, height, source.getFormat());
        c.process(source, destination);
        // if (scale >= 2) {
        // return sharpen(destination);
        // }
        return destination;
    }

    private Image sharpen(Image source) {
        Convolution c = new Convolution(Kernel.SIZE_3X3);
        c.set(new float[] { 0.1f, -0.2f, 0.1f, -0.2f, 1.8f, -0.2f, 0.1f, -0.2f, 0.1f }, 0, 0, Kernel.SIZE_3X3.size);
        c.normalize(false);
        return c.process(source);
    }

    @Override
    public Image createImage(int width, int height, ImageFormat format) {
        if (width <= 0 || height <= 0 || format == null) {
            throw new IllegalArgumentException(ILLEGAL_PARAMETER + width + ", " + height + " : " + format);
        }
        return new Image(width, height, format);
    }

    /**
     * Copies pixel data from the byte array source to the destination.
     * The type (format) is
     * 
     * @param source
     * @param sourceFormat The source type
     * @param destination
     */
    protected void copyPixels(byte[] source, ImageFormat sourceFormat, Image destination) {

        ByteBuffer buffer = (ByteBuffer) destination.getBuffer().rewind();
        switch (sourceFormat) {
        case ABGR4:
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
                copyPixels_4BYTE_ABGR_TO_RGB(source, buffer);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + destination.getFormat());
            }
            break;
        case RGBA:
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
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + destination.getFormat());
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
        byte[] rgba = new byte[4];
        int length = source.length;
        for (int index = 0; index < length;) {
            rgba[0] = source[index++];
            rgba[1] = source[index++];
            rgba[2] = source[index++];
            rgba[3] = source[index++];
            destination.put(rgba, 0, rgba.length);
        }
    }

    /**
     * Straight copy from source to destination
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_RGBA_TO_RGB(byte[] source, ByteBuffer destination) {
        byte[] rgb = new byte[3];
        int length = source.length;
        for (int index = 0; index < length;) {
            rgb[0] = source[index++];
            rgb[1] = source[index++];
            rgb[2] = source[index++];
            index++;
            destination.put(rgb, 0, rgb.length);
        }
    }

    /**
     * Straight copy from source to destination
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_ABGR_TO_RGBA(byte[] source, ByteBuffer destination) {
        byte[] rgba = new byte[4];
        int length = source.length;
        for (int index = 0; index < length;) {
            rgba[3] = source[index++];
            rgba[2] = source[index++];
            rgba[1] = source[index++];
            rgba[0] = source[index++];
            destination.put(rgba, 0, rgba.length);
        }
    }

    /**
     * Straight copy from source to destination - just swap ABGR to RGBA
     * 
     * @param source
     * @param destination
     */
    protected void copyPixels_4BYTE_ABGR_TO_RGB(byte[] source, ByteBuffer destination) {
        byte[] rgb = new byte[3];
        int length = source.length;
        for (int index = 0; index < length;) {
            index++;
            rgb[2] = source[index++];
            rgb[1] = source[index++];
            rgb[0] = source[index++];
            destination.put(rgb, 0, rgb.length);
        }
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
