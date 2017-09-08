package com.nucleus;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.matrix.j2se.J2SEMatrixEngine;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.texturing.J2SEImageFactory;

/**
 * Base class for J2SE Windowd application, use this for implementations that need to create a window
 *
 */
public abstract class J2SEWindowApplication implements CoreAppStarter, WindowListener {

    public static final String WINDOW_WIDTH_KEY = "WINDOW-WIDTH";
    public static final String WINDOW_HEIGHT_KEY = "WINDOW-HEIGHT";
    public static final String WINDOW_UNDECORATED_KEY = "WINDOW-UNDECORATED";
    public static final String FULLSCREEN_KEY = "FULLSCREEN";

    protected CoreApp coreApp;
    protected Class<?> clientClass;
    protected int swapInterval = 1;
    protected int windowWidth = 480;
    protected int windowHeight = 800;
    protected boolean windowUndecorated = false;
    protected boolean fullscreen = false;
    protected J2SEWindow j2seWindow;
    protected RenderContextListener contextListener;

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * The constructor will create the window to be used by calling {@link #createCoreWindows(Renderers)}
     * When window is ready {@link #createCoreApp(int, int)} should be called.
     * 
     * @param args
     * @param version
     * @param clientClass Must implement {@link ClientApplication}
     * @throws IllegalArgumentException If clientClass is null
     */
    public J2SEWindowApplication(String[] args, Renderers version, Class<?> clientClass) {
        SimpleLogger.setLogger(new J2SELogger());
        if (clientClass == null) {
            throw new IllegalArgumentException("ClientClass is null");
        }
        this.clientClass = clientClass;
        setProperties(args);
        createCoreWindows(version);
    }

    /**
     * Reads arguments from the VM and sets
     * 
     * @param args
     */
    protected void setProperties(String[] args) {
        if (args == null) {
            return;
        }
        for (String str : args) {
            setProperty(str);
        }
    }

    /**
     * Called from {@link #setProperties(String[])} to parse one property string.
     * 
     * @param str
     */
    protected void setProperty(String str) {
        if (str.toUpperCase().startsWith(WINDOW_WIDTH_KEY)) {
            windowWidth = Integer.parseInt(str.substring(WINDOW_WIDTH_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_WIDTH_KEY + " set to " + windowWidth);
        }
        if (str.toUpperCase().startsWith(WINDOW_HEIGHT_KEY)) {
            windowHeight = Integer.parseInt(str.substring(WINDOW_HEIGHT_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_HEIGHT_KEY + " set to " + windowHeight);
        }
        if (str.toUpperCase().startsWith(WINDOW_UNDECORATED_KEY)) {
            windowUndecorated = Boolean.parseBoolean(str.substring(WINDOW_UNDECORATED_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_UNDECORATED_KEY + " set to " + windowUndecorated);
        }
        if (str.toUpperCase().startsWith(FULLSCREEN_KEY)) {
            fullscreen = Boolean.parseBoolean(str.substring(FULLSCREEN_KEY.length() + 1));
            SimpleLogger.d(getClass(), FULLSCREEN_KEY + " set to " + fullscreen);
        }

    }

    protected abstract J2SEWindow createGLES20Window();

    @Override
    public void createCoreWindows(Renderers version) {
        switch (version) {
        case GLES20:
            j2seWindow = createGLES20Window();
            break;
        default:
            throw new IllegalArgumentException("Not implemented for version " + version);
        }
    }

    @Override
    public void createCoreApp(int width, int height) {
        NucleusRenderer renderer = RendererFactory.getRenderer(j2seWindow.getGLESWrapper(), new J2SEImageFactory(),
                new J2SEMatrixEngine());
        coreApp = CoreApp.createCoreApp(width, height, renderer, clientClass);
        j2seWindow.setCoreApp(coreApp);
    }

    /**
     * Returns the {@link NucleusRenderer} renderer - do NOT call this method before {@link #contextCreated(int, int)}
     * has been called by the renderer.
     * 
     * 
     * @return The renderer, or null if {@link #contextCreated(int, int)} has not been called by the renderer.
     */
    public NucleusRenderer getRenderer() {
        if (coreApp == null) {
            return null;
        }
        return coreApp.getRenderer();
    }

}
