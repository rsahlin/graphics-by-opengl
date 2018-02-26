package com.nucleus.component;

import com.nucleus.CoreApp;
import com.nucleus.SimpleLogger;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RootNode;

/**
 * User for thread that drives the component processing if done on cpu.
 * Can be started on a separate thread or run on main thread.
 *
 */
public class ComponentProcessorRunnable implements Runnable {

    private static final String NULL_PARAMETER = "Parameter is null:";
    private RootNode rootNode;
    private NucleusRenderer renderer;
    Thread runnableThread;
    private ComponentProcessor componentProcessor;
    private boolean running = false;

    /**
     * Creates a new runnable that can be used to call component (logic) processing in a separate thread.
     * 
     */
    public ComponentProcessorRunnable(NucleusRenderer renderer, ComponentProcessor componentProcessor,
            boolean enableMultiThread) {
        if (renderer == null || componentProcessor == null) {
            throw new IllegalArgumentException(NULL_PARAMETER + renderer + ", " + componentProcessor);
        }
        this.renderer = renderer;
        this.componentProcessor = componentProcessor;
        // TODO Create thread manager that handles available thread, lease thread instead of creating here
        if (Runtime.getRuntime().availableProcessors() > 1 && enableMultiThread) {
            SimpleLogger.d(getClass(), "Started extra process for component processing, number of processors: "
                    + Runtime.getRuntime().availableProcessors());
            runnableThread = new Thread(this);
        } else {
            SimpleLogger.d(getClass(), "Running everything on one thread.");
        }

    }

    /**
     * Sets the root node for the logic processor
     * Call this from {@linkplain CoreApp} when the root node is set.
     * 
     * @param root
     */
    public void setRootNode(RootNode root) {
        this.rootNode = root;
    }

    @Override
    public void run() {
        SimpleLogger.d(getClass(), "Started thread to call processNode()");
        running = true;
        while (running) {
            synchronized (this) {
                internalProcessNode(rootNode, FrameSampler.getInstance().getDelta());
                try {
                    wait();
                } catch (InterruptedException e) {

                }
            }
        }
        SimpleLogger.d(getClass(), "Exited call processNode() thread");
    }

    public void destroy() {
        running = false;
        Thread.currentThread().interrupt();
    }

    private void internalProcessNode(RootNode rootNode, float delta) {
        if (rootNode != null) {
            long start = System.currentTimeMillis();
            componentProcessor.processRoot(rootNode, delta);
            FrameSampler.getInstance().addTag(FrameSampler.Samples.COMPONENTPROCESSOR, start,
                    System.currentTimeMillis());
        }
    }

    /**
     * Process the root node, updating behavior
     * 
     * @param rootNode
     * @param delta
     */
    public void process(RootNode rootNode, float delta) {
        this.rootNode = rootNode;
        if (runnableThread != null) {

            if (!runnableThread.isAlive()) {
                runnableThread.start();
            } else {
                synchronized (this) {
                    notify();
                }
            }
        } else {
            internalProcessNode(rootNode, FrameSampler.getInstance().getDelta());
        }
    }
}
