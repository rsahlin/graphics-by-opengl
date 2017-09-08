package com.nucleus;

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
     * Call this when GL context is created to create core app and prepare it.
     * 
     * @param width
     * @param height
     */
    protected void internalContextCreated(int width, int height) {
        coreAppStarter.createCoreApp(width, height);
        coreApp.contextCreated(width, height);
    }

}
