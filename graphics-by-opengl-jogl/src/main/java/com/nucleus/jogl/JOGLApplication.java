package com.nucleus.jogl;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;

/**
 * Base class for an application using {@link NucleusRenderer} through JOGL
 * The purpose of this class is to separate JOGL specific init and startup from shared code.
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLApplication extends J2SEWindowApplication {

    protected static final WindowType DEFAULT_WINDOW_TYPE = WindowType.EGL;

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
    protected void setProperties(String[] args) {
        this.windowType = DEFAULT_WINDOW_TYPE;
        super.setProperties(args);
    }

    @Override
    protected J2SEWindow createWindow(Renderers version) {
        switch (windowType) {
            case NEWT:
                j2seWindow = new JOGLGLESWindow(version, this, windowWidth, windowHeight, windowUndecorated,
                        fullscreen, swapInterval);
                break;
            case EGL:
                j2seWindow = new JOGLEGLWindow(version, this, windowWidth, windowHeight);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + windowType);
        }
        return j2seWindow;
    }

}
