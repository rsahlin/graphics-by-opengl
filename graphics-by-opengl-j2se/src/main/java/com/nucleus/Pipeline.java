package com.nucleus;

/**
 * Programmable stages of processing for the processor(s),
 * This encapsulates the objects needed to perform different types of programmable processing.
 * Main stages are graphics and compute
 *
 */
public abstract class Pipeline {

    public abstract void enableShader(Shader shader) throws BackendException;

}
