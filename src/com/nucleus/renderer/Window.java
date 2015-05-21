package com.nucleus.renderer;

/**
 * The dimensions of the renderable area, this is a singleton class since only one instance of GL is supported.
 * 
 * @author Richard Sahlin
 *
 */
public class Window {

    protected static Window window = null;

    int width;
    int height;

    /**
     * Hide instantiation from clients.
     */
    private Window() {
    }

    public static Window getInstance() {
        if (window == null) {
            window = new Window();
        }
        return window;
    }

    /**
     * Sets the dimension of the window area, ie the renderable area. This is an internal method.
     * 
     * @param width
     * @param height
     */
    protected void setDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width of the visible window
     * 
     * @return Width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the visible window
     * 
     * @return Height in pixels
     */
    public int getHeight() {
        return height;
    }

}
