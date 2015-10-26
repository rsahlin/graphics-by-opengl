package com.nucleus.texturing;

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
            break;
        case 3:
            c = new Convolution(Kernel.SIZE_3X3);
            c.set(new float[] { 1, 4, 1, 4, 4, 4, 1, 4, 1 }, 0, 0, Kernel.SIZE_3X3.size);
            break;
        case 4:
            c = new Convolution(Kernel.SIZE_4X4);
            c.set(new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, 0, 0, Kernel.SIZE_4X4.size);
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
                    1, 1, 1, 2, 2, 1, 1, 1,
                    1, 1, 2, 3, 3, 2, 1, 1,
                    1, 2, 3, 4, 4, 3, 2, 1,
                    1, 2, 3, 4, 4, 3, 2, 1,
                    1, 1, 2, 3, 3, 2, 1, 1,
                    1, 1, 1, 2, 2, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1

            }, 0, 0, Kernel.SIZE_8X8.size);
            break;
        default:
            c = new Convolution(Kernel.SIZE_8X8);

        }

        c.normalize(false);
        Image destination = new Image(width, height, ImageFormat.RGBA);
        c.process(source, destination);
        return destination;
    }

    @Override
    public Image createImage(int width, int height, ImageFormat format) {
        if (width <= 0 || height <= 0 || format == null) {
            throw new IllegalArgumentException(ILLEGAL_PARAMETER + width + ", " + height + " : " + format);
        }
        return new Image(width, height, format);
    }

}
