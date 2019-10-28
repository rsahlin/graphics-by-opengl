package com.nucleus;

/**
 * Callback for window resize
 * 
 * @author Richard Sahlin
 *
 */
public interface WindowListener {

    /**
     * A resize of the window, this is called when the display window has changed size.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void resize(int x, int y, int width, int height);

    /**
     * The window has been closed - this shall be called when user has invoked an action to close the window.
     * App / renderer and window must be destroyed and freed.
     */
    public void windowClosed();

}
