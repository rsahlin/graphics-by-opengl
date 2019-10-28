package com.nucleus;

import com.nucleus.Backend.BackendFactory;
import com.nucleus.J2SEWindowApplication.PropertySettings;
import com.nucleus.J2SEWindowApplication.WindowType;
import com.nucleus.mmi.Key;
import com.nucleus.mmi.Pointer;
import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;
import com.nucleus.mmi.core.CoreInput;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * Window that connects to the underlying GL.
 * Use this on platforms where a window needs to be created.
 * This shall take care of informing {@link RenderContextListener} when context is created.
 *
 */
public abstract class J2SEWindow implements WindowListener {

    public static class VideoMode {
        private boolean fullscreen = false;
        private int width;
        private int height;
        private int refresh;
        private int swapInterval;

        public VideoMode(int width, int height, boolean fullscreen, int swapInterval) {
            this.fullscreen = fullscreen;
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isFullScreen() {
            return fullscreen;
        }

        public int getSwapInterval() {
            return swapInterval;
        }

    }

    public static class Configuration {

        private Renderers version;
        private int swapInterval = 1;
        private VideoMode videoMode = new VideoMode(1920, 1080, false, 1);
        private SurfaceConfiguration surfaceConfig;

        public Configuration(Renderers version, SurfaceConfiguration surfaceConfig, VideoMode videoMode) {
            this.version = version;
            this.surfaceConfig = surfaceConfig;
            this.videoMode = videoMode;
        }

        public SurfaceConfiguration getSurfaceConfiguration() {
            return surfaceConfig;
        }

        public VideoMode getVideoMode() {
            return videoMode;
        }

        public Renderers getRenderVersion() {
            return version;
        }

    }

    protected CoreApp coreApp;

    protected BackendFactory factory;
    protected Backend backend;
    protected CoreApp.CoreAppStarter coreAppStarter;
    protected WindowListener windowListener;
    protected VideoMode videoMode;
    protected Renderers version;
    protected WindowType windowType;

    public J2SEWindow(BackendFactory factory, CoreApp.CoreAppStarter coreAppStarter, PropertySettings appSettings) {
        if (coreAppStarter == null) {
            throw new IllegalArgumentException("Appstarter is null");
        }
        windowType = appSettings.windowType;
        this.coreAppStarter = coreAppStarter;
        this.factory = factory;
    }

    /**
     * Creates the underlying window system and render backend
     * Subclasses must implement this method to setup the needed window system and render API.
     * Implementations may defer creation until window framework is up and running (via async callbacks)
     * 
     * @return The set video mode for window or fullscreen.
     * 
     */
    public abstract VideoMode init(PropertySettings appSettings);

    /**
     * Sets callback for {@link WindowListener} events
     * 
     * @param windowListener
     */
    public void setWindowListener(WindowListener windowListener) {
        this.windowListener = windowListener;
    }

    /**
     * Returns the Backend core wrapper, this must be created in subclasses
     * 
     * @return The Backend wrapper or null if not created
     * This may mean that the window has not been made visible.
     */
    public Backend getBackend() {
        return backend;
    }

    /**
     * Sets the CoreApp in this window.
     * This is used to drive rendering by calling {@link CoreApp#drawFrame()}
     * It will also be used to get callback when GL context has been created
     * 
     * @param coreApp
     */
    public void setCoreApp(CoreApp coreApp) {
        this.coreApp = coreApp;
    }

    /**
     * Call this to create the core app, call this before calling {@link #internalContextCreated(int, int)}
     * This will display the splash screen - if needed make sure to swap buffers after calling this method to show the
     * splash.
     * 
     * @param width
     * @param height
     */
    public void internalCreateCoreApp(int width, int height) {
        coreAppStarter.createCoreApp(width, height);
    }

