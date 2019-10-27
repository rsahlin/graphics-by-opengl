package com.nucleus.jogl;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication;
import com.nucleus.common.Type;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;

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
        appSettings.windowType = DEFAULT_WINDOW_TYPE;
        super.setProperties(args);
    }

    @Override
    protected J2SEWindow createWindow(Renderers version) {
        switch (appSettings.windowType) {
            case NEWT:
            case JAWT:
                j2seWindow = new JOGLGLESWindow(new JOGLWrapperFactory(), this, appSettings);
                break;
            case EGL:
                j2seWindow = new JOGLEGLWindow(new JOGLWrapperFactory(), this, appSettings);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + appSettings.windowType);
        }
        return j2seWindow;
    }

}
