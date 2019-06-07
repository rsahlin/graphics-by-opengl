package com.nucleus.jogl;

import com.jogamp.opengl.GLAutoDrawable;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * Window for a GLES 2/3 renderer, this class must create the correct {@link GLESWrapper}
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLGLESWindow extends JOGLGLWindow {

    public JOGLGLESWindow(Renderers version, BackendFactory factory, CoreAppStarter coreAppStarter,
            SurfaceConfiguration config, int width,
            int height, boolean undecorated, boolean fullscreen, int swapInterval) {
        super(version, factory, coreAppStarter, config, width, height, undecorated, fullscreen, swapInterval);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (backend != null) {
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
        if (backend == null) {

            backend = factory.createBackend(version, null, drawable.getContext());
        }
        super.init(drawable);
    }

    @Override
    public void setVisible(boolean visible) {
        glWindow.setVisible(visible);
    }

}
