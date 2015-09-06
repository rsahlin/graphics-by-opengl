package com.nucleus.android;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;

import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.ProcessFrameRunnable;

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
    ProcessFrameRunnable frameRunnable;
    Thread runnableThread;

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
        frameRunnable = new ProcessFrameRunnable(renderer);
        if (Runtime.getRuntime().availableProcessors() > 1) {
            System.out.println("Started extra process for logic processing, number of processors: "
                    + Runtime.getRuntime().availableProcessors());
            runnableThread = new Thread(frameRunnable);
        } else {
            System.out.println("Running everything on one thread.");
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        renderer.getViewFrustum().setViewPort(0, 0, width, height);
        renderer.GLContextCreated(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        renderer.beginFrame();
        try {
            if (runnableThread != null) {
                if (!runnableThread.isAlive()) {
                    runnableThread.start();
                } else {
                    synchronized (frameRunnable) {
                        frameRunnable.notify();
                    }
                }
            } else {
                renderer.processFrame();
            }
            renderer.renderScene();
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
        renderer.endFrame();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        renderer.init();
    }

}
