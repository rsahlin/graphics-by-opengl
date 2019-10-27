package com.nucleus.jogl;

import com.jogamp.opengl.GLAutoDrawable;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.J2SEWindowApplication.PropertySettings;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper;

/**
 * Window for a GLES 2/3 renderer, this class must create the correct {@link GLESWrapper}
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLGLESWindow extends JOGLGLWindow {

    public JOGLGLESWindow(BackendFactory factory, CoreAppStarter coreAppStarter, PropertySettings appSettings) {
        super(factory, coreAppStarter, appSettings);
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

}
