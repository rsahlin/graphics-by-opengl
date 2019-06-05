package com.nucleus.texturing;

import java.nio.ByteBuffer;

import com.nucleus.texturing.BufferImage.ImageFormat;

/**
 * A simple image convolution (kernal) filter.
 * Used to filter images, for instance when mip-maps are created at runtime.
 * The matrix can have different sizes and the weight for each position can be set.
 * Call {@link #normalize()} before processing to normalize the matrix values.
 * Only ByteBuffer backed images of format RGBA are supported, a simple addition could add support for RGB images.
 * Please note that this is just a reference implementation, this type of processing should not be
 * done on the CPU in Java.
 * The kernel filter position cannot be specified, they are fixed in the different processing functions.
 * Next step is to port this to OpenGL.
 * 
 * @author Richard Sahlin
 *
 */
public class Convolution {

    public enum Kernel {
        SIZE_2X2(4, 2),
        SIZE_3X3(9, 3),
        SIZE_4X4(16, 4),
        SIZE_5X5(25, 5),
        SIZE_8X8(64, 8),
        SIZE_16X16(256, 16);

        public final int size;
        public final int width;

        private Kernel(int size, int width) {
            this.size = size;
            this.width = width;
        }

        /**
         * Returns an array of float values that are uniform across the kernel
         * 
         * @param value
         * @return Float array with matching size for the kernel filled with value
         */
        public float[] createDefaultKernel(float value) {
            float[] result = new float[size];
            for (int i = 0; i < size; i++) {
                result[i] = value;
            }
            return result;
        }

    }

    public float[] matrix;
    private Kernel kernel;
    int width;
    int height;
    byte[] pixelRow3 = new byte[12];

    /**
     * Creates a convolution filter with the specified kernel size
     * 
     * @param kernel
     */
    public Convolution(Kernel kernel) {
        createKernel(kernel);
    }

    /**
     * Creates a new kernel, if a kernel was set before it is discarded and re-created.
     * 
     * @param size Width and height of kernel, 3 for a 3 x 3 kernel
     */
    public void createKernel(Kernel kernel) {
        matrix = new float[kernel.size];
        width = (int) Math.sqrt(kernel.size);
        height = width;
        this.kernel = kernel;
    }

