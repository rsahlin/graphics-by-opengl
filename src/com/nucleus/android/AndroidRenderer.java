package com.nucleus.android;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;

import com.nucleus.renderer.NucleusRenderer;

/**
 * Base implementation for Android renderer used with GLSurfaceView
 * This will handle the most common situations for rendering and reading input events.
 * 
 * @author Richard Sahlin
 */
public class AndroidRenderer implements Renderer {

    public final static String ANDROID_RENDERER_TAG = "AndroidRenderer";
    private final static String NULL_RENDERER_ERROR = "NucleusRenderer is null";
    NucleusRenderer renderer;

    /**
     * Creates a new Android renderer using the specified BaseRenderer implementation
     * 
     * @param renderer
     * @param appListener Call when each frame shall be drawn.
     * @throws IllegalArgumentException If renderer is null.
     */
    public AndroidRenderer(NucleusRenderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException(NULL_RENDERER_ERROR);
        }
        this.renderer = renderer;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        renderer.getViewFrustum().setViewPort(0, 0, width, height);
        renderer.GLContextCreated(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float deltaTime = renderer.beginFrame();
        renderer.updateFrame(deltaTime);
        renderer.endFrame();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        renderer.init();
    }

}
