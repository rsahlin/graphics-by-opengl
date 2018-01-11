package com.nucleus.android;

import com.nucleus.SimpleLogger;
import com.nucleus.android.egl14.EGL14Utils;
import com.nucleus.common.Environment;
import com.nucleus.egl.EGL14Constants;
import com.nucleus.egl.EGLUtils;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.SurfaceConfiguration;

import android.annotation.SuppressLint;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("ClickableViewAccessibility")
public class EGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    Thread thread;

    protected NucleusActivity nucleusActivity;
    protected Renderers version;
    protected EGLContext EGLContext;
    protected EGLDisplay EglDisplay;
    protected EGLConfig EglConfig;
    protected EGLSurface EGLSurface;
    protected SurfaceConfiguration surfaceConfig;
    protected Surface surface;
    protected RenderContextListener renderListener;
    protected final int eglSwapInterval;
    Environment env;
    /**
     * Special surface attribs that may be specified when creating the surface - see
     * https://www.khronos.org/registry/EGL/sdk/docs/man/html/eglCreateWindowSurface.xhtml
     * Shall be terminateded by EGL_NONE
     * EGL_RENDER_BUFFER
     * EGL_VG_ALPHA_FORMAT
     * EGL_VG_COLORSPACE
     */
    protected int[] surfaceAttribs;

    /**
     * 
     * @param surfaceConfig
     * @param version
     * @param nucleusActivity
     * @param swapInterval
     * @param surfaceAttribs Surface attribs that may be specified when creating the surface - see
     * https://www.khronos.org/registry/EGL/sdk/docs/man/html/eglCreateWindowSurface.xhtml
     * EGL_RENDER_BUFFER
     * EGL_VG_ALPHA_FORMAT
     * EGL_VG_COLORSPACE
     * Shall be terminateded by EGL_NONE
     * 
     */
    public EGLSurfaceView(SurfaceConfiguration surfaceConfig, Renderers version, NucleusActivity nucleusActivity,
            int swapInterval, int[] surfaceAttribs) {
        super(nucleusActivity);
        env = Environment.getInstance();
        this.nucleusActivity = nucleusActivity;
        this.surfaceConfig = surfaceConfig;
        this.version = version;
        getHolder().addCallback(this);
        eglSwapInterval = swapInterval;
        this.surfaceAttribs = surfaceAttribs;
    }

    /**
     * Returns the EGLDisplay that shall be used, default is to fetch EGL_DEFAULT_DISPLAY
     * 
     * @return
     */
    protected EGLDisplay getEGLDisplay() {
        return EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    }

    /**
     * Creates a list of egl config attribs for the surface config, if surfaceConfig is null then
     * {@link #createDefaultConfigAttribs()} is called.
     * 
     * @param surfaceConfig
     * @return
     */
    protected int[] createEGLConfigAttribs(SurfaceConfiguration surfaceConfig) {
        if (surfaceConfig != null) {
            return EGLUtils.createConfig(surfaceConfig);
        } else {
            // Create default.
            return createDefaultConfigAttribs();
        }
    }

    /**
     * Creates the egl context, default is to set client version to {@link #version#major}.
     * 
     * @throws IllegalArgumentException If context could not be created
     */
    protected void createEGLContext() {
        int[] eglContextAttribList = new int[] {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, version.major,
                EGL14.EGL_NONE
        };
        EGLContext = EGL14.eglCreateContext(EglDisplay, EglConfig,
                EGL14.EGL_NO_CONTEXT, eglContextAttribList, 0);
        if (EGLContext == null) {
            throw new IllegalArgumentException("Could not create EGL context");
        }
    }

    /**
     * Creates the egl context, first getting the display by calling
     */
    protected void createEglContext() {
        if (EglDisplay == null) {
            SimpleLogger.d(getClass(), "egl display is null, creating.");
            EglDisplay = getEGLDisplay();
            if (EglDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new IllegalArgumentException("Could not get egl display.");
            }

            int[] versionArray = new int[2];
            if (!EGL14.eglInitialize(EglDisplay, versionArray, 0, versionArray, 1)) {
                EglDisplay = null;
                throw new IllegalArgumentException("Could not initialize egl display");
            }
            SimpleLogger.d(getClass(), "egl initialized, version: " + versionArray[0] + "." + versionArray[1]);
        }
        if (EglConfig == null) {
            SimpleLogger.d(getClass(), "egl config is null, creating.");

            int[] eglConfigAttribList = createEGLConfigAttribs(surfaceConfig);
            int[] numEglConfigs = new int[1];
            EGLConfig[] eglConfigs = new EGLConfig[1];
            if (!EGL14.eglChooseConfig(EglDisplay, eglConfigAttribList, 0,
                    eglConfigs, 0, eglConfigs.length, numEglConfigs, 0)) {
                throw new IllegalArgumentException("Could not choose egl config.");
            }
            EglConfig = eglConfigs[0];
            surfaceConfig = EGL14Utils.getSurfaceConfig(EglDisplay, EglConfig);
            SimpleLogger.d(getClass(), "Selected EGL Configuration:");
            SimpleLogger.d(getClass(), surfaceConfig.toString());
        }
        if (EGLContext == null) {
            SimpleLogger.d(getClass(), "egl context is null, creating.");
            createEGLContext();
        }
    }

    protected int[] createDefaultConfigAttribs() {
        return new int[] {
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE
        };
    }

    protected void createEglSurface() {
        if (EGLSurface == null) {
            if (surfaceAttribs == null) {
                surfaceAttribs = new int[] { EGL14.EGL_NONE };
            }
            // turn our SurfaceControl into a Surface
            EGLSurface = EGL14.eglCreateWindowSurface(EglDisplay, EglConfig, surface,
                    surfaceAttribs, 0);
            if (EGLSurface == null) {
                SimpleLogger.d(getClass(), "Could not create window surface");
                throw new IllegalArgumentException("Could not create egl surface.");
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SimpleLogger.d(getClass(), "surfaceChanged() " + width + ", " + height);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SimpleLogger.d(getClass(), "surfaceCreated() ");
        surface = holder.getSurface();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        } else {
            if (EGLSurface == null) {
                createEglSurface();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (nucleusActivity == null) {
            return true;
        }
        nucleusActivity.handleTouch(event);
        return true;
    }

    protected void createEGL() {
        boolean created = EGLContext == null;
        createEglContext();
        createEglSurface();
        makeCurrent();
        SimpleLogger.d(getClass(), "EGL created and made current");
        EGL14.eglSwapInterval(EglDisplay, eglSwapInterval);
        if (env.isProperty(Environment.Property.FRONTBUFFERAUTO, false)
                && surfaceConfig.hasExtensionSupport(EGL14Constants.EGL_ANDROID_front_buffer_auto_refresh)) {
            EGL14.eglSurfaceAttrib(EglDisplay, EGLSurface, EGL14Constants.EGL_FRONT_BUFFER_AUTO_REFRESH_ANDROID, 1);
            SimpleLogger.d(getClass(),
                    "Set surfaceattrib for: " + EGL14Constants.EGL_ANDROID_front_buffer_auto_refresh);
        }
        if (created) {
            nucleusActivity.onSurfaceCreated(getWidth(), getHeight());
            EGL14.eglSwapBuffers(EglDisplay, EGLSurface);
            nucleusActivity.contextCreated(getWidth(), getHeight());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        SimpleLogger.d(getClass(), "surfaceDestroyed()");
        surface = null;
        EGLSurface = null;
    }

    private void makeCurrent() {
        if (!EGL14.eglMakeCurrent(EglDisplay, EGLSurface, EGLSurface, EGLContext)) {
            throw new IllegalArgumentException("Could not make egl current");
        }
    }

    public void setRenderContextListener(RenderContextListener listener) {
        this.renderListener = listener;
    }

    /**
     * Sets the egl swap interval, if no EGLDisplay exists then nothing is done.
     * 
     * @param interval
     */
    public void setEGLSwapInterval(int interval) {
        if (EglDisplay != null) {
            SimpleLogger.d(getClass(),
                    "set EGLSwapInterval to " + interval + " : " + EGL14.eglSwapInterval(EglDisplay, interval));
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
        if (EglDisplay != null && EGLSurface != null) {
            SimpleLogger.d(getClass(), "set EGL surfaceattrib: " + attribute + " : " + value + " : "
                    + EGL14.eglSurfaceAttrib(EglDisplay, EGLSurface, attribute, value));
        } else {
            SimpleLogger.d(getClass(), "Could not set EGL surfaceattrib, display or surface is null");
        }
    }

    /**
     * Draws the current frame.
     */
    protected void drawFrame() {
        renderListener.drawFrame();
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Starting EGL surface thread");
        createEGL();
        while (surface != null) {
            drawFrame();
            if (EGLSurface != null) {
                swapBuffers();
            }
        }
        if (renderListener != null) {
            renderListener.surfaceLost();
        }
        SimpleLogger.d(getClass(), "Exiting surface thread");
        thread = null;
    }

    /**
     * Swapbuffers and syncronize
     */
    protected void swapBuffers() {
        Environment env = Environment.getInstance();
        long start = System.currentTimeMillis();
        EGL14.eglSwapBuffers(EglDisplay, EGLSurface);
        boolean eglWaitGL = env.isProperty(Environment.Property.EGLWAITGL, false);
        if (eglWaitGL) {
            EGL14.eglWaitGL();
        }
        FrameSampler.getInstance().addTag(FrameSampler.Samples.EGLSWAPBUFFERS.name() + "-WAITGL=" + eglWaitGL,
                start,
                System.currentTimeMillis(), FrameSampler.Samples.EGLSWAPBUFFERS.detail);
    }

}
