package com.nucleus.jogl;

import com.jogamp.opengl.GLAutoDrawable;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;

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
     * @param width
     * @param height
     * @param undecorated
     * @param fullscreen
     * @param swapInterval
     */
    public JOGLGLESWindow(Renderers version, CoreAppStarter coreAppStarter, int width, int height, boolean undecorated,
            boolean fullscreen, int swapInterval) {
        super(version, coreAppStarter, width, height, undecorated, fullscreen, swapInterval);
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
            switch (version) {
                case GLES20:
                    wrapper = new JOGLGLES20Wrapper(drawable.getGL().getGL2ES2());
                    break;
                case GLES30:
                case GLES31:
                    wrapper = new JOGLGLES30Wrapper(drawable.getGL().getGL4ES3());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid renderer version " + version);
            }
        }
        super.init(drawable);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        SimpleLogger.d(getClass(), "reshape(" + x + "," + y + " : " + width + "," + height + ")");
        super.reshape(drawable, x, y, width, height);
    }

}