    /**
     * Sets all values in the kernel to 0
     */
    public void clearKernel() {
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = 0;
        }
    }

    /**
     * Copies a number of kernel values from the source values to the kernel.
     * Values are copied sequentially
     * 
     * @param values Values to copy
     * @param sourceIndex Index into source values where first value is read
     * @param destIndex Index into kernel where values are stored
     * @param count Number of values to copy
     * @throws ArrayIndexOutOfBoundsException If there is not enough values in values or the kernel
     */
    public void set(float[] values, int sourceIndex, int destIndex, int count) {
        for (int i = 0; i < count; i++) {
            matrix[i + destIndex] = values[i + sourceIndex];
        }
    }

    /**
     * Calculates the sum of the values in the kernel
     * 
     * @param absolute True to calculate the sum using absolute values
     * @return The sum of the values in this kernel
     */
    public float calculateSum(boolean absolute) {
        return calculateSum(matrix, absolute);
    }

    /**
     * Calculates the sum of the values in the kernel
     * 
     * @param kernel
     * @param absolute True to calculate the sum using absolute values
     * @return Sum of the kernel values
     */
    public static float calculateSum(float[] kernel, boolean absolute) {
        float sum = 0;
        for (float f : kernel) {
            if (absolute) {
                sum += Math.abs(f);
            } else {
                sum += f;
            }
        }
        return sum;
    }

    /**
     * Normalizes the values in the kernel, that is calculates the sum and divides all
     * kernel values by this sum
     * 
     * @param absolute True to use absolute values when calculating the sum
     */
    public void normalize(boolean absolute) {
        normalize(matrix, absolute);
    }

    /**
     * Normalizes the kernel based on the sum of the kernel values.
     * 
     * @param kernel
     * @param absolute True to use absolute values when calculating the sum, ie -1 counts as 1, false otherwise
     */
    public static void normalize(float[] kernel, boolean absolute) {
        normalize(kernel, absolute, 1f);
    }

    /**
     * Normalizes the kernel based on the sum of the kernel values and the factor.
     * Each kernel value will be(kernel / sum) * factor
     * 
     * @param kernel
     * @param absolute
     * @param factor Factor used to multiply the normalized values with, used to darken or lighten.
     * Values above 1 will lighten, values below 1 will darken
     * 
     */
    public static void normalize(float[] kernel, boolean absolute, float factor) {
        float sum = Convolution.calculateSum(kernel, absolute);
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] = (kernel[i] / sum) * factor;
        }
    }

    /**
     * Normalizes the kernel, into a destination kernel, based on the sum of the kernel values and the factor.
     * Each kernel value will be(kernel / sum) * factor
     * 
     * @param kernel The source kernel
     * @param destination The destination for the normalized kernel
     * @param absolute
     * @param factor Factor used to multiply the normalized values with, used to darken or lighten.
     * Values above 1 will lighten, values below 1 will darken
     * 
     */
    public static void normalize(float[] kernel, float[] destination, boolean absolute, float factor) {
        float sum = Convolution.calculateSum(kernel, absolute);
        for (int i = 0; i < kernel.length; i++) {
            destination[i] = (kernel[i] / sum) * factor;
        }
    }

    /**
     * Process the specified image using this filter and return the result as a copy
     * 
     * @param image
     * @param return The processed image.
     */
    public BufferImage process(BufferImage image) {
        BufferImage result = new BufferImage(image.getWidth(), image.getHeight(), image.getFormat());
        process(image, result);
        return result;
    }

    /**
     * Process the source image and store the result in destination.
     * Source and destination format must be the same
     * 
     * @param source
     * @param destination
     */
    public void process(BufferImage source, BufferImage destination) {
        if (source.getFormat() != destination.getFormat()) {
            throw new IllegalArgumentException("Only supports process of same source and destination format");
        }
        byte[] pixels = new byte[source.getSizeInBytes()];
        byte[] destPixels = new byte[destination.getSizeInBytes()];
        source.getBuffer().rewind();
        ((ByteBuffer) source.getBuffer()).get(pixels);

        process(kernel, source, destination, pixels, destPixels);
        destination.getBuffer().rewind();
        ((ByteBuffer) destination.getBuffer()).put(destPixels);
    }

    private void process(Kernel kernel, BufferImage source, BufferImage destination, byte[] pixels, byte[] destPixels) {
        int width = destination.getWidth();
        int height = destination.getHeight();
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        ImageFormat format = destination.getFormat();
        switch (kernel) {
            case SIZE_2X2:
                switch (format) {
                    case RGBA:
                        process2X2RGBA(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    case RGB:
                        process2X2RGB(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported format for 2X2 " + format);
                }
                break;
            case SIZE_3X3:
                switch (format) {
                    case RGBA:
                        process3X3RGBA(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    case RGB:
                        process3X3RGB(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported format for 3X3 " + format);
                }
                break;
            case SIZE_4X4:
                switch (format) {
                    case RGBA:
                        process4X4RGBA(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    case RGB:
                        process4X4RGB(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported format for 4X4 " + format);
                }
                break;
            case SIZE_5X5:
                switch (format) {
                    case RGBA:
                        process5X5RGBA(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    case RGB:
                        process5X5RGB(pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported format for 5X5 " + format);
                }
                break;
            case SIZE_8X8:
            case SIZE_16X16:
                switch (format) {
                    case RGBA:
                        processKernelRGBA(kernel, pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    case RGB:
                        processKernelRGB(kernel, pixels, destPixels, width, height, sourceWidth, sourceHeight);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported format for " + kernel + " kernel " + format);
                }
                break;
        }
    }

    private void processKernelRGBA(Kernel kernel, byte[] pixels, byte[] destPixels, int width, int height,
            int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 4;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        int w = 0;
        int h = 0;
        for (int y = 0; y < height - 0; y++) {
            index = (y * width) * 4;
            for (int x = 0; x < width; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 4));
                w = x * kernel.width < sourceWidth ? kernel.width : (sourceWidth - x * kernel.width);
                h = y * kernel.width < sourceHeight ? kernel.width : (sourceHeight - y * kernel.width);
                for (int column = 0; column < h; column++) {
                    fetchPixelRowRGBA(w, pixels, sourceIndex, column * kernel.width, acc);
                    sourceIndex += widthInBytes;
                }
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
                destPixels[index++] = (byte) (acc[3]);
            }
        }
    }

    private void processKernelRGB(Kernel kernel, byte[] pixels, byte[] destPixels, int width, int height,
            int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 3;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 0; y < height - 0; y++) {
            index = (y * width) * 3;
            for (int x = 0; x < width - 0; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 3));
                for (int column = 0; column < kernel.width; column++) {
                    fetchPixelRowRGB(kernel.width, pixels, sourceIndex, column * kernel.width, acc);
                    sourceIndex += widthInBytes;
                }
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
            }
        }
    }

    private void process2X2RGB(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 3;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 0; y < height; y++) {
            index = (y * width) * 3;
            for (int x = 0; x < width; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 3));
                if (sourceIndex + 6 < pixels.length) {
                    fetchPixelRow2RGB(pixels, sourceIndex, 0, acc);
                }
                if (sourceIndex + widthInBytes + 6 < pixels.length) {
                    fetchPixelRow2RGB(pixels, sourceIndex + widthInBytes, 2, acc);
                }
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
            }
        }
    }

    private void process2X2RGBA(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 4;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 0; y < height - 1; y++) {
            index = (y * width) * 4;
            for (int x = 0; x < width - 1; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 4));
                fetchPixelRow2RGBA(pixels, sourceIndex, 0, acc);
                fetchPixelRow2RGBA(pixels, sourceIndex + widthInBytes, 2, acc);
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
                destPixels[index++] = (byte) (acc[3]);
            }
        }
    }

    private void process3X3RGBA(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 4;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 1; y < height - 1; y++) {
            index = (y * width) * 4;
            for (int x = 1; x < width - 1; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 4));
                fetchPixelRow3RGBA(pixels, sourceIndex - widthInBytes - 4, 0, acc);
                fetchPixelRow3RGBA(pixels, sourceIndex - 4, 3, acc);
                fetchPixelRow3RGBA(pixels, sourceIndex + widthInBytes - 4, 6, acc);
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
                destPixels[index++] = (byte) (acc[3]);
            }
        }

    }

    private void process3X3RGB(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 3;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 1; y < height - 1; y++) {
            index = (y * width) * 3;
            for (int x = 1; x < width - 1; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 3));
                fetchPixelRow3RGB(pixels, sourceIndex - widthInBytes - 3, 0, acc);
                fetchPixelRow3RGB(pixels, sourceIndex - 3, 3, acc);
                fetchPixelRow3RGB(pixels, sourceIndex + widthInBytes - 3, 6, acc);
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
            }
        }
    }

    private void process4X4RGBA(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 4;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 0; y < height; y++) {
            index = (y * width) * 4;
            for (int x = 0; x < width; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 4));
                if (sourceIndex < pixels.length) {
                    fetchPixelRow4RGBA(pixels, sourceIndex, 0, acc);
                }
                if (sourceIndex + widthInBytes < pixels.length) {
                    fetchPixelRow4RGBA(pixels, sourceIndex + widthInBytes, 4, acc);
                }
                if (sourceIndex + widthInBytes * 2 < pixels.length) {
                    fetchPixelRow4RGBA(pixels, sourceIndex + widthInBytes * 2, 8, acc);
                }
                if (sourceIndex + widthInBytes * 3 < pixels.length) {
                    fetchPixelRow4RGBA(pixels, sourceIndex + widthInBytes * 3, 12, acc);
                }
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
                destPixels[index++] = (byte) (acc[3]);
            }
        }
    }

    private void process4X4RGB(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 3;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 0; y < height; y++) {
            index = (y * width) * 3;
            for (int x = 0; x < width; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 3));
                if (sourceIndex < pixels.length) {
                    fetchPixelRow4RGB(pixels, sourceIndex, 0, acc);
                }
                if (sourceIndex < pixels.length) {
                    fetchPixelRow4RGB(pixels, sourceIndex + widthInBytes, 4, acc);
                }
                if (sourceIndex < pixels.length) {
                    fetchPixelRow4RGB(pixels, sourceIndex + widthInBytes * 2, 8, acc);
                }
                if (sourceIndex < pixels.length) {
                    fetchPixelRow4RGB(pixels, sourceIndex + widthInBytes * 3, 12, acc);
                }
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
            }
        }
    }

    private void process5X5RGBA(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 4;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 2; y < height - 2; y++) {
            index = (y * width) * 4;
            for (int x = 2; x < width - 2; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 4));
                fetchPixelRow5RGBA(pixels, sourceIndex - widthInBytes * 2 - 4 * 2, 0, acc);
                fetchPixelRow5RGBA(pixels, sourceIndex - widthInBytes - 4 * 2, 5, acc);
                fetchPixelRow5RGBA(pixels, sourceIndex - 4 * 2, 10, acc);
                fetchPixelRow5RGBA(pixels, sourceIndex + widthInBytes - 4 * 2, 15, acc);
                fetchPixelRow5RGBA(pixels, sourceIndex + widthInBytes * 2 - 4 * 2, 20, acc);
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
                destPixels[index++] = (byte) (acc[3]);
            }
        }
    }

    private void process5X5RGB(byte[] pixels, byte[] destPixels, int width, int height, int sourceWidth,
            int sourceHeight) {
        int index;
        int sourceIndex;
        int widthInBytes = sourceWidth * 3;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        for (int y = 2; y < height - 2; y++) {
            index = (y * width) * 3;
            for (int x = 2; x < width - 2; x++) {
                clearAcc(acc);
                sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * 3));
                fetchPixelRow5RGB(pixels, sourceIndex - widthInBytes * 2 - 3 * 2, 0, acc);
                fetchPixelRow5RGB(pixels, sourceIndex - widthInBytes - 3 * 2, 5, acc);
                fetchPixelRow5RGB(pixels, sourceIndex - 3 * 2, 10, acc);
                fetchPixelRow5RGB(pixels, sourceIndex + widthInBytes - 3 * 2, 15, acc);
                fetchPixelRow5RGB(pixels, sourceIndex + widthInBytes * 2 - 3 * 2, 20, acc);
                destPixels[index++] = (byte) (acc[0]);
                destPixels[index++] = (byte) (acc[1]);
                destPixels[index++] = (byte) (acc[2]);
            }
        }
    }

    /**
     * Clears the rgba values in the acc
     * 
     * @param acc
     */
    private final void clearAcc(float[] acc) {
        acc[0] = 0;
        acc[1] = 0;
        acc[2] = 0;
        acc[3] = 0;
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGBA in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow2RGBA(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGB is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow2RGB(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGBA in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow3RGBA(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGB is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow3RGB(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGBA in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow4RGBA(byte[] pixels, int offset, int index, float[] acc) {
        if (offset >= pixels.length) {
            return;
        }
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGB is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow4RGB(byte[] pixels, int offset, int index, float[] acc) {
        if (offset >= pixels.length) {
            return;
        }
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        if (offset == pixels.length) {
            return;
        }
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGBA in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow5RGBA(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGB is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow5RGB(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow8(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
        acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGBA in acc
     * 
     * @param count Number of pixels to fetch
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRowRGBA(int count, byte[] pixels, int offset, int index, float[] acc) {
        for (int i = 0; i < count; i++) {
            acc[0] += (pixels[offset++] & 0xff) * matrix[index];
            acc[1] += (pixels[offset++] & 0xff) * matrix[index];
            acc[2] += (pixels[offset++] & 0xff) * matrix[index];
            acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
        }
    }

    /**
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param count Number of pixels to fetch
     * @param pixels
     * @param offset Offset into pixels where RGB is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRowRGB(int count, byte[] pixels, int offset, int index, float[] acc) {
        for (int i = 0; i < count; i++) {
            acc[0] += (pixels[offset++] & 0xff) * matrix[index];
            acc[1] += (pixels[offset++] & 0xff) * matrix[index];
            acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        }
    }

}
