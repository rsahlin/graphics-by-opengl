package com.nucleus.jogl;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication;
import com.nucleus.common.Type;
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

    protected static final WindowType DEFAULT_WINDOW_TYPE = WindowType.NEWT;

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * 
     * @param args
     * @param version
     * @param clientClass Implementing class for {@link ClientApplication}, must implement {@link ClientApplication}
     * interface
     * @throws IllegalArgumentException If clientClass is null
     */
    public JOGLApplication(String[] args, Renderers version, Type<Object> clientClass) {
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
                j2seWindow = new JOGLGLESWindow(version, this, getConfiguration(), windowWidth, windowHeight,
                        windowUndecorated,
                        fullscreen, swapInterval);
                break;
            case EGL:
                j2seWindow = new JOGLEGLWindow(version, this, getConfiguration(), windowWidth, windowHeight);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + windowType);
        }
        j2seWindow.setVisible(true);
        return j2seWindow;
    }

}
