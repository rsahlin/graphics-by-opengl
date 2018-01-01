package com.nucleus.lwjgl3;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper.Renderers;

/**
 * Entry point for an application using lwjgl3 library
 */
public class LWJGL3Application extends J2SEWindowApplication {

    /**
     * To select GLFW or JAWT window
     */
    private static final String WINDOW_TYPE_KEY = "WINDOWTYPE";

    private boolean running = false;

    public enum WindowType {
        GLFW(),
        JAWT();
    }

    protected J2SEWindow window;
    protected WindowType windowType;

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * 
     * @param args
     * @param version
     * @param clientClass Must implement {@link ClientApplication}
     * @throws IllegalArgumentException If clientClass is null
     */
    public LWJGL3Application(String[] args, Renderers version, Class<?> clientClass) {
        super(args, version, clientClass);
        switch (windowType) {
            case GLFW:
                createCoreApp(windowWidth, windowHeight);
                ((GLFWWindow) window).swapBuffers();
                coreApp.contextCreated(windowWidth, windowHeight);
                break;
            default:
                // Do nothing - create context based on callback
        }
    }

    @Override
    protected void setProperties(String[] args) {
        this.windowType = WindowType.GLFW;
        super.setProperties(args);
    }

    @Override
    protected void setProperty(String str) {
        super.setProperty(str);
        if (str.toUpperCase().startsWith(WINDOW_TYPE_KEY)) {
            windowType = WindowType.valueOf(str.substring(WINDOW_TYPE_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_TYPE_KEY + " set to " + windowType);
        }
    }

    @Override
    protected J2SEWindow createWindow(Renderers version) {
        switch (windowType) {
            case GLFW:
                window = new GLFWWindow(this, windowWidth, windowHeight);
                break;
            case JAWT:
                window = new JAWTWindow(this, windowWidth, windowHeight);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + windowType);
        }
        return window;
    }

    @Override
    public void resize(int x, int y, int width, int height) {
        if (coreApp != null) {
            coreApp.getRenderer().resizeWindow(x, y, width, height);
        }
    }

    @Override
    public void windowClosed() {
    }

    /**
     * Call this method after instantiation to drive rendering if it is not driven by paint() method from window.
     * Will automatically exit if window type is one that drives rendering via paint()
     */

    public void run() {
        switch (windowType) {
            case GLFW:
                running = true;
                while (running) {
                    window.drawFrame();
                }
                break;
            default:
        }
    }

}
