package com.nucleus.jogl;

import java.nio.IntBuffer;
import java.util.List;

import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.nativewindow.windows.WindowsGraphicsDevice;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.egl.EGL;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp;
import com.nucleus.J2SEWindowApplication.WindowType;
import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.Constants;
import com.nucleus.common.Environment;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.FrameRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

public class JOGLEGLWindow extends JOGLGLWindow implements Runnable, GLCapabilitiesChooser, GLEventListener {

    Thread thread;

    protected BackendFactory factory;
    protected long EGLContext = Constants.NO_VALUE;
    protected long EglDisplay = Constants.NO_VALUE;
    protected long EglConfig = Constants.NO_VALUE;
    protected long EGLSurface = Constants.NO_VALUE;
    protected long surface;
    protected SurfaceConfiguration surfaceConfig;
    protected FrameRenderer frameRenderer;
    protected boolean waitForClient = false;
    protected int sleep = 0;
    protected GLContext glContext;
    protected GLWindow nativeWindow;
    protected boolean visible = false;
    GLCapabilities glCapabilities;
    GLDrawable glDrawable;

    public JOGLEGLWindow(Renderers version, WindowType windowType, BackendFactory factory,
            CoreApp.CoreAppStarter coreAppStarter,
            SurfaceConfiguration config,
            int width, int height) {
        super(version, windowType, factory, coreAppStarter, config, width, height, false, true, 1);
    }

    @Override
    public void init() {
        glCapabilities = new GLCapabilities(getProfile(version));
        glCapabilities.setSampleBuffers(config.getSamples() > 0);
        glCapabilities.setNumSamples(config.getSamples());
        glCapabilities.setBackgroundOpaque(true);
        glCapabilities.setAlphaBits(0);
        createGLWindow();
        /**
         * Start a thread to rendering using EGL and GL
         */
        // Thread t = new Thread(this);
        // t.start();
    }

    protected void createGLWindow() {
        GLCapabilities caps = new GLCapabilities(GLProfile.getGL4ES3());
        GLCapabilities chosen = new GLCapabilities(GLProfile.getGL4ES3());
        WindowsGraphicsDevice device = new WindowsGraphicsDevice(AbstractGraphicsDevice.DEFAULT_UNIT);

        // AbstractGraphicsDevice agd = NativeWindowFactory.createDevice(
        // NativeWindowFactory.getDefaultDisplayConnection(),
        // true);
        nativeWindow = GLWindow.create(new GLCapabilities(GLProfile.get(GLProfile.GL4ES3)));
        nativeWindow.setSize(width, height);
        // nativeWindow.setUndecorated(undecorated);
        nativeWindow.setRealized(true);
        nativeWindow.addGLEventListener(this);
    }

    protected void createEglContext(GLAutoDrawable drawable) {
        if (EglDisplay == Constants.NO_VALUE) {
            GLProfile.initSingleton();
            IntBuffer major = BufferUtils.createIntBuffer(1);
            major.put(1);
            IntBuffer minor = BufferUtils.createIntBuffer(1);
            minor.put(4);
            jogamp.opengl.egl.EGLContext.getCurrent().makeCurrent();
            long display = EGL.eglGetDisplay(EGL.EGL_DEFAULT_DISPLAY);

            // EGLContext = jogamp.opengl.egl.EGLContext.getCurrent()
            // glContext = glDrawable.createContext(null);
            SimpleLogger.d(getClass(), "GLProfile isInitialized " + GLProfile.isInitialized());
            IntBuffer attribList = createDefaultConfigAttribs();
            PointerBuffer configs = PointerBuffer.allocateDirect(100);
            IntBuffer count = BufferUtils.createIntBuffer(1);
            // EGL.eglChooseConfig(display, attribList, configs, 100, count);
            // GraphicsConfigurationFactory factory = EGLGraphicsConfigurationFactory.getFactory(drawable,
            // glCapabilities);
            // WindowsWGLGraphicsConfiguration chosenConfig = (WindowsWGLGraphicsConfiguration) factory
            // .chooseGraphicsConfiguration(chosen, caps, this, screen, AbstractGraphicsDevice.DEFAULT_UNIT);

            // EGLGraphicsConfiguration eglConfig = EGLGraphicsConfigurationFactory.chooseGraphicsConfigurationStatic(
            // chosen, glCapabilities, this, screen, VisualIDHolder.VID_UNDEFINED, true);

            // EGL.eglCreateContext(nativeWindow.getDisplayHandle(), chosenConfig. , share_context, attrib_list)

            // EGL.eglCreateContext(eglDevice.getNativeDisplayID(), eglC, share_context, attrib_list)

            backend = factory.createBackend(version, null, glContext);
        }

    }

