package com.nucleus;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.nucleus.Backend.BackendFactory;
import com.nucleus.common.Platform;
import com.nucleus.common.Platform.OS;
import com.nucleus.mmi.Key;
import com.nucleus.mmi.Pointer;
import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;
import com.nucleus.mmi.core.CoreInput;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.renderer.Window;

/**
 * Window that connects to the underlying GL.
 * Use this on platforms where a window needs to be created.
 * This shall take care of informing {@link RenderContextListener} when context is created.
 *
 */
public abstract class J2SEWindow implements WindowListener {

    protected CoreApp coreApp;

    protected BackendFactory factory;
    protected Renderers version;
    protected Backend backend;
    protected CoreApp.CoreAppStarter coreAppStarter;
    protected int width;
    protected int height;
    protected WindowListener windowListener;
    protected SurfaceConfiguration config;
    protected boolean fullscreen = false;

    public J2SEWindow(Renderers version, BackendFactory factory, CoreApp.CoreAppStarter coreAppStarter, int width,
            int height, SurfaceConfiguration config) {
        if (coreAppStarter == null) {
            throw new IllegalArgumentException("Appstarter is null");
        }
        this.coreAppStarter = coreAppStarter;
        this.width = width;
        this.height = height;
        this.config = config;
        this.factory = factory;
        this.version = version;
        OS os = Platform.getInstance().getOS();
        if (os != OS.android) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            Window.getInstance().setScreenSize(d.width, d.height);
        } else {
            Window.getInstance().setScreenSize(width, height);
        }
    }

    /**
     * Creates the underlying window system and render backend
     * Subclasses must implement this method to setup the needed window system and render API.
     * Implementations may defer creation until window framework is up and running (via async callbacks)
     * 
     */
    public abstract void init();

    /**
     * Sets callback for {@link WindowListener} events
     * 
     * @param windowListener
     */
    public void setWindowListener(WindowListener windowListener) {
        this.windowListener = windowListener;
    }

    /**
     * Returns the Backend core wrapper, this must be created in subclasses
     * 
     * @return The Backend wrapper or null if not created
     * This may mean that the window has not been made visible.
     */
    public Backend getBackend() {
        return backend;
    }

    /**
     * Sets the CoreApp in this window.
     * This is used to drive rendering by calling {@link CoreApp#drawFrame()}
     * It will also be used to get callback when GL context has been created
     * 
     * @param coreApp
     */
    public void setCoreApp(CoreApp coreApp) {
        this.coreApp = coreApp;
    }

    /**
     * Call this to create the core app, call this before calling {@link #internalContextCreated(int, int)}
     * This will display the splash screen - if needed make sure to swap buffers after calling this method to show the
     * splash.
     * 
     * @param width
     * @param height
     */
    public void internalCreateCoreApp(int width, int height) {
        coreAppStarter.createCoreApp(width, height);
    }

    /**
     * Call this when GL context is created to create core app and prepare it, shall be called after
     * {@link #internalCreateCoreApp(int, int)}
     * This will load the scene and allocate objects, after this method returns the CoreApp shall be driven by calling
     * {@link CoreApp#drawFrame()}
     * 
     * @param width
     * @param height
     */
    public void internalContextCreated(int width, int height) {
        coreApp.contextCreated(width, height);
    }

    /**
     * Used to drive rendering for window types that does not provide paint callbacks.
     * Call this method until app should stop
     * Override in subclasses
     */
    public void drawFrame() {

    }

    /**
     * Handles a pointer action, this will pass on the pointer event to app.
     * 
     * @param action
     * @param type
     * @param xpos
     * @param ypos
     * @param pointer The pointer index, 0 and upwards
     * @param timestamp
     */
    protected void handleMouseEvent(PointerAction action, Type type, int xpos, int ypos, int pointer, long timestamp) {
        switch (action) {
            case DOWN:
                CoreInput.getInstance().pointerEvent(PointerAction.DOWN, type,
                        timestamp, pointer,
                        new float[] { xpos, ypos }, Pointer.DOWN_PRESSURE);
                break;
            case UP:
                CoreInput.getInstance().pointerEvent(PointerAction.UP, type, timestamp, pointer, new float[] {
                        xpos, ypos }, Pointer.DOWN_PRESSURE);
                break;
            case MOVE:
                CoreInput.getInstance().pointerEvent(PointerAction.MOVE, type, timestamp, pointer, new float[] {
                        xpos, ypos }, Pointer.DOWN_PRESSURE);
            default:
        }
    }

    /**
     * Passes the keyevent on to the {@link CoreApp} which will send to registered listeners
     * 
     * @param event
     */
    protected void handleKeyEvent(Key event) {
        CoreInput.getInstance().onKeyEvent(event);
    }

    @Override
    public void resize(int x, int y, int width, int height) {
        if (windowListener != null) {
            windowListener.resize(x, y, width, height);
        }
    }

    @Override
    public void windowClosed() {
        if (windowListener != null) {
            windowListener.windowClosed();
        } else {
            SimpleLogger.d(getClass(), "windowClosed(); - windowListener is null");
        }

    }

    protected void mouseWheelMoved(float rotation, long when) {
        float zoom = rotation * Pointer.ZOOM_FACTOR;
        CoreInput.getInstance().pointerEvent(PointerAction.ZOOM, Pointer.Type.MOUSE, when,
                Pointer.POINTER_1, new float[] {
                        zoom, zoom },
                0);
    }

    protected void exit() {
        SimpleLogger.d(getClass(), "exit");
        if (fullscreen) {
            fullscreen = false;
            setFullscreenMode(false);
        } else {
            if (coreApp.onBackPressed()) {
                coreApp.setDestroyFlag();
                destroy();
                System.exit(0);
            }
        }
    }

    /**
     * Shows or hides this window
     * 
     * @param visible
     */
    public abstract void setVisible(boolean visible);

    /**
     * Sets the title of the window
     * 
     * @param title
     */
    public abstract void setWindowTitle(String title);

    /**
     * Switch to and from fullscreen mode.
     * 
     * @param fullscreen
     */
    protected abstract void setFullscreenMode(boolean fullscreen);

    /**
     * Destroy the window(s) and release window resources
     */
    protected abstract void destroy();

}
