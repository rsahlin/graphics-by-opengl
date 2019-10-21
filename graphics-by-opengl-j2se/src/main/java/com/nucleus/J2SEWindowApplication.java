package com.nucleus;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.J2SEWindow.Configuration;
import com.nucleus.common.Environment;
import com.nucleus.common.Type;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.SurfaceConfiguration;
import com.nucleus.renderer.Window;
import com.nucleus.texturing.AWTImageFactory;
import com.nucleus.texturing.BaseImageFactory;

/**
 * Base class for J2SE Windowed application, use this for implementations that need to create a window
 *
 */
public abstract class J2SEWindowApplication implements CoreAppStarter, WindowListener {

    public enum WindowType {
        /**
         * Only available when using LWJGL
         */
        GLFW(),
        /**
         * Only avaialable when using JOGL
         */
        NEWT(),
        JAWT(),
        EGL();
    }

    /**
     * To select GLFW or JAWT window
     */
    protected static final String WINDOW_TYPE_KEY = "WINDOWTYPE";

    public static final String WINDOW_WIDTH_KEY = "WINDOW-WIDTH";
    public static final String WINDOW_HEIGHT_KEY = "WINDOW-HEIGHT";
    public static final String WINDOW_UNDECORATED_KEY = "WINDOW-UNDECORATED";
    public static final String FULLSCREEN_KEY = "FULLSCREEN";
    public static final String SAMPLES = "SAMPLES";
    public static final String ALPHA_BITS = "ALPHA";
    public static final String NATIVE_GLES = "GLES";
    public static final String FORCE_GLES_VERSION = "FORCE_GLES_VERSION";
    public static final int DEFAULT_DEPTH_BITS = 32;
    public static final int DEFAULT_SAMPLES = 4;

    protected CoreApp coreApp;

    protected J2SEWindow j2seWindow;
    protected Configuration windowConfiguration = new Configuration();
    /**
     * Number of bits of alpha for surface
     */
    protected int alpha = 8;
    /**
     * Number of samples for surface
     */
    protected int samples = DEFAULT_SAMPLES;
    protected int depthBits = DEFAULT_DEPTH_BITS;