    /**
     * Call this when GL context is created to create core app and prepare it, shall be called after
     * {@link #internalCreateCoreApp(int, int)}
     * This will load the scene and allocate objects, after this method returns the CoreApp shall be driven by calling
     * {@link CoreApp#drawFrame()}
     * 
     * @param width
     * @param height
     */
    public void internalContextCreated(int width, int height) {
        coreApp.contextCreated(width, height);
    }

    /**
     * Used to drive rendering for window types that does not provide paint callbacks.
     * Call this method until app should stop
     * Override in subclasses
     */
    public void drawFrame() {

    }

    /**
     * Handles a pointer action, this will pass on the pointer event to app.
     * 
     * @param action
     * @param type
     * @param xpos
     * @param ypos
     * @param pointer The pointer index, 0 and upwards
     * @param timestamp
     */
    protected void handleMouseEvent(PointerAction action, Type type, int xpos, int ypos, int pointer, long timestamp) {
        switch (action) {
            case DOWN:
                CoreInput.getInstance().pointerEvent(PointerAction.DOWN, type,
                        timestamp, pointer,
                        new float[] { xpos, ypos }, Pointer.DOWN_PRESSURE);
                break;
            case UP:
                CoreInput.getInstance().pointerEvent(PointerAction.UP, type, timestamp, pointer, new float[] {
                        xpos, ypos }, Pointer.DOWN_PRESSURE);
                break;
            case MOVE:
                CoreInput.getInstance().pointerEvent(PointerAction.MOVE, type, timestamp, pointer, new float[] {
                        xpos, ypos }, Pointer.DOWN_PRESSURE);
            default:
        }
    }

    /**
     * Passes the keyevent on to the {@link CoreApp} which will send to registered listeners
     * 
     * @param event
     */
    protected void handleKeyEvent(Key event) {
        CoreInput.getInstance().onKeyEvent(event);
    }

    @Override
    public void resize(int x, int y, int width, int height) {
        if (windowListener != null) {
            windowListener.resize(x, y, width, height);
        }
    }

    @Override
    public void windowClosed() {
        if (windowListener != null) {
            windowListener.windowClosed();
        } else {
            SimpleLogger.d(getClass(), "windowClosed(); - windowListener is null");
        }

    }

    protected void mouseWheelMoved(float rotation, long when) {
        float zoom = rotation * Pointer.ZOOM_FACTOR;
        CoreInput.getInstance().pointerEvent(PointerAction.ZOOM, Pointer.Type.MOUSE, when,
                Pointer.POINTER_1, new float[] {
                        zoom, zoom },
                0);
    }

    /**
     * If fullscreen mode then switch to windowed mode, if window mode then exit.
     */
    protected void onBackPressed() {
        SimpleLogger.d(getClass(), "onBackPressed()");
        if (videoMode.fullscreen) {
            videoMode.fullscreen = false;
            videoMode = setVideoMode(videoMode, 0);
        } else {
            exit();
        }
    }

    protected void exit() {
        if (coreApp != null && coreApp.onBackPressed()) {
            coreApp.setDestroyFlag();
            destroy();
            System.exit(0);
        }
    }

    /**
     * Initializes the window, set visibility and fullscreen mode according to configuration.
     * Call this method to prepare and display the window.
     * 
     */
    protected Configuration prepareWindow(PropertySettings appSettings) {
        videoMode = init(appSettings);
        Configuration configuration = new Configuration(appSettings.version, appSettings.getConfiguration(),
                videoMode);
        videoMode = setVideoMode(videoMode, 0);
        return configuration;
    }

    /**
     * Shows or hides this window
     * 
     * @param visible
     */
    public abstract void setVisible(boolean visible);

    /**
     * Sets the title of the window
     * 
     * @param title
     */
    public abstract void setWindowTitle(String title);

    /**
     * Set the video mode, fullscreen or windowed and resolution
     * 
     * @param The video mode to set
     * @param Index to monitor to set the fullscreen mode for
     * @return
     */
    public abstract VideoMode setVideoMode(VideoMode videoMode, int monitorIndex);

    /**
     * Destroy the window(s) and release window resources
     */
    public abstract void destroy();

}
