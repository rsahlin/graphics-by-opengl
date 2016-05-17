package com.nucleus;

import com.nucleus.actor.J2SELogicProcessor;
import com.nucleus.actor.LogicProcessor;
import com.nucleus.actor.LogicProcessorRunnable;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.NodeController;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewController;

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
     * The scene rootnode
     */
    protected RootNode rootNode;

    /**
     * Touch and pointer input
     */
    protected PointerInputProcessor inputProcessor = new PointerInputProcessor();

    Thread runnableThread;
    LogicProcessorRunnable logicRunnable;
    LogicProcessor logicProcessor;

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
        logicRunnable = new LogicProcessorRunnable(renderer, new J2SELogicProcessor());
        if (Runtime.getRuntime().availableProcessors() > 1) {
            System.out.println("Started extra process for logic processing, number of processors: "
                    + Runtime.getRuntime().availableProcessors());
            runnableThread = new Thread(logicRunnable);
        } else {
            System.out.println("Running everything on one thread.");
        }

    }

    /**
     * Returns the renderer, this should generally not be used.
     * 
     * @return
     */
    public NucleusRenderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the pointer input processor, this can be used to listen to low level MMI (pointer input) events.
     * 
     * @return
     */
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
        renderer.resizeWindow(0, 0, width, height);
    }

    /**
     * Main loop, call this method to produce one frame.
     * This method MUST be called from a thread that can access GL.
     * The normal case is to call it from window/surface that has onDraw/display callbacks.
     */
    public void drawFrame() {
        if (contextCreated) {
            contextCreated = false;
            Window w = Window.getInstance();
            renderer.GLContextCreated(w.getWidth(), w.getHeight());
        }
        if (!hasCalledCreated) {
            throw new IllegalArgumentException(NOT_CALLED_CREATECONTEXT);
        }
        try {
            if (runnableThread != null) {
                if (!runnableThread.isAlive()) {
                    runnableThread.start();
                } else {
                    synchronized (logicRunnable) {
                        logicRunnable.notify();
                    }
                }
            } else {
                // TODO make sure this calls the same code as LogicProcessorRunnable
                if (rootNode != null) {
                    logicProcessor.processNode(rootNode.getScene(), renderer.getFrameSampler().getDelta());
                }
            }
            renderer.beginFrame();
            if (rootNode != null) {
                renderer.render(rootNode);
            }
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
        renderer.endFrame();

    }

    /**
     * Sets the processor for actor objects, this will be used when actors are loaded to resolve symbols into
     * implementation classes.
     * 
     * @param logicProcessor
     */
    public void setLogicProcessor(LogicProcessor logicProcessor) {
        this.logicProcessor = logicProcessor;
    }

    /**
     * Sets the scene rootnode, this will update the root node in the logic runnable {@linkplain LogicProcessorRunnable}
     * 
     * @param node
     */
    public void setRootNode(RootNode node) {
        this.rootNode = node;
        logicRunnable.setRootNode(node);
        ViewController vc = new ViewController();
        vc.registerEventHandler(null);
        NodeController nc = new NodeController(node);
        nc.registerEventHandler(null);

    }

    /**
     * Adds pointer input callback {@linkplain MMIEventListener} to the scene, after this call the Node tree will get
     * callbacks on pointer input
     * 
     * @param root
     */
    public void addPointerInput(RootNode root) {
        inputProcessor.addMMIListener(root.getScene());
    }

    /**
     * Removes the scene from pointer input callbacks {@linkplain MMIEventListener}, after this call the Node tree will
     * no longer get pointer input callbacks
     * 
     * @param root
     */
    public void removePointerInput(RootNode root) {
        inputProcessor.removeMMIListener(root.getScene());
    }

}