    protected RenderContextListener contextListener;

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * The constructor will create the window to be used by calling {@link #createCoreWindows(Renderers)}
     * When window is ready {@link #createCoreApp(int, int)} should be called.
     * 
     * @param args
     * @param version
     * @param clientClass Implementing class for {@link ClientApplication}, must implement {@link ClientApplication}
     * interface
     * @throws IllegalArgumentException If clientClass is null
     */
    public J2SEWindowApplication(String[] args, Renderers version, Type<Object> clientClass) {
        SimpleLogger.setLogger(new J2SELogger());
        BaseImageFactory.setFactory(new AWTImageFactory());
        CoreApp.setClientClass(clientClass);
        setProperties(args);
        windowConfiguration.surfaceConfig = getConfiguration();
        windowConfiguration.version = version;
        createCoreWindows(version);
    }

    /**
     * Reads arguments from the VM and sets
     * 
     * @param args
     */
    protected void setProperties(String[] args) {
        setSystemProperties();
        if (args == null) {
            return;
        }
        for (String str : args) {
            setProperty(str);
        }
    }

    protected void setSystemProperties() {
        String swap = Environment.getInstance().getProperty(Environment.Property.EGLSWAPINTERVAL);
        if (swap != null && swap.length() > 0) {
            windowConfiguration.swapInterval = Integer.parseInt(swap);
        }
    }

    /**
     * Called from {@link #setProperties(String[])} to parse one property string.
     * 
     * @param str
     */
    protected void setProperty(String str) {
        if (str.toUpperCase().startsWith(WINDOW_WIDTH_KEY)) {
            windowConfiguration.width = Integer.parseInt(str.substring(WINDOW_WIDTH_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_WIDTH_KEY + " set to " + windowConfiguration.width);
        }
        if (str.toUpperCase().startsWith(WINDOW_HEIGHT_KEY)) {
            windowConfiguration.height = Integer.parseInt(str.substring(WINDOW_HEIGHT_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_HEIGHT_KEY + " set to " + windowConfiguration.height);
        }
        if (str.toUpperCase().startsWith(WINDOW_UNDECORATED_KEY)) {
            windowConfiguration.windowUndecorated = Boolean
                    .parseBoolean(str.substring(WINDOW_UNDECORATED_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_UNDECORATED_KEY + " set to " + windowConfiguration.windowUndecorated);
        }
        if (str.toUpperCase().startsWith(FULLSCREEN_KEY)) {
            windowConfiguration.fullscreen = Boolean.parseBoolean(str.substring(FULLSCREEN_KEY.length() + 1));
            SimpleLogger.d(getClass(), FULLSCREEN_KEY + " set to " + windowConfiguration.fullscreen);
        }
        if (str.toUpperCase().startsWith(WINDOW_TYPE_KEY)) {
            windowConfiguration.windowType = WindowType.valueOf(str.substring(WINDOW_TYPE_KEY.length() + 1));
            SimpleLogger.d(getClass(), WINDOW_TYPE_KEY + " set to " + windowConfiguration.windowType);
        }
        if (str.toUpperCase().startsWith(ALPHA_BITS)) {
            alpha = Integer.parseInt(str.substring(ALPHA_BITS.length() + 1));
            SimpleLogger.d(getClass(), ALPHA_BITS + " set to " + alpha);
        }
        if (str.toUpperCase().startsWith(SAMPLES)) {
            samples = Integer.parseInt(str.substring(SAMPLES.length() + 1));
            SimpleLogger.d(getClass(), SAMPLES + " set to " + samples);
        }
        if (str.toUpperCase().startsWith(NATIVE_GLES)) {
            windowConfiguration.nativeGLES = Boolean.parseBoolean(str.substring(NATIVE_GLES.length() + 1));
            SimpleLogger.d(getClass(), NATIVE_GLES + " set to " + windowConfiguration.nativeGLES);
        }
        if (str.toUpperCase().startsWith(FORCE_GLES_VERSION)) {
            windowConfiguration.forceGLESVersion = str.substring(FORCE_GLES_VERSION.length() + 1);
            SimpleLogger.d(getClass(), FORCE_GLES_VERSION + " set to " + windowConfiguration.forceGLESVersion);
        }

    }

    /**
     * Create and setup the window implementation based on the renderer version
     * The returned window shall be ready to be used.
     * 
     * @return
     */
    protected abstract J2SEWindow createWindow(Renderers version);

    @Override
    public void createCoreWindows(Renderers version) {
        j2seWindow = createWindow(version);
        j2seWindow.setWindowListener(this);
        Window.getInstance().setPlatformWindow(j2seWindow);
    }

    @Override
    public void createCoreApp(int width, int height) {
        NucleusRenderer renderer = RendererFactory.getRenderer(j2seWindow.getBackend());
        coreApp = CoreApp.createCoreApp(renderer, windowConfiguration);
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

    @Override
    public void resize(int x, int y, int width, int height) {
        if (coreApp != null) {
            coreApp.getRenderer().resizeWindow(x, y, width, height);
        }
    }

    @Override
    public void windowClosed() {
        if (coreApp != null) {
            coreApp.setDestroyFlag();
        } else {
            SimpleLogger.d(getClass(), "windowClosed() coreApp is null");
        }
    }

    /**
     * Returns surface configuration
     * 
     * @return
     */
    protected SurfaceConfiguration getConfiguration() {
        SurfaceConfiguration config = new SurfaceConfiguration();
        config.setDepthBits(depthBits);
        config.setAlphaBits(alpha);
        config.setSamples(samples);
        return config;
    }

    protected void tearDown() {
        if (coreApp != null) {
            if (j2seWindow != null) {
                j2seWindow.setVisible(false);
                j2seWindow.destroy();
            }
            coreApp.setDestroyFlag();
        }
    }

}
