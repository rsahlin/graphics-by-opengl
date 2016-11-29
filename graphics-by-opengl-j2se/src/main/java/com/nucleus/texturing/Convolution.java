package com.nucleus.texturing;

import java.nio.ByteBuffer;

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
    public Image process(Image image) {
        Image result = new Image(image.getWidth(), image.getHeight(), image.getFormat());
        process(image, result);
        return result;
    }

    /**
     * Process the source image and store the result in destination.
     * 
     * @param source
     * @param destination
     */
    public void process(Image source, Image destination) {
        byte[] pixels = new byte[source.getSizeInBytes()];
        byte[] destPixels = new byte[destination.getSizeInBytes()];
        source.getBuffer().rewind();
        ((ByteBuffer) source.getBuffer()).get(pixels);

        process(kernel, source, destination, pixels, destPixels);
        destination.getBuffer().rewind();
        ((ByteBuffer) destination.getBuffer()).put(destPixels);
    }

    private void process(Kernel kernel, Image source, Image destination, byte[] pixels, byte[] destPixels) {
        int width = destination.getWidth();
        int height = destination.getHeight();
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int sizeInBytes = destination.getFormat().size;
        float yScale = sourceHeight / height;
        float xScale = sourceWidth / width;
        float acc[] = new float[4];
        int widthInBytes = sourceWidth * sizeInBytes;
        int index;
        int sourceIndex;
        switch (kernel) {
        case SIZE_2X2:
            for (int y = 0; y < height; y++) {
                index = (y * width) * sizeInBytes;
                for (int x = 0; x < width; x++) {
                    clearAcc(acc);
                    sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * sizeInBytes));
                    fetchPixelRow2(pixels, sourceIndex, 0, acc);
                    fetchPixelRow2(pixels, sourceIndex + widthInBytes, 2, acc);
                    destPixels[index++] = (byte) (acc[0]);
                    destPixels[index++] = (byte) (acc[1]);
                    destPixels[index++] = (byte) (acc[2]);
                    destPixels[index++] = (byte) (acc[3]);
                }
            }
            break;
        case SIZE_3X3:
            for (int y = 1; y < height - 1; y++) {
                index = (y * width) * sizeInBytes;
                for (int x = 1; x < width - 1; x++) {
                    clearAcc(acc);
                    sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * sizeInBytes));
                    fetchPixelRow3(pixels, sourceIndex - widthInBytes - sizeInBytes, 0, acc);
                    fetchPixelRow3(pixels, sourceIndex - sizeInBytes, 3, acc);
                    fetchPixelRow3(pixels, sourceIndex + widthInBytes - sizeInBytes, 6, acc);
                    if (acc[0] < 0) {
                        acc[0] = 0;
                    }
                    if (acc[1] < 0) {
                        acc[1] = 0;
                    }
                    if (acc[2] < 0) {
                        acc[2] = 0;
                    }
                    if (acc[3] < 0) {
                        acc[3] = 0;
                    }
                    if (acc[0] > 255) {
                        acc[0] = 255;
                    }
                    if (acc[1] > 255) {
                        acc[1] = 255;
                    }
                    if (acc[2] > 255) {
                        acc[2] = 255;
                    }
                    if (acc[3] > 255) {
                        acc[3] = 255;
                    }
                    destPixels[index++] = (byte) (acc[0]);
                    destPixels[index++] = (byte) (acc[1]);
                    destPixels[index++] = (byte) (acc[2]);
                    destPixels[index++] = (byte) (acc[3]);
                }
            }
            break;
        case SIZE_4X4:
            for (int y = 0; y < height; y++) {
                index = (y * width) * sizeInBytes;
                for (int x = 0; x < width; x++) {
                    clearAcc(acc);
                    sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * sizeInBytes));
                    if (sourceIndex < pixels.length) {
                        fetchPixelRow4(pixels, sourceIndex, 0, acc);
                    }
                    if (sourceIndex < pixels.length) {
                        fetchPixelRow4(pixels, sourceIndex + widthInBytes, 4, acc);
                    }
                    if (sourceIndex < pixels.length) {
                        fetchPixelRow4(pixels, sourceIndex + widthInBytes * 2, 8, acc);
                    }
                    if (sourceIndex < pixels.length) {
                        fetchPixelRow4(pixels, sourceIndex + widthInBytes * 3, 12, acc);
                    }
                    destPixels[index++] = (byte) (acc[0]);
                    destPixels[index++] = (byte) (acc[1]);
                    destPixels[index++] = (byte) (acc[2]);
                    destPixels[index++] = (byte) (acc[3]);
                }
            }
            break;
        case SIZE_5X5:
            for (int y = 2; y < height - 2; y++) {
                index = (y * width) * sizeInBytes;
                for (int x = 2; x < width - 2; x++) {
                    clearAcc(acc);
                    sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * sizeInBytes));
                    fetchPixelRow5(pixels, sourceIndex - widthInBytes * 2 - sizeInBytes * 2, 0, acc);
                    fetchPixelRow5(pixels, sourceIndex - widthInBytes - sizeInBytes * 2, 5, acc);
                    fetchPixelRow5(pixels, sourceIndex - sizeInBytes * 2, 10, acc);
                    fetchPixelRow5(pixels, sourceIndex + widthInBytes - sizeInBytes * 2, 15, acc);
                    fetchPixelRow5(pixels, sourceIndex + widthInBytes * 2 - sizeInBytes * 2, 20, acc);
                    destPixels[index++] = (byte) (acc[0]);
                    destPixels[index++] = (byte) (acc[1]);
                    destPixels[index++] = (byte) (acc[2]);
                    destPixels[index++] = (byte) 255;
                }
            }
            break;
        case SIZE_8X8:
        case SIZE_16X16:
            for (int y = 0; y < height - 0; y++) {
                index = (y * width) * sizeInBytes;
                for (int x = 0; x < width - 0; x++) {
                    clearAcc(acc);
                    sourceIndex = (int) ((((y * yScale) * sourceWidth + (x * xScale)) * sizeInBytes));
                    for (int column = 0; column < kernel.width; column++) {
                        fetchPixelRow(kernel.width, pixels, sourceIndex, column * kernel.width, acc);
                        sourceIndex += widthInBytes;
                    }
                    destPixels[index++] = (byte) (acc[0]);
                    destPixels[index++] = (byte) (acc[1]);
                    destPixels[index++] = (byte) (acc[2]);
                    destPixels[index++] = (byte) (acc[3]);
                }
            }
            break;
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
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow2(byte[] pixels, int offset, int index, float[] acc) {
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
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow3(byte[] pixels, int offset, int index, float[] acc) {
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
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow4(byte[] pixels, int offset, int index, float[] acc) {
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
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow5(byte[] pixels, int offset, int index, float[] acc) {
        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        offset++; // skip alpha

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        offset++; // skip alpha

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        offset++; // skip alpha

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index++];
        offset++; // skip alpha

        acc[0] += (pixels[offset++] & 0xff) * matrix[index];
        acc[1] += (pixels[offset++] & 0xff) * matrix[index];
        acc[2] += (pixels[offset++] & 0xff) * matrix[index];
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
     * Fetches a pixel row at index from pixels, and stores as RGB in acc
     * 
     * @param count Number of pixels to fetch
     * @param pixels
     * @param offset Offset into pixels where RGBA is read
     * @param index Index into matrix
     * @param acc
     */
    private final void fetchPixelRow(int count, byte[] pixels, int offset, int index, float[] acc) {
        for (int i = 0; i < count; i++) {
            acc[0] += (pixels[offset++] & 0xff) * matrix[index];
            acc[1] += (pixels[offset++] & 0xff) * matrix[index];
            acc[2] += (pixels[offset++] & 0xff) * matrix[index];
            acc[3] += (pixels[offset++] & 0xff) * matrix[index++];
        }

    }

}
