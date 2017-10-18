package com.nucleus.component;

import com.nucleus.CoreApp;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer;
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
    Thread runnableThread;
    private LogicProcessor logicProcessor;
    private boolean running = false;

    /**
     * Creates a new runnable that can be used to call processing of logic in a separate thread.
     * 
     */
    public LogicProcessorRunnable(NucleusRenderer renderer, LogicProcessor logicProcessor, boolean enableMultiThread) {
        if (renderer == null || logicProcessor == null) {
            throw new IllegalArgumentException(NULL_PARAMETER + renderer + ", " + logicProcessor);
        }
        this.renderer = renderer;
        this.logicProcessor = logicProcessor;
        //TODO Create thread manager that handles available thread, lease thread instead of creating here
        if (Runtime.getRuntime().availableProcessors() > 1 && enableMultiThread) {
            System.out.println("Started extra process for logic processing, number of processors: "
                    + Runtime.getRuntime().availableProcessors());
            runnableThread = new Thread(this);
        } else {
            System.out.println("Running everything on one thread.");
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
        System.out.println("Started thread to call processNode()");
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
        System.out.println("Exited call processNode() thread");
    }

    public void destroy() {
        running = false;
        Thread.currentThread().interrupt();
    }

    private void internalProcessNode(RootNode rootNode, float delta) {
        if (rootNode != null) {
            long start = System.currentTimeMillis();
            logicProcessor.processRoot(rootNode, delta);
            FrameSampler.getInstance().addTag(FrameSampler.LOGICPROCESSOR, start, System.currentTimeMillis());
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
            internalProcessNode(rootNode,FrameSampler.getInstance().getDelta());
        }
    }
}
