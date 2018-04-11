package com.nucleus.android;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import com.nucleus.SimpleLogger;
import com.nucleus.android.egl10.EGL10Utils;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer.FrameRenderer;
import com.nucleus.renderer.SurfaceConfiguration;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;

@SuppressLint("ClickableViewAccessibility")
public class AndroidSurfaceView extends GLSurfaceView
        implements GLSurfaceView.EGLConfigChooser, Renderer, EGLWindowSurfaceFactory {

    /**
     * Set to true to exit from onDrawFrame directly - for instance when an error has occured.
     */
    protected volatile boolean noUpdates = false;

    private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098; // EGL 1.3 to set client version

    private EGL10 egl;
    private EGLDisplay eglDisplay;
    private EGLConfig eglConfig;
    private EGLSurface eglSurface;
    private boolean surfaceDestroyed = false;
    private long lastDraw;
    private NucleusActivity nucleusActivity;
    protected FrameRenderer frameRenderer;

    /**
     * The result surface configuration from EGL
     */
    private SurfaceConfiguration surfaceConfig;

    /**
     * The wanted surface configuration
     */
    private SurfaceConfiguration wantedConfig;

    /**
     * Creates a new surface view for GL, this class will be used for Renderer, will choose EGL config based on
     * configuration
     * 
     * @param wantedConfig The wanted surface config
     * @param version Renderer version
     * @param nucleusActivity
     * @param coreAppStarter
     * @throws IllegalArgumentException If clientClass is null
     */
    public AndroidSurfaceView(SurfaceConfiguration wantedConfig, Renderers version, NucleusActivity nucleusActivity) {
        super(nucleusActivity);
        this.nucleusActivity = nucleusActivity;
        setEGLWindowSurfaceFactory(this);
        this.wantedConfig = wantedConfig;
        setEGLContextClientVersion(version.major);
        setEGLConfigChooser(this);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (nucleusActivity == null) {
            return true;
        }
        nucleusActivity.handleTouch(event);
        return true;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        SimpleLogger.d(getClass(), "chooseConfig()");
        this.egl = egl;
        this.eglDisplay = display;
        eglConfig = EGL10Utils.selectConfig(egl, display, wantedConfig);
        if (eglConfig == null) {
            throw new IllegalArgumentException("No EGL config matching default surface configuration.");
        }
        surfaceDestroyed = false;
        surfaceConfig = new SurfaceConfiguration();
        EGL10Utils.readSurfaceConfig(egl, display, eglConfig, surfaceConfig);
        EGL10Utils.setEGLInfo(egl, display, surfaceConfig);
        SimpleLogger.d(getClass(), "chooseConfig() has: " + surfaceConfig.toString());
        return eglConfig;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        SimpleLogger.d(getClass(), "surfaceChanged() " + width + ", " + height);
    }

    protected void handleThrowable(Throwable t) {
        noUpdates = true;
        NucleusActivity.handleThrowable(t);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (noUpdates) {
            return;
        }
        try {
            frameRenderer.renderFrame();
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        SimpleLogger.d(getClass(), "surfaceCreated() " + getWidth() + ", " + getHeight());
        nucleusActivity.onSurfaceCreated(getWidth(), getHeight());
        egl.eglSwapBuffers(eglDisplay, eglSurface);
        checkEGLError("eglSwapBuffers()");
        nucleusActivity.contextCreated(getWidth(), getHeight());
    }

    @Override
    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
            EGLConfig config, Object nativeWindow) {
        SimpleLogger.d(getClass(), "createWindowSurface()");
        eglSurface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        checkEGLError("eglCreateWindowSurface()");
        return eglSurface;
    }

    private void checkEGLError(String tag) {
        int error = egl.eglGetError();
        if (error != EGL11.EGL_SUCCESS) {
            throw new IllegalArgumentException(tag + " EGL error " + error);
        }
    }

    @Override
    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
        SimpleLogger.d(getClass(), "destroySurface()");
        surfaceDestroyed = true;
        if (surface != null && display != null) {
            egl.eglDestroySurface(display, surface);
            eglSurface = null;
            eglDisplay = null;
        }
        if (frameRenderer != null) {
            frameRenderer.surfaceLost();
        }
    }

    public void setRenderContextListener(FrameRenderer frameRenderer) {
        this.frameRenderer = frameRenderer;
    }

}