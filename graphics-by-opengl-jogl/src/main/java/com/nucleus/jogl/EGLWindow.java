package com.nucleus.jogl;

import java.nio.IntBuffer;
import java.util.List;

import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.nativewindow.DefaultGraphicsScreen;
import com.jogamp.nativewindow.GraphicsConfigurationFactory;
import com.jogamp.nativewindow.egl.EGLGraphicsDevice;
import com.jogamp.nativewindow.windows.WindowsGraphicsDevice;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.egl.EGL;
import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.common.Constants;
import com.nucleus.common.Environment;
import com.nucleus.egl.EGL14Constants;
import com.nucleus.egl.EGLUtils;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.SurfaceConfiguration;

import jogamp.opengl.egl.EGLDisplayUtil;
import jogamp.opengl.egl.EGLGraphicsConfigurationFactory;
import jogamp.opengl.windows.wgl.WindowsWGLGraphicsConfiguration;

public class EGLWindow extends J2SEWindow implements Runnable,
        GLCapabilitiesChooser {

    Thread thread;

    protected Renderers version;
    protected long EGLContext = Constants.NO_VALUE;
    protected long EglDisplay = Constants.NO_VALUE;
    protected long EglConfig = Constants.NO_VALUE;
    protected long EGLSurface = Constants.NO_VALUE;
    protected long surface;
    protected SurfaceConfiguration surfaceConfig;
    protected RenderContextListener renderListener;
    protected boolean waitForClient = false;
    protected int sleep = 0;

    public EGLWindow(int width, int height, boolean undecorated, boolean fullscreen, GLProfile glProfile,
            CoreApp.CoreAppStarter coreAppStarter, int swapInterval) {
        super(coreAppStarter, width, height);
        Thread t = new Thread(this);
        t.start();

    }

    protected void createEglContext() {
        if (EglDisplay == Constants.NO_VALUE) {

            GLCapabilities caps = new GLCapabilities(GLProfile.getGL4ES3());
            GLCapabilities chosen = new GLCapabilities(GLProfile.getGL4ES3());
            WindowsGraphicsDevice device = new WindowsGraphicsDevice(AbstractGraphicsDevice.DEFAULT_UNIT);

            // AbstractGraphicsDevice agd = NativeWindowFactory.createDevice(
            // NativeWindowFactory.getDefaultDisplayConnection(),
            // true);
            GLWindow nativeWindow = GLWindow.create(new GLCapabilities(GLProfile.get(GLProfile.GL4ES3)));
            nativeWindow.setSize(width, height);
            nativeWindow.setVisible(true);
            EGLGraphicsDevice eglDevice = EGLDisplayUtil.eglCreateEGLGraphicsDevice(nativeWindow.getNativeSurface());
            DefaultGraphicsScreen screen = new DefaultGraphicsScreen(eglDevice, AbstractGraphicsDevice.DEFAULT_UNIT);

            GraphicsConfigurationFactory factory = EGLGraphicsConfigurationFactory.getFactory(device, caps);
            WindowsWGLGraphicsConfiguration chosenConfig = (WindowsWGLGraphicsConfiguration) factory
                    .chooseGraphicsConfiguration(
                            chosen, caps, this, screen,
                            AbstractGraphicsDevice.DEFAULT_UNIT);

            // EGL.eglCreateContext(nativeWindow.getDisplayHandle(), chosenConfig. , share_context, attrib_list)

            // EGL.eglCreateContext(eglDevice.getNativeDisplayID(), eglC, share_context, attrib_list)

            SimpleLogger.d(getClass(), "egl display is null, creating.");
            if (EglDisplay == EGL.EGL_NO_DISPLAY) {
                throw new IllegalArgumentException("Could not create egl display.");
            }

            IntBuffer versionArray = IntBuffer.wrap(new int[2]);
            if (!EGL.eglInitialize(EglDisplay, versionArray, versionArray)) {
                EglDisplay = Constants.NO_VALUE;
                throw new IllegalArgumentException("Could not initialize egl display");
            }
            versionArray.rewind();
            SimpleLogger.d(getClass(),
                    "egl display initialized, version: " + versionArray.get() + "." + versionArray.get());
        }
        if (EglConfig == Constants.NO_VALUE) {
            SimpleLogger.d(getClass(), "egl config is null, creating.");

            int[] eglConfigAttribList = null;
            if (surfaceConfig != null) {
                eglConfigAttribList = EGLUtils.createConfig(surfaceConfig);
            } else {
                // Create default.
                eglConfigAttribList = createDefaultConfigAttribs();
            }
            IntBuffer eglConfigAttribs = IntBuffer.wrap(eglConfigAttribList);
            PointerBuffer configs = PointerBuffer.allocateDirect(10);
            IntBuffer numEglConfigs = IntBuffer.wrap(new int[1]);
            if (!EGL.eglChooseConfig(EglDisplay, eglConfigAttribs,
                    configs, 1, numEglConfigs)) {
                throw new IllegalArgumentException("Could not choose egl config.");
            }
            EglConfig = configs.get(0);
            // surfaceConfig = EGL14Utils.getSurfaceConfig(EglDisplay, EglConfig);
            // SimpleLogger.d(getClass(), "Selected EGL Configuration:");
            // SimpleLogger.d(getClass(), surfaceConfig.toString());
        }
        if (EGLContext == 0) {
            SimpleLogger.d(getClass(), "egl context is null, creating.");
            int[] eglContextAttribList = new int[] {
                    EGL.EGL_CONTEXT_CLIENT_VERSION, version.major,
                    EGL.EGL_NONE
            };
            EGLContext = EGL.eglCreateContext(EglDisplay, EglConfig,
                    EGL.EGL_NO_CONTEXT, IntBuffer.wrap(eglContextAttribList));
            if (EGLContext == 0) {
                throw new IllegalArgumentException("Could not create EGL context");
            }
        }
    }

    protected int[] createDefaultConfigAttribs() {
        return new int[] {
                EGL.EGL_RENDERABLE_TYPE,
                EGL.EGL_OPENGL_ES2_BIT,
                EGL.EGL_RED_SIZE, 8,
                EGL.EGL_GREEN_SIZE, 8,
                EGL.EGL_BLUE_SIZE, 8,
                EGL.EGL_ALPHA_SIZE, 8,
                EGL.EGL_NONE
        };
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

    protected void createEGL() {
        boolean created = EGLContext == 0;
        createEglContext();
        createEglSurface();
        makeCurrent();
        SimpleLogger.d(getClass(), "EGL created and made current");
        SimpleLogger.d(getClass(), "Set egl swap interval to 0");
        if (surfaceConfig.hasExtensionSupport(EGL14Constants.EGL_ANDROID_front_buffer_auto_refresh)) {
            EGL.eglSurfaceAttrib(EglDisplay, EGLSurface, EGL14Constants.EGL_FRONT_BUFFER_AUTO_REFRESH_ANDROID, 1);
            SimpleLogger.d(getClass(),
                    "Set surfaceattrib for: " + EGL14Constants.EGL_ANDROID_front_buffer_auto_refresh);
        }
        if (created) {
            // nucleusActivity.onSurfaceCreated(getWidth(), getHeight());
            // EGL.eglSwapBuffers(EglDisplay, EGLSurface.get);
            // nucleusActivity.contextCreated(getWidth(), getHeight());
        }
    }

    private void makeCurrent() {
        if (!EGL.eglMakeCurrent(EglDisplay, EGLSurface, EGLSurface, EGLContext)) {
            throw new IllegalArgumentException("Could not make egl current");
        }
    }

    public void setRenderContextListener(RenderContextListener listener) {
        this.renderListener = listener;
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
        renderListener.drawFrame();
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Starting EGL surface thread");
        createEGL();
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
        if (renderListener != null) {
            renderListener.surfaceLost();
        }
        SimpleLogger.d(getClass(), "Exiting surface thread");
        thread = null;
    }

    @Override
    public int chooseCapabilities(CapabilitiesImmutable desired, List<? extends CapabilitiesImmutable> available,
            int windowSystemRecommendedChoice) {
        return 0;
    }

}
