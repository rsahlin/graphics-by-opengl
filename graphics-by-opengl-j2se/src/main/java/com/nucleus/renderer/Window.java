package com.nucleus.renderer;

import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.resource.ResourceBias.RESOLUTION;

/**
 * The size of the renderable area, this is a singleton class since only one instance of GL is supported.
 * Alse keeps a reference to create J2SEWindow if created on the platform.
 * 
 * @author Richard Sahlin
 *
 */
public class Window {

    private static Window window = null;

    int width;
    int height;
    J2SEWindow platformWindow;

    /**
     * The display resolution, ie the full resolution of the display
     */
    int[] screenSize;
    /**
     * Best fit resolution for the display - use this to figure out image scale bias
     */
    RESOLUTION resolution;

    /**
     * Hide instantiation from clients.
     */
    private Window() {
    }

    /**
     * Returns the Window instance, this will always be the same.
     * 
     * @return Window instance (singleton)
     */
    public static Window getInstance() {
        if (window == null) {
            window = new Window();
        }
        return window;
    }

    /**
     * Sets the size of the window area, ie the renderable area. This is an internal method.
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        SimpleLogger.d(getClass(), "setSize() " + width + ", " + height);
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the size of the screen, ie total number of pixels on display - may be bigger than window size.
     * Do not call this method directly, it is initialized by implementations (JOGL, Android, LWJGL)
     * 
     * @param width
     * @param height
     */
    public void setScreenSize(int width, int height) {
        SimpleLogger.d(getClass(), "setScreenSize() " + width + ", " + height);
        this.screenSize = new int[] { width, height };
        resolution = RESOLUTION.getResolution(height);
    }

    /**
     * Sets the window created on the platform, not valid for Android
     * 
     * @param platformWindow
     */
    public void setPlatformWindow(J2SEWindow platformWindow) {
        this.platformWindow = platformWindow;
    }

    /**
     * Sets the title of the platform window
     * 
     * @param title
     */
    public void setTitle(String title) {
        if (platformWindow != null) {
            platformWindow.setWindowTitle(title);
        }
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

    /**
     * Returns the total display size
     * 
     * @return
     */
    public int[] getScreenSize() {
        return screenSize;
    }

    /**
     * Returns the resolution of display, use this for image scale bias
     * 
     * @return
     */
    public RESOLUTION getResolution() {
        return resolution;
    }

}
