package com.nucleus.jogl;

import com.jogamp.opengl.GLAutoDrawable;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * Window for a GLES 2/3 renderer, this class must create the correct {@link GLESWrapper}
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLGLESWindow extends JOGLGLWindow {

    /**
     * 
     * @param version
     * @param coreAppStarter
     * @param config
     * @param width
     * @param height
     * @param undecorated
     * @param fullscreen
     * @param swapInterval
     */
    public JOGLGLESWindow(Renderers version, CoreAppStarter coreAppStarter, SurfaceConfiguration config, int width,
            int height,
            boolean undecorated,
            boolean fullscreen, int swapInterval) {
        super(version, coreAppStarter, config, width, height, undecorated, fullscreen, swapInterval);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (wrapper != null) {
            JOGLGLESUtils.freeNames();
        }
        super.display(drawable);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        SimpleLogger.d(getClass(), "dispose()");
        System.exit(0);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        SimpleLogger.d(getClass(), "init()");
        if (wrapper == null) {
            wrapper = JOGLWrapperFactory.createWrapper(version, drawable.getContext());
        }
        super.init(drawable);
    }

}
