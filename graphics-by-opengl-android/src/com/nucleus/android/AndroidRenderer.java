package com.nucleus.android;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.nucleus.CoreApp;

import android.opengl.GLSurfaceView.Renderer;

/**
 * Base implementation for Android renderer used with GLSurfaceView
 * This will handle the most common situations for rendering and reading input events.
 * 
 * @author Richard Sahlin
 */
public class AndroidRenderer implements Renderer {

    public final static String ANDROID_RENDERER_TAG = "AndroidRenderer";
    private final static String NULL_CORE_RENDERER_ERROR = "Core application renderer is null";
    CoreApp coreApp;
    /**
     * Set to true to exit from onDrawFrame directly - for instance when an error has occured.
     */
    private volatile boolean noUpdates = false;

    /**
     * Creates a new Android renderer using the specified BaseRenderer implementation
     * 
     * @param renderer
     * @param appListener Call when each frame shall be drawn.
     * @throws IllegalArgumentException If renderer is null.
     */
    AndroidRenderer(CoreApp coreApp) {
        if (coreApp == null) {
            throw new IllegalArgumentException(NULL_CORE_RENDERER_ERROR);
        }
        this.coreApp = coreApp;

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        try {
            coreApp.contextCreated(width, height);
        } catch (Throwable t) {
            handleThrowable(t);
        }
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
        try {
            coreApp.getRenderer().init(null);
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

}
