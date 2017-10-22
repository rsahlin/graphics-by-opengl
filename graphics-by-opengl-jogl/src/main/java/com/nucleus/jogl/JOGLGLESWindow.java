package com.nucleus.jogl;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper;

/**
 * Window for a GLES 2/3 renderer, this class must create the correct {@link GLESWrapper}
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLGLESWindow extends JOGLGLWindow {

    protected String glProfile;

    /**
     * 
     * @param profile GLProfile
     * @param width
     * @param height
     * @param undecorated
     * @param fullscreen
     * @param coreAppStarter
     * @param swapInterval
     */
    public JOGLGLESWindow(String profile, int width, int height, boolean undecorated, boolean fullscreen,
            CoreAppStarter coreAppStarter, int swapInterval) {
        super(width, height, undecorated, fullscreen, GLProfile.get(profile), coreAppStarter, swapInterval);
        glProfile = profile;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (wrapper != null) {
            ((JOGLGLES20Wrapper) wrapper).freeNames();
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
            switch (glProfile) {
                case GLProfile.GL2ES2:
                    wrapper = new JOGLGLES20Wrapper(drawable.getGL().getGL2ES2());
                    break;
                case GLProfile.GL4ES3:
                    wrapper = new JOGLGLES30Wrapper(drawable.getGL().getGL4ES3());
                    break;
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
