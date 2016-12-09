package com.nucleus.android;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import com.nucleus.CoreApp;
import com.nucleus.SimpleLogger;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.SurfaceConfiguration;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;

public class AndroidSurfaceView extends GLSurfaceView
        implements GLSurfaceView.EGLConfigChooser, Renderer, EGLWindowSurfaceFactory {

    private final static String NULL_CORE_RENDERER_ERROR = "Core application renderer is null";
    CoreApp coreApp;
    /**
     * Set to true to exit from onDrawFrame directly - for instance when an error has occured.
     */
    private volatile boolean noUpdates = false;

    private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098; // EGL 1.3 to set client version


    private EGL10 egl;
    private EGLDisplay display;
    private EGLConfig eglConfig;
    private EGLSurface eglSurface;

    PointerInputProcessor inputProcessor;
    /**
     * The result surface configuration from EGL
     */
    private SurfaceConfiguration surfaceConfig;

    /**
     * The wanted surface configuration
     */
    private SurfaceConfiguration wantedConfig = new SurfaceConfiguration();

    /**
     * Creates a new surface view for GL, this class will be used for Renderer, will choose EGL config based on
     * configuration
     * 
     * @param coreApp
     * @param wantedConfig The wanted surface config
     * @param context
     * @param renderer
     * @param inputProcessor
     */
    public AndroidSurfaceView(CoreApp coreApp, SurfaceConfiguration wantedConfig, Context context,
            PointerInputProcessor inputProcessor) {
        super(context);
        setEGLWindowSurfaceFactory(this);
        if (coreApp == null) {
            throw new IllegalArgumentException(NULL_CORE_RENDERER_ERROR);
        }
        this.coreApp = coreApp;
        this.wantedConfig = wantedConfig;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(this);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        this.inputProcessor = inputProcessor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int count = event.getPointerCount();
        for (int i = 0; i < count; i++) {
            int finger = event.getPointerId(i);
            int actionFinger = event.getActionIndex();
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                // Recording down for multi touch - all pointers will be re-sent when a new finger goes down.
                inputProcessor.pointerEvent(PointerAction.DOWN, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_DOWN:
                inputProcessor.pointerEvent(PointerAction.DOWN, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (finger == 0) {
                    inputProcessor.pointerEvent(PointerAction.UP, event.getEventTime(), actionFinger, new float[] {
                            event.getX(i), event.getY(i) });
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                inputProcessor.pointerEvent(PointerAction.UP, event.getEventTime(), finger, new float[] {
                        event.getX(i), event.getY(i) });
                break;
            case MotionEvent.ACTION_MOVE:
                inputProcessor.pointerEvent(PointerAction.MOVE, event.getEventTime(), finger,
                        new float[] { event.getX(i), event.getY(i) });
                break;
            default:
            }
        }
        requestRender();
        return true;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        this.egl = egl;
        this.display = display;
        eglConfig = EGLUtils.selectConfig(egl, display, wantedConfig);
        if (eglConfig == null) {
            throw new IllegalArgumentException("No EGL config matching default surface configuration.");
        }
        surfaceConfig = new SurfaceConfiguration();
        EGLUtils.readSurfaceConfig(egl, display, eglConfig, surfaceConfig);
        SimpleLogger.d(getClass(), "chooseConfig() has: " + surfaceConfig.toString());

        return eglConfig;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        SimpleLogger.d(getClass(), "surfaceChanged() " + width + ", " + height);
        coreApp.contextCreated(width, height);
    }

    private void handleThrowable(Throwable t) {
        noUpdates = true;
        NucleusActivity.handleThrowable(t);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (noUpdates) {
            return;
        }
        try {
            coreApp.drawFrame();

        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        SimpleLogger.d(getClass(), "surfaceCreated() " + getWidth() + ", " + getHeight());
        try {
            if (!coreApp.getRenderer().isInitialized()) {
                coreApp.getRenderer().init(surfaceConfig, getWidth(), getHeight());
                try {
                    coreApp.displaySplash();
                } catch (GLException e) {
                    throw new RuntimeException(e);
                }
                egl.eglSwapBuffers(display, eglSurface);
                checkEGLError("eglSwapBuffers()");
            }
            if (surfaceConfig == null) {
                surfaceConfig = new SurfaceConfiguration();
                EGLUtils.readSurfaceConfig(egl, display, eglConfig, surfaceConfig);
                SimpleLogger.d(getClass(),
                        "onSurfaceCreated(EGLConfig) has: " + surfaceConfig.toString());
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
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
        if (eglSurface != null && display != null) {
            egl.eglDestroySurface(display, eglSurface);
            eglSurface = null;
        }
    }
}