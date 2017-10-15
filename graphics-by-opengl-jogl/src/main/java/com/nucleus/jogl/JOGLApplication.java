package com.nucleus.jogl;

import com.jogamp.opengl.GLProfile;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication;
import com.nucleus.WindowListener;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;

/**
 * Base class for an application using {@link NucleusRenderer} through JOGL
 * The purpose of this class is to separate JOGL specific init and startup from shared code.
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLApplication extends J2SEWindowApplication implements WindowListener {

    protected JOGLGLESWindow window;

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * 
     * @param args
     * @param version
     * @param clientClass Must implement {@link ClientApplication}
     * @throws IllegalArgumentException If clientClass is null
     */
    public JOGLApplication(String[] args, Renderers version, Class<?> clientClass) {
        super(args, version, clientClass);
    }

    @Override
    protected J2SEWindow createWindow(Renderers version) {
        switch (version) {
            case GLES20:
                window = new JOGLGLESWindow(GLProfile.GL2ES2, windowWidth, windowHeight, windowUndecorated, fullscreen, this,
                        swapInterval);
                break;
            case GLES30:
                window = new JOGLGLESWindow(GLProfile.GL4ES3, windowWidth, windowHeight, windowUndecorated, fullscreen, this,
                        swapInterval);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + version);
        }
        window.setGLEVentListener();
        window.setWindowListener(this);
        // Setting window to visible will trigger the GLEventListener, on the same or another thread.
        window.setVisible(true);
        return window;
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

}
