package com.nucleus;

import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;

/**
 * Window that connects to the underlying GL.
 * Use this on platforms where a window needs to be created.
 * This shall take care of informing {@link RenderContextListener} when context is created.
 *
 */
public abstract class J2SEWindow {

    protected CoreApp coreApp;

    protected GLES20Wrapper wrapper;
    protected CoreApp.CoreAppStarter coreAppStarter;
    protected int width;
    protected int height;

    public J2SEWindow(CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        if (coreAppStarter == null) {
            throw new IllegalArgumentException("Appstarter is null");
        }
        this.coreAppStarter = coreAppStarter;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the {@link GLESWrapper}
     * 
     * @return The GLES wrapper or null if not created
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
     * @param xpos
     * @param ypos
     * @param pointer The pointer index, 0 and upwards
     * @param timestamp
     * @param action
     */
    protected void handleMouseEvent(int xpos, int ypos, int pointer, long timestamp, PointerAction action) {
        switch (action) {
        case DOWN:
            coreApp.getInputProcessor().pointerEvent(PointerAction.DOWN,
                    timestamp, pointer,
                    new float[] { xpos, ypos });
            break;
        case UP:
            coreApp.getInputProcessor().pointerEvent(PointerAction.UP, timestamp, pointer, new float[] {
                    xpos, ypos });
            break;
        case MOVE:
            coreApp.getInputProcessor().pointerEvent(PointerAction.MOVE, timestamp, pointer, new float[] {
                    xpos, ypos });
        default:
        }
    }

}
