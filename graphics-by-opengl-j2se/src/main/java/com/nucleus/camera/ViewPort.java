package com.nucleus.camera;

/**
 * The viewport dimensions
 * 
 * @author Richard Sahlin
 *
 */
public class ViewPort {

    public final static int VIEWPORT_X = 0;
    public final static int VIEWPORT_Y = 1;
    public final static int VIEWPORT_WIDTH = 2;
    public final static int VIEWPORT_HEIGHT = 3;

    /**
     * Number of values for the viewport.
     */
    public final static int VIEWPORT_SIZE = 4;
    /**
     * The viewport setting, same as glViewPort, units are in pixels. This is the transform from normalized device
     * coordinates to window/screen coordinates. Normally set to 0,0,width,height
     * x,y,width,height
     */
    private int[] viewPort = new int[VIEWPORT_SIZE];

    /**
     * Sets the dimension, in pixels, for the screen viewport.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setViewPort(int x, int y, int width, int height) {
        viewPort[VIEWPORT_X] = x;
        viewPort[VIEWPORT_Y] = y;
        viewPort[VIEWPORT_WIDTH] = width;
        viewPort[VIEWPORT_HEIGHT] = height;
    }

    /**
     * Fetches the viewport, this is a reference to the array holding the values. Do not modify these values,
     * use setViewPort to change.
     * 
     * @return
     */
    public int[] getViewPort() {
        return viewPort;
    }
}
