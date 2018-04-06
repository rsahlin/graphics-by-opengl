package com.nucleus.lwjgl3;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication;
import com.nucleus.common.Type;
import com.nucleus.opengl.GLESWrapper.Renderers;

/**
 * Entry point for an application using lwjgl3 library
 */
public class LWJGL3Application extends J2SEWindowApplication {

    protected static final WindowType DEFAULT_WINDOW_TYPE = WindowType.JAWT;

    private boolean running = false;

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * 
     * @param args
     * @param version
     * @param clientClass Implementing class for {@link ClientApplication}, must implement {@link ClientApplication}
     * interface
     * @throws IllegalArgumentException If clientClass is null
     */
    public LWJGL3Application(String[] args, Renderers version, Type<Object> clientClass) {
        super(args, version, clientClass);
        switch (windowType) {
            case GLFW:
                createCoreApp(windowWidth, windowHeight);
                ((GLFWWindow) j2seWindow).swapBuffers();
                coreApp.contextCreated(windowWidth, windowHeight);
                break;
            default:
                // Do nothing - create context based on callback
        }
    }

    @Override
    protected void setProperties(String[] args) {
        this.windowType = DEFAULT_WINDOW_TYPE;
        super.setProperties(args);
    }

    @Override
    protected J2SEWindow createWindow(Renderers version) {
        switch (windowType) {
            case GLFW:
                j2seWindow = new GLFWWindow(version, this, windowWidth, windowHeight);
                break;
            case JAWT:
                j2seWindow = new JAWTWindow(version, this, windowWidth, windowHeight);
                break;
            case EGL:
                j2seWindow = new LWJGLEGLWindow(version, this, windowWidth, windowHeight);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + windowType);
        }
        return j2seWindow;
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
                    j2seWindow.drawFrame();
                }
                break;
            default:
        }
    }

}
