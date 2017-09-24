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

import com.nucleus.texturing.Convolution;
import com.nucleus.texturing.Convolution.Kernel;
import com.nucleus.texturing.Image;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.J2SEImageFactory;

public class FConvolutionTest implements WindowListener {

    private static volatile boolean wait = false;
    J2SEImageFactory imageFactory = new J2SEImageFactory();
    private final static int ITERATIONS = 50;
    private final static String IMAGE_NAME = "assets/af.png";

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
    public void testProcessOriginal() throws IOException {
        executeTest(IMAGE_NAME, "Original 3X3", Kernel.SIZE_3X3, new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 }, ITERATIONS);
    }

    @Test
    public void testProcessOriginal5X5() throws IOException {
        executeTest(IMAGE_NAME, "Original 5X5", Kernel.SIZE_5X5, new float[] {
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0 }, ITERATIONS);
    }

    @Test
    public void testProcessBlur5X5() throws IOException {
        executeTest(IMAGE_NAME, "Original 5X5", Kernel.SIZE_5X5, new float[] {
                1, 2, 3, 2, 1,
                2, 3, 4, 3, 2,
                3, 4, 5, 4, 3,
                2, 3, 4, 3, 2,
                1, 2, 3, 2, 1 }, ITERATIONS);
    }

    @Test
    public void testProcessBlur() throws IOException {
        executeTest(IMAGE_NAME, "Blur 3X3", Kernel.SIZE_3X3, new float[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 }, ITERATIONS);
    }

    @Test
    public void testProcessScaleHalf() throws IOException {
        Image source = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        Image destination = new Image(source.getWidth() >>> 1, source.getHeight() >>> 1, source.getFormat());
        executeTest(source, destination, "Scale 1/2 2X2", Kernel.SIZE_2X2, new float[] { 1, 1, 1, 1 }, 1);

        Image destination2 = new Image(source.getWidth() >>> 1, source.getHeight() >>> 1, source.getFormat());
        executeTest(source, destination2, "Scale 1/2 3X3", Kernel.SIZE_3X3, new float[] { 1, 4, 1, 4, 4, 4, 1, 4, 1 },
                1);

    }

    @Test
    public void testProcessScaleQuarter() throws IOException {
        Image source = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        Image destination = new Image(source.getWidth() >>> 2, source.getHeight() >>> 2, source.getFormat());
        executeTest(source, destination, "Scale 1/4 4X4", Kernel.SIZE_4X4, new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1 }, 1);

        Image destination2 = new Image(source.getWidth() >>> 2, source.getHeight() >>> 2, source.getFormat());
        executeTest(source, destination2, "Scale 1/4 5X5", Kernel.SIZE_5X5, new float[] { 1, 2, 3, 2, 1,
                2, 3, 4, 3, 2,
                3, 4, 5, 4, 3,
                2, 3, 4, 3, 2,
                1, 2, 3, 2, 1 },
                1);

    }

    @Test
    public void testProcessScaleEight() throws IOException {
        Image source = imageFactory.createImage(IMAGE_NAME, ImageFormat.RGBA);
        Image destination = new Image(source.getWidth() >>> 3, source.getHeight() >>> 3, source.getFormat());
        executeTest(source, destination, "Scale 1/8 8X8", Kernel.SIZE_8X8, new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, 1);

        Image destination2 = new Image(source.getWidth() >>> 3, source.getHeight() >>> 3, source.getFormat());
        executeTest(source, destination2, "Scale 1/8 8X8 center", Kernel.SIZE_8X8, new float[] {
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
    private void executeTest(String imageName, String title, Kernel kernel, float[] data, int loop) throws IOException {
        Image source = imageFactory.createImage(imageName, ImageFormat.RGBA);
        Image destination = new Image(source.getWidth(), source.getHeight(), source.getFormat());
        executeTest(source, destination, title, kernel, data, loop);
    }

    private void executeTest(Image source, Image destination, String title, Kernel kernel, float[] data, int loop) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            processImage(kernel, data, source, destination);
        }
        long end = System.currentTimeMillis();
        Assert.assertNotNull(destination);
        BufferedImage image = toBufferedImage(destination, BufferedImage.TYPE_4BYTE_ABGR);
        String fillrateStr = "";
        if (loop > 1) {
            int size = destination.getWidth() * destination.getHeight();
            int fillrate = (size * loop) / (int) (end - start);
            fillrateStr = " " + Float.toString(fillrate / 1000) + ", mpixels/s";

        }
        display(image, title + fillrateStr);

    }

    private void processImage(Convolution.Kernel kernel, float[] data, Image image, Image destination) {
        Convolution filter = new Convolution(kernel);
        filter.set(data, 0, 0, data.length);
        filter.normalize(false);
        filter.process(image, destination);
    }

    private BufferedImage toBufferedImage(Image image, int format) {
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
        System.out.println("done");

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
