package com.nucleus;

import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.J2SEWindow.Configuration;
import com.nucleus.common.Environment;
import com.nucleus.common.Type;
import com.nucleus.properties.Property;
import com.nucleus.properties.Property.BooleanGetter;
import com.nucleus.properties.Property.Getter;
import com.nucleus.properties.Property.IntGetter;
import com.nucleus.properties.Property.VersionGetter;
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

    static class WindowTypeGetter implements Getter<WindowType> {

        @Override
        public WindowType getProperty(String value, WindowType defaultValue) {
            return defaultValue;
        }
    }

    public enum WindowProperties {
        WINDOW_TYPE("WINDOW-TYPE", new WindowTypeGetter()),
        WINDOW_WIDTH("WINDOW-WIDTH", new IntGetter()),
        WINDOW_HEIGHT("WINDOW-HEIGHT", new IntGetter()),
        WINDOW_UNDECORATED("WINDOW_UNDECORATED", new BooleanGetter()),
        FULLSCREEN("FULLSCREEN", new BooleanGetter()),
        SAMPLES("SAMPLES", new IntGetter()),
        ALPHA_BITS("ALPHA_BITS", new IntGetter()),
        NATIVE_GLES("GLES", new BooleanGetter()),
        FORCE_VERSION("FORCE-VERSION", new BooleanGetter()),
        SET_VERSION("SET-VERSION", new VersionGetter());

        public final Getter<?> getter;
        public final String key;

        private WindowProperties(String key, Getter<?> getter) {
            this.key = key;
            this.getter = getter;
        }

        public static WindowProperties get(String key) {
            for (WindowProperties wp : values()) {
                if (key.toUpperCase().startsWith(wp.key)) {
                    return wp;
                }
            }
            return null;
        }

    }

    class AppProperty extends Property {

        final WindowProperties property;

        AppProperty(WindowProperties key, String value) {
            super(key.name(), value);
            this.property = key;
        }

        Integer getInt() {
            if (property.getter instanceof IntGetter) {
                return ((IntGetter) property.getter).getProperty(getValue(), 0);
            }
            throw new IllegalArgumentException("No int value for " + property);
        }

        Boolean getBoolean() {
            if (property.getter instanceof BooleanGetter) {
                return ((BooleanGetter) property.getter).getProperty(getValue(), false);
            }
            throw new IllegalArgumentException("No boolean value for " + property);
        }

        WindowType getWindowType() {
            if (property.getter instanceof WindowTypeGetter) {
                return ((WindowTypeGetter) property.getter).getProperty(getValue(), null);
            }
            throw new IllegalArgumentException("No WindowType value for " + property);
        }

        Renderers getVersion() {
            if (property.getter instanceof VersionGetter) {
                return ((VersionGetter) property.getter).getProperty(getValue(), null);
            }
            throw new IllegalArgumentException("No Renderers version value for " + property);

        }

    }

    protected static final Property[] PROPERTIES = new Property[] {
            new Property(WindowProperties.WINDOW_TYPE.name(), "WINDOWTYPE"),
            new Property(WindowProperties.WINDOW_WIDTH.name(), "WINDOW"),
            new Property(WindowProperties.WINDOW_HEIGHT.name(), "WINDOW-HEIGHT"),
            new Property(WindowProperties.WINDOW_UNDECORATED.name(), "WINDOW-UNDECORATED"),
            new Property(WindowProperties.FULLSCREEN.name(), "FULLSCREEN"),
            new Property(WindowProperties.SAMPLES.name(), "SAMPLES"),
            new Property(WindowProperties.NATIVE_GLES.name(), "GLES"),
            new Property(WindowProperties.FORCE_VERSION.name(), "FORCE_VERSION"),
            new Property(WindowProperties.SET_VERSION.name(), "SET_VERSION") };

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
        windowConfiguration.version = windowConfiguration.setVersion != null ? windowConfiguration.setVersion : version;
        createCoreWindows(windowConfiguration.version);
    }

    /**
     * Reads arguments from the VM and sets
     * 
     * @param args
     */
    protected void setProperties(String[] args) {
        listProperties();
        setSystemProperties();
        if (args == null) {
            return;
        }
        for (String str : args) {
            setProperty(str);
        }
    }

    protected void listProperties() {

    }

    protected void setSystemProperties() {
        String swap = Environment.getInstance().getProperty(Environment.Property.SWAPINTERVAL);
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
        AppProperty property = getProperty(str);
        if (property != null) {
            switch (property.property) {
                case WINDOW_WIDTH:
                    windowConfiguration.width = property.getInt();
                    break;
                case WINDOW_HEIGHT:
                    windowConfiguration.height = property.getInt();
                    break;
                case WINDOW_UNDECORATED:
                    windowConfiguration.windowUndecorated = property.getBoolean();
                    break;
                case FULLSCREEN:
                    windowConfiguration.fullscreen = property.getBoolean();
                    break;
                case WINDOW_TYPE:
                    windowConfiguration.windowType = property.getWindowType();
                case ALPHA_BITS:
                    alpha = property.getInt();
                    break;
                case SAMPLES:
                    samples = property.getInt();
                    break;
                case NATIVE_GLES:
                    windowConfiguration.nativeGLES = property.getBoolean();
                    break;
                case FORCE_VERSION:
                    windowConfiguration.forceVersion = property.getBoolean();
                    break;
                case SET_VERSION:
                    windowConfiguration.setVersion = property.getVersion();
                    break;
            }
        }

    }

    protected AppProperty getProperty(String str) {
        WindowProperties p = WindowProperties.get(str);
        if (p != null) {
            String value = str.substring(p.key.length() + 1);
            if (p.getter.getProperty(value, null) != null) {
                return new AppProperty(p, value);
            } else {
                SimpleLogger.d(getClass(), "Error fetching property value for " + p + ", value: " + value);
            }
        }
        return null;
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
