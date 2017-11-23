package com.nucleus.android;

import com.nucleus.CoreApp;
import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper.Renderers;
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

    public EGLSurfaceView(SurfaceConfiguration surfaceConfig, Renderers version, NucleusActivity nucleusActivity) {
        super(nucleusActivity);
        this.nucleusActivity = nucleusActivity;
        this.surfaceConfig = surfaceConfig;
        this.version = version;
        getHolder().addCallback(this);
    }

    NucleusActivity nucleusActivity;
    Renderers version;
    EGLContext EGLContext;
    EGLDisplay EglDisplay;
    EGLConfig EglConfig;
    EGLSurface EGLSurface;
    SurfaceConfiguration surfaceConfig;
    Surface surface;
    CoreApp coreApp;

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

        if (EglConfig == null) {
            int[] eglConfigAttribList = new int[] {
                    EGL14.EGL_RENDERABLE_TYPE,
                    EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_NONE
            };
            int[] numEglConfigs = new int[1];
            EGLConfig[] eglConfigs = new EGLConfig[1];
            if (!EGL14.eglChooseConfig(EglDisplay, eglConfigAttribList, 0,
                    eglConfigs, 0, eglConfigs.length, numEglConfigs, 0)) {
                SimpleLogger.d(getClass(), "Could not choose egl config");
                return false;
            }
            EglConfig = eglConfigs[0];
        }

        if (EGLContext == null) {
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
        }
        return true;
    }

    private boolean createEglSurface() {
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
        SimpleLogger.d(getClass(), "surfaceChanged()");

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SimpleLogger.d(getClass(), "surfaceCreated()");
        surface = holder.getSurface();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (coreApp == null) {
            return true;
        }
        nucleusActivity.handleTouch(event);
        return true;
    }

    private void createEGL() {
        if (!createEglContext()) {
            throw new IllegalArgumentException("Could not create EGL context");
        }
        if (!createEglSurface()) {
            throw new IllegalArgumentException("Could not create EGL surface");
        }
        makeCurrent();
        SimpleLogger.d(getClass(), "EGL created and made current");
        nucleusActivity.onSurfaceCreated(getWidth(), getHeight());
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

    public void setCoreApp(CoreApp coreApp) {
        this.coreApp = coreApp;
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Starting EGL surface thread");
        createEGL();
        while (surface != null) {
            coreApp.drawFrame();
            if (EGLSurface != null) {
                EGL14.eglSwapBuffers(EglDisplay, EGLSurface);
                EGL14.eglWaitGL();
            }
        }
        SimpleLogger.d(getClass(), "Exiting surface thread");
    }

}
