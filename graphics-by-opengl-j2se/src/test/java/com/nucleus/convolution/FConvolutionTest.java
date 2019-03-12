package com.nucleus.convolution;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.nucleus.BaseTestCase;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.BufferImage.SourceFormat;
import com.nucleus.texturing.Convolution;
import com.nucleus.texturing.Convolution.Kernel;
import com.nucleus.texturing.J2SEImageFactory;

public class FConvolutionTest extends BaseTestCase implements WindowListener {

    private static volatile boolean wait = false;
    J2SEImageFactory imageFactory = new J2SEImageFactory();
    private final static int ITERATIONS = 10;
    private final static String IMAGE_NAME = "assets/atari.png";

    private static List<Frame> frames = new ArrayList<Frame>();

    private static int x = 0;
    private static int y = 0;

    private class MyFrame extends Frame {

        private BufferedImage image;

        public MyFrame(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(image, getInsets().left, getInsets().top, null);
        }

    }

    @AfterClass
    public static void waitForUser() {
        wait = true;
        while (wait) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        removeFrames(FConvolutionTest.frames);

    }

    private static void removeFrames(List<Frame> frames) {
        for (Frame f : frames) {
            f.setVisible(false);
            f.dispose();
        }
        frames.clear();
    }

    @Test
    public void testProcessOriginal2X2RGBA() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGBA, "Original 2X2 - RGBA", Kernel.SIZE_2X2, new float[] { 0, 1, 1, 0 },
                ITERATIONS);
    }

    @Test
    public void testProcessOriginal2X2RGB() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGB, "Original 2X2 - RGB", Kernel.SIZE_2X2, new float[] { 0, 1, 1, 0 },
                ITERATIONS);
    }

    @Test
    public void testProcessOriginal3X3RGBA() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGBA, "Original 3X3 - RGBA", Kernel.SIZE_3X3,
                new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 }, ITERATIONS);
    }

    @Test
    public void testProcessOriginal3X3RGB() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGB, "Original 3X3 RGB", Kernel.SIZE_3X3,
                new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 }, ITERATIONS);
    }

    @Test
    public void testProcessOriginal5X5() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGBA, "Original 5X5", Kernel.SIZE_5X5, new float[] {
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0 }, ITERATIONS);
    }

    @Test
    public void testProcessBlur5X5() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGBA, "Blur 5X5", Kernel.SIZE_5X5, new float[] {
                1, 2, 3, 2, 1,
                2, 3, 4, 3, 2,
                3, 4, 5, 4, 3,
                2, 3, 4, 3, 2,
                1, 2, 3, 2, 1 }, ITERATIONS);
    }

    @Test
    public void testProcessBlur() throws IOException {
        executeTest(IMAGE_NAME, ImageFormat.RGBA, "Blur 3X3", Kernel.SIZE_3X3,
                new float[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 }, ITERATIONS);
    }

    @Test
    public void testProcessScaleHalf() throws IOException {
        BufferImage sourceRGBA = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        BufferImage destination = new BufferImage(sourceRGBA.getWidth() >>> 1, sourceRGBA.getHeight() >>> 1,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination, "Scale 1/2 2X2 - RGBA", Kernel.SIZE_2X2, new float[] { 1, 1, 1, 1 }, 1);

        BufferImage destination2 = new BufferImage(sourceRGBA.getWidth() >>> 1, sourceRGBA.getHeight() >>> 1,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination2, "Scale 1/2 3X3 - RGBA", Kernel.SIZE_3X3,
                new float[] { 1, 4, 1, 4, 4, 4, 1, 4, 1 }, 1);

        BufferImage sourceRGB = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGB);
        destination = new BufferImage(sourceRGB.getWidth() >>> 1, sourceRGB.getHeight() >>> 1,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination, "Scale 1/2 2X2 - RGB", Kernel.SIZE_2X2, new float[] { 1, 1, 1, 1 }, 1);

        destination2 = new BufferImage(sourceRGB.getWidth() >>> 1, sourceRGB.getHeight() >>> 1,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination2, "Scale 1/2 3X3 - RGB", Kernel.SIZE_3X3,
                new float[] { 1, 4, 1, 4, 4, 4, 1, 4, 1 }, 1);

    }

    /**
     * @Test
     * public void testProcessScaleHalfRGB() throws IOException {
     * BufferImage source = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGB);
     * BufferImage destination = new BufferImage(source.getWidth() >>> 1, source.getHeight() >>> 1,
     * source.getFormat());
     * executeTest(source, destination, "Scale 1/2 RGB 2X2", Kernel.SIZE_2X2, new float[] { 1, 1, 1, 1 }, 1);
     * 
     * BufferImage destination2 = new BufferImage(source.getWidth() >>> 1, source.getHeight() >>> 1,
     * source.getFormat());
     * executeTest(source, destination2, "Scale 1/2 RGB 3X3", Kernel.SIZE_3X3,
     * new float[] { 1, 4, 1, 4, 4, 4, 1, 4, 1 },
     * 1);
     * 
     * }
     **/
    @Test
    public void testProcessScaleQuarter() throws IOException {
        BufferImage sourceRGBA = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        BufferImage destination = new BufferImage(sourceRGBA.getWidth() >>> 2, sourceRGBA.getHeight() >>> 2,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination, "Scale 1/4 4X4 - RGBA", Kernel.SIZE_4X4,
                new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1 },
                1);

        BufferImage destination2 = new BufferImage(sourceRGBA.getWidth() >>> 2, sourceRGBA.getHeight() >>> 2,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination2, "Scale 1/4 5X5 - RGBA", Kernel.SIZE_5X5, new float[] { 1, 2, 3, 2, 1,
                2, 3, 4, 3, 2,
                3, 4, 5, 4, 3,
                2, 3, 4, 3, 2,
                1, 2, 3, 2, 1 },
                1);
        BufferImage sourceRGB = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGB);
        destination = new BufferImage(sourceRGB.getWidth() >>> 2, sourceRGB.getHeight() >>> 2,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination, "Scale 1/4 4X4 - RGB", Kernel.SIZE_4X4,
                new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1 },
                1);

        destination2 = new BufferImage(sourceRGB.getWidth() >>> 2, sourceRGB.getHeight() >>> 2,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination2, "Scale 1/4 5X5 - RGB", Kernel.SIZE_5X5, new float[] { 1, 2, 3, 2, 1,
                2, 3, 4, 3, 2,
                3, 4, 5, 4, 3,
                2, 3, 4, 3, 2,
                1, 2, 3, 2, 1 },
                1);

    }

    @Test
    public void testProcessScaleEight() throws IOException {
        BufferImage sourceRGBA = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        BufferImage destination = new BufferImage(sourceRGBA.getWidth() >>> 3, sourceRGBA.getHeight() >>> 3,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination, "Scale 1/8 8X8 - RGBA", Kernel.SIZE_8X8, null, 1);

        BufferImage destination2 = new BufferImage(sourceRGBA.getWidth() >>> 3, sourceRGBA.getHeight() >>> 3,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination2, "Scale 1/8 8X8 center - RGBA", Kernel.SIZE_8X8, new float[] {
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        }, 1);

        BufferImage sourceRGB = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGB);
        destination = new BufferImage(sourceRGB.getWidth() >>> 3, sourceRGB.getHeight() >>> 3,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination, "Scale 1/8 8X8 - RGB", Kernel.SIZE_8X8, null, 1);

        destination2 = new BufferImage(sourceRGB.getWidth() >>> 3, sourceRGB.getHeight() >>> 3,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination2, "Scale 1/8 8X8 center - RGB", Kernel.SIZE_8X8, new float[] {
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        }, 1);

    }

    @Test
    public void testProcessScaleSixteen() throws IOException {
        BufferImage sourceRGBA = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        BufferImage destination = new BufferImage(sourceRGBA.getWidth() >>> 4, sourceRGBA.getHeight() >>> 4,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination, "Scale 1/16 16X16 - RGBA", Kernel.SIZE_16X16, null, 1);

        BufferImage destination2 = new BufferImage(sourceRGBA.getWidth() >>> 4, sourceRGBA.getHeight() >>> 4,
                sourceRGBA.getFormat());
        executeTest(sourceRGBA, destination2, "Scale 1/16 16X16 center - RGBA", Kernel.SIZE_16X16, new float[] {
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        }, 1);

        BufferImage sourceRGB = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGB);
        destination = new BufferImage(sourceRGB.getWidth() >>> 4, sourceRGB.getHeight() >>> 4,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination, "Scale 1/8 8X8 - RGB", Kernel.SIZE_16X16, null, 1);

        destination2 = new BufferImage(sourceRGB.getWidth() >>> 4, sourceRGB.getHeight() >>> 4,
                sourceRGB.getFormat());
        executeTest(sourceRGB, destination2, "Scale 1/8 8X8 center - RGB", Kernel.SIZE_16X16, new float[] {
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 2, 3, 4, 4, 3, 2, 1,
                1, 1, 2, 3, 3, 2, 1, 1,
                1, 1, 1, 2, 2, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        }, 1);

    }

    /**
     * Executes a process test for the specified image name, the image will be loaded from filesystem
     * and the destination will be a copy with the same format, width and height.
     * 
     * @param imageName
     * @param title
     * @param kernel
     * @param data
     * @param loop
     * @throws IOException
     */
    private void executeTest(String imageName, ImageFormat format, String title, Kernel kernel, float[] data, int loop)
            throws IOException {
        BufferImage source = imageFactory.createImage(imageName, format);
        BufferImage destination = new BufferImage(source.getWidth(), source.getHeight(), source.getFormat());
        executeTest(source, destination, title, kernel, data, loop);
    }

    private void executeTest(BufferImage source, BufferImage destination, String title, Kernel kernel, float[] data,
            int loop) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            processImage(kernel, data, source, destination);
        }
        long end = System.currentTimeMillis();
        Assert.assertNotNull(destination);
        SourceFormat sf = SourceFormat.get(destination.getFormat());
        BufferedImage image = toBufferedImage(destination, sf.type);
        String fillrateStr = "";
        if (loop > 1) {
            int size = destination.getWidth() * destination.getHeight();
            int fillrate = (size * loop) / (int) (end - start);
            fillrateStr = " " + Float.toString(fillrate / 1000) + ", mpixels/s";

        }
        display(image, title + fillrateStr);

    }

    private void processImage(Convolution.Kernel kernel, float[] data, BufferImage image, BufferImage destination) {
        if (data == null) {
            data = kernel.createDefaultKernel(1.0f);
        }
        Convolution filter = new Convolution(kernel);
        filter.set(data, 0, 0, data.length);
        filter.normalize(false);
        filter.process(image, destination);
    }

    private BufferedImage toBufferedImage(BufferImage image, int format) {
        byte[] pixels = new byte[image.getSizeInBytes()];
        image.getBuffer().rewind();
        ((ByteBuffer) image.getBuffer()).get(pixels);

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
                format);
        result.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), pixels);
        return result;
    }

    private void display(BufferedImage image, String title) {
        MyFrame myFrame = new MyFrame(image);
        myFrame.setTitle(title);
        myFrame.addWindowListener(this);
        myFrame.setVisible(true);
        Insets i = myFrame.getInsets();
        myFrame.setSize(image.getWidth() + i.left + i.right, image.getHeight() + i.top + i.bottom);
        myFrame.setLocation(x, y);
        x += myFrame.getWidth();
        if ((x + myFrame.getWidth()) > Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
            x = 0;
            y += myFrame.getHeight();
        }
        frames.add(myFrame);

    }

    @Override
    public void windowActivated(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        wait = false;
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowIconified(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowOpened(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }
}