    protected IntBuffer createDefaultConfigAttribs() {
        int[] attribValues = new int[] {
                EGL.EGL_RENDERABLE_TYPE,
                EGL.EGL_OPENGL_ES2_BIT,
                EGL.EGL_RED_SIZE, 8,
                EGL.EGL_GREEN_SIZE, 8,
                EGL.EGL_BLUE_SIZE, 8,
                EGL.EGL_ALPHA_SIZE, 8,
                EGL.EGL_NONE };
        IntBuffer attribs = BufferUtils.createIntBuffer(attribValues.length);
        attribs.put(attribValues);
        return attribs;
    }

    protected void createEglSurface() {
        if (EGLSurface == 0) {
            int[] eglSurfaceAttribList = new int[] {
                    EGL.EGL_NONE
            };
            IntBuffer eglSurfaceAttribs = IntBuffer.wrap(eglSurfaceAttribList);
            // turn our SurfaceControl into a Surface
            EGLSurface = EGL.eglCreateWindowSurface(EglDisplay, EglConfig, surface,
                    eglSurfaceAttribs);
            if (EGLSurface == 0) {
                SimpleLogger.d(getClass(), "Could not create window surface");
                throw new IllegalArgumentException("Could not create egl surface.");
            }
        }
    }

    public void setRenderContextListener(FrameRenderer frameRenderer) {
        this.frameRenderer = frameRenderer;
    }

    /**
     * Specify if a call to eglWaitClient() is made after eglSwapBuffers
     * 
     * @param waitClient True to call eglWaitClient() efter swapbuffers
     */
    public void setWaitClient(boolean waitClient) {
        SimpleLogger.d(getClass(), "Setting waitForClient to " + waitClient);
        this.waitForClient = waitClient;
    }

    /**
     * Sets number of millis to sleep after swapping buffers, and waitForClient if enabled.
     * 
     * @param millis
     */
    public void setEGLSleep(int millis) {
        SimpleLogger.d(getClass(), "Setting sleep to " + millis);
        sleep = millis;
    }

    /**
     * Sets the egl swap interval, if no EGLDisplay exists then nothing is done.
     * 
     * @param interval
     */
    public void setEGLSwapInterval(int interval) {
        if (EglDisplay != 0) {
            EGL.eglSwapInterval(EglDisplay, interval);
            SimpleLogger.d(getClass(), "set EGLSwapInterval to " + interval);
        } else {
            SimpleLogger.d(getClass(), "EGLDisplay is null, cannot set swapInterval");
        }
    }

    /**
     * Sets an egl surfaceattrib, if EGLDisplay or EGLSurface is null then nothing is done.
     * 
     * @param attribute
     * @param value
     */
    public void setEGLSurfaceAttrib(int attribute, int value) {
        if (EglDisplay != 0 && EGLSurface != 0) {
            EGL.eglSurfaceAttrib(EglDisplay, EGLSurface, attribute, value);
            SimpleLogger.d(getClass(), "set EGL surfaceattrib: " + attribute + " : " + value);
        } else {
            SimpleLogger.d(getClass(), "Could not set EGL surfaceattrib, display or surface is null");
        }
    }

    @Override
    public void drawFrame() {
        frameRenderer.renderFrame();
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Starting EGL surface thread");
        Environment env = Environment.getInstance();
        while (surface != 0) {
            drawFrame();
            if (EGLSurface != 0) {
                long start = System.currentTimeMillis();
                EGL.eglSwapBuffers(EglDisplay, EGLSurface);
                boolean eglWaitGL = env.isProperty(Environment.Property.EGLWAITGL, false);
                if (eglWaitGL) {
                    EGL.eglWaitGL();
                }
                FrameSampler.getInstance().addTag(FrameSampler.Samples.EGLSWAPBUFFERS.name() + "-WAITGL=" + eglWaitGL,
                        start,
                        System.currentTimeMillis(), FrameSampler.Samples.EGLSWAPBUFFERS.detail);
                if (waitForClient) {
                    start = System.currentTimeMillis();
                    EGL.eglWaitClient();
                    FrameSampler.getInstance().addTag(FrameSampler.Samples.EGLWAITNATIVE, start,
                            System.currentTimeMillis());
                }
                if (sleep > 0) {
                    // System.sleep(sleep);
                }
            }
        }
        if (frameRenderer != null) {
            frameRenderer.surfaceLost();
        }
        SimpleLogger.d(getClass(), "Exiting surface thread");
        thread = null;
    }

    @Override
    public int chooseCapabilities(CapabilitiesImmutable desired, List<? extends CapabilitiesImmutable> available,
            int windowSystemRecommendedChoice) {
        return 0;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (nativeWindow != null) {
            nativeWindow.setVisible(visible);
        }
    }

    @Override
    public void setWindowTitle(String title) {
        if (nativeWindow != null) {
            nativeWindow.setTitle(title);
        }
    }

    @Override
    protected void setFullscreenMode(boolean fullscreen) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    protected void destroy() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        this.glDrawable = drawable;
        createEglContext(drawable);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

}
