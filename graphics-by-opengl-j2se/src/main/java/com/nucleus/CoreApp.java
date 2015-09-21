package com.nucleus;

import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.ProcessFrameRunnable;

/**
 * Base application, use this to get the objects needed to start and run an application.
 * Used by JOGL and Android implementations to share objects that are platform agnostic.
 * 
 * @author Richard Sahlin
 *
 */
public class CoreApp {

    private final static String NOT_CALLED_CREATECONTEXT = "Must call contextCreated() before rendering.";

    /**
     * Interface for the core app to create the objects needed.
     * Used by the internal implementation and not by clients.
     * 
     * @author Richard Sahlin
     *
     */
    public interface CoreAppStarter {
        /**
         * Creates window if necessary, the gles wrapper and core application using NucleusRenderer with the specified
         * GLES version.
         * Note, this method will only create the underlying renderer of the correct version, it is not guaranteed
         * that the GL context is created - do not perform any rendering until the {@link CoreApp#contextCreated} method
         * has been called.
         * 
         * @param version Version of GLES to use.
         */
        public void createCore(Renderers version);
    }

    /**
     * Implement this interface in client applications that are using {@link CoreApp} This interface is intended for the
     * J2SE (platform agnostic) version of projects.
     * 
     * 
     * @author Richard Sahlin
     *
     */
    public interface ClientApplication {

        /**
         * Initializes the client application with the {@link CoreApp} Implementations shall do necessary init in this
         * method.
         * 
         * @param coreApp
         * @throws IllegalArgumentException If coreApp is null
         */
        public void init(CoreApp coreApp);

    }

    /**
     * The renderer
     */
    protected NucleusRenderer renderer;
    /**
     * Touch and pointer input
     */
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();

    Thread runnableThread;
    ProcessFrameRunnable frameRunnable;

    /**
     * Set to true to trigger a call to context created next time {@link #drawFrame()} is called
     */
    private volatile boolean contextCreated = false;
    /**
     * Set to true when {@link #contextCreated(int, int)} is called
     */
    private volatile boolean hasCalledCreated = false;
    private volatile int width = -1, height = -1;

    /**
     * Creates a new Core application with the specified renderer.
     * Call {@link #drawFrame()} to produce frames and call attached {@link FrameListener}
     * 
     * @param renderer
     */
    public CoreApp(NucleusRenderer renderer) {
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

    public NucleusRenderer getRenderer() {
        return renderer;
    }

    public PointerInputProcessor getInputProcessor() {
        return inputProcessor;
    }

    /**
     * Call this to signal that GL context was created, next time {@link #drawFrame()} is called then the registered
     * {@link RenderContextListener} are called.
     * Must be called before {@link #drawFrame()} is called.
     * 
     * @param width Width of gl surface
     * @param height Height of gl surface
     */
    public void contextCreated(int width, int height) {
        hasCalledCreated = true;
        contextCreated = true;
        this.width = width;
        this.height = height;
    }

    /**
     * Main loop, call this method to produce one frame. This will call registered {@link FrameListener} so that
     * logic can be updated together with the new frame.
     * This method MUST be called from a thread that can access GL.
     * The normal case is to call it from window/surface that has onDraw/display callbacks.
     */
    public void drawFrame() {
        if (contextCreated) {
            contextCreated = false;
            renderer.getViewFrustum().setViewPort(0, 0, width, height);
            renderer.GLContextCreated(width, height);
        }
        if (!hasCalledCreated) {
            throw new IllegalArgumentException(NOT_CALLED_CREATECONTEXT);
        }
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

}
