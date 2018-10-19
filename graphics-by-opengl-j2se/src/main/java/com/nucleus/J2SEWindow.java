package com.nucleus;

import com.nucleus.mmi.KeyEvent;
import com.nucleus.mmi.PointerData;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.mmi.core.InputProcessor;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
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

    protected GLES20Wrapper wrapper;
    protected CoreApp.CoreAppStarter coreAppStarter;
    protected int width;
    protected int height;
    protected WindowListener windowListener;
    protected SurfaceConfiguration config;
    protected boolean fullscreen = false;

    public J2SEWindow(CoreApp.CoreAppStarter coreAppStarter, int width, int height, SurfaceConfiguration config) {
        if (coreAppStarter == null) {
            throw new IllegalArgumentException("Appstarter is null");
        }
        this.coreAppStarter = coreAppStarter;
        this.width = width;
        this.height = height;
        this.config = config;
        Window.getInstance().setScreenSize(width, height);

    }

    /**
     * Sets callback for {@link WindowListener} events
     * 
     * @param windowListener
     */
    public void setWindowListener(WindowListener windowListener) {
        this.windowListener = windowListener;
    }

    /**
     * Returns the {@link GLESWrapper} this must be created in subclasses
     * 
     * @return The GLES wrapper or null if not created
     * This may mean that the window has not been made visible.
     */
    public GLESWrapper getGLESWrapper() {
        return wrapper;
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
                InputProcessor.getInstance().pointerEvent(PointerAction.DOWN, type,
                        timestamp, pointer,
                        new float[] { xpos, ypos }, PointerData.DOWN_PRESSURE);
                break;
            case UP:
                InputProcessor.getInstance().pointerEvent(PointerAction.UP, type, timestamp, pointer, new float[] {
                        xpos, ypos }, PointerData.DOWN_PRESSURE);
                break;
            case MOVE:
                InputProcessor.getInstance().pointerEvent(PointerAction.MOVE, type, timestamp, pointer, new float[] {
                        xpos, ypos }, PointerData.DOWN_PRESSURE);
            default:
        }
    }

    /**
     * Passes the keyevent on to the {@link CoreApp} which will send to registered listeners
     * 
     * @param event
     */
    protected void handleKeyEvent(KeyEvent event) {
        InputProcessor.getInstance().onKeyEvent(event);
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
        float zoom = rotation * PointerData.ZOOM_FACTOR;
        InputProcessor.getInstance().pointerEvent(PointerAction.ZOOM, PointerData.Type.MOUSE, when,
                PointerData.POINTER_1, new float[] {
                        zoom, zoom },
                0);
    }

    protected void backPressed() {
        SimpleLogger.d(getClass(), "backPressed()");
        if (fullscreen) {
            fullscreen = false;
            setFullscreenMode(false);
        } else {
            if (coreApp.onBackPressed()) {
                coreApp.setDestroyFlag();
                setVisible(false);
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
