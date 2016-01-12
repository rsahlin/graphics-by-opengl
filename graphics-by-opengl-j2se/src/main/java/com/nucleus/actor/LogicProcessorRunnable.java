package com.nucleus.actor;

import com.nucleus.renderer.NucleusRenderer;

public class LogicProcessorRunnable implements Runnable {

    private static final String NULL_PARAMETER = "Parameter is null:";
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

    @Override
    public void run() {
        System.out.println("Started thread to call processNode()");
        running = true;
        while (running) {
            synchronized (this) {
                logicProcessor.processNode(renderer.getScene(), renderer.getFrameSampler().getDelta());
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

}
