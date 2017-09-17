package com.nucleus.component;

import com.nucleus.CoreApp;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;

/**
 * User for thread that drives the logic processing if done on cpu.
 * Can be started on a separate thread or run on main thread.
 *
 */
public class LogicProcessorRunnable implements Runnable {

    private static final String NULL_PARAMETER = "Parameter is null:";
    private RootNode rootNode;
    private NucleusRenderer renderer;
    private LogicProcessor logicProcessor;
    private boolean running = false;

    /**
     * Creates a new runnable that can be used to call processing of logic in a separate thread.
     * 
     */
    public LogicProcessorRunnable(NucleusRenderer renderer, LogicProcessor logicProcessor) {
        if (renderer == null || logicProcessor == null) {
            throw new IllegalArgumentException(NULL_PARAMETER + renderer + ", " + logicProcessor);
        }
        this.renderer = renderer;
        this.logicProcessor = logicProcessor;
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
        System.out.println("Started thread to call processNode()");
        running = true;
        while (running) {
            synchronized (this) {
            	process(FrameSampler.getInstance().getDelta());
                try {
                    wait();
                } catch (InterruptedException e) {

                }
            }
        }
        System.out.println("Exited call processNode() thread");
    }

    public void destroy() {
        running = false;
        Thread.currentThread().interrupt();
    }

    /**
     * Process the node specified by calling {@link #setRootNode(RootNode)}
     * Only needed to call this method if thread has not been creted for this class, normally called from the
     * {@link #run()} method.
     * 
     * @param delta
     */
    public void process(float delta) {
        if (rootNode != null) {
        	for (Node node : rootNode.getScene()) {
                logicProcessor.processNode(node, delta);
        	}
        }
    }

}
