package com.nucleus.android;

import com.nucleus.SimpleLogger;
import com.nucleus.android.egl14.EGL14Utils;
import com.nucleus.egl.EGL14Constants;
import com.nucleus.egl.EGLUtils;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.SurfaceConfiguration;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
    protected boolean waitForClient = false;

    public EGLSurfaceView(SurfaceConfiguration surfaceConfig, Renderers version, NucleusActivity nucleusActivity) {
        super(nucleusActivity);
        this.nucleusActivity = nucleusActivity;
        this.surfaceConfig = surfaceConfig;
        this.version = version;
        getHolder().addCallback(this);
    }

    protected boolean createEglContext() {
        if (EglDisplay == null) {
            EglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (EglDisplay == EGL14.EGL_NO_DISPLAY) {
                SimpleLogger.d(getClass(), "Could not create egl display");
                return false;
            }

            int[] version = new int[2];
            if (!EGL14.eglInitialize(EglDisplay, version, 0, version, 1)) {
                EglDisplay = null;
                SimpleLogger.d(getClass(), "Could not initialize egl display");
                return false;
            }
        }
        int[] eglConfigAttribList = null;
        if (surfaceConfig != null) {
            eglConfigAttribList = EGLUtils.createConfig(surfaceConfig);
        } else {
            // Create default.
            eglConfigAttribList = createDefaultConfigAttribs();
        }

        int[] numEglConfigs = new int[1];
        EGLConfig[] eglConfigs = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(EglDisplay, eglConfigAttribList, 0,
                eglConfigs, 0, eglConfigs.length, numEglConfigs, 0)) {
            SimpleLogger.d(getClass(), "Could not choose egl config");
            return false;
        }
        EglConfig = eglConfigs[0];
        surfaceConfig = EGL14Utils.getSurfaceConfig(EglDisplay, EglConfig);
        SimpleLogger.d(getClass(), "Selected EGL Configuration:");
        SimpleLogger.d(getClass(), surfaceConfig.toString());

        int[] eglContextAttribList = new int[] {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        EGLContext = EGL14.eglCreateContext(EglDisplay, EglConfig,
                EGL14.EGL_NO_CONTEXT, eglContextAttribList, 0);
        if (EGLContext == null) {
            SimpleLogger.d(getClass(), "Could not create EGL context");
            return false;
        }
        return true;
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

    protected boolean createEglSurface() {
        if (EGLSurface == null) {
            int[] eglSurfaceAttribList = new int[] {
                    EGL14.EGL_NONE
            };
            // turn our SurfaceControl into a Surface
            EGLSurface = EGL14.eglCreateWindowSurface(EglDisplay, EglConfig, surface,
                    eglSurfaceAttribList, 0);
            if (EGLSurface == null) {
                SimpleLogger.d(getClass(), "Could not create window surface");
                return false;
            }
        }
        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SimpleLogger.d(getClass(), "surfaceChanged() " + width + ", " + height);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SimpleLogger.d(getClass(), "surfaceCreated() ");
        surface = holder.getSurface();
        thread = new Thread(this);
        thread.start();
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
        if (!createEglContext()) {
            throw new IllegalArgumentException("Could not create EGL context");
        }
        if (!createEglSurface()) {
            throw new IllegalArgumentException("Could not create EGL surface");
        }
        makeCurrent();
        SimpleLogger.d(getClass(), "EGL created and made current");
        EGL14.eglSurfaceAttrib(EglDisplay, EGLSurface, EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_DESTROYED);
        if (surfaceConfig.hasExtensionSupport(EGL14Constants.EGL_ANDROID_front_buffer_auto_refresh)) {
            // EGL14.eglSurfaceAttrib(EglDisplay, EGLSurface, EGL14Constants.EGL_FRONT_BUFFER_AUTO_REFRESH_ANDROID, 1);
            // SimpleLogger.d(getClass(), "Set attrib for: " + EGL14Constants.EGL_ANDROID_front_buffer_auto_refresh);
        }
        nucleusActivity.onSurfaceCreated(getWidth(), getHeight());
        EGL14.eglSwapBuffers(EglDisplay, EGLSurface);
        nucleusActivity.contextCreated(getWidth(), getHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        SimpleLogger.d(getClass(), "surfaceDestroyed()");
        surface = null;
        EGLSurface = null;
        if (renderListener != null) {
            renderListener.surfaceLost();
        }
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
     * Specify if a call to eglWaitClient() is made after eglSwapBuffers
     * 
     * @param waitClient True to call eglWaitClient() efter swapbuffers
     */
    public void setWaitClient(boolean waitClient) {
        this.waitForClient = waitClient;
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Starting EGL surface thread");
        createEGL();
        while (surface != null) {
            renderListener.drawFrame();
            if (EGLSurface != null) {
                EGL14.eglSwapBuffers(EglDisplay, EGLSurface);
                if (waitForClient) {
                    EGL14.eglWaitClient();
                }
            }
        }
        SimpleLogger.d(getClass(), "Exiting surface thread");
    }

}
