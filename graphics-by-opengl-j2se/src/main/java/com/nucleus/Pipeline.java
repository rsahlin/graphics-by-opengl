package com.nucleus;

import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Primitive;

/**
 * Programmable stages of processing for the processor(s),
 * This encapsulates the objects needed to perform different types of programmable processing.
 * Main stages are graphics and compute
 *
 */
public abstract class Pipeline {

    /**
     * Enable this pipeline, any processing commands issued after this call will use this pipeline
     * 
     * @param renderer
     * @throws BackendException
     */
    public abstract void enable(NucleusRenderer renderer) throws BackendException;

    /**
     * Updates the pipeline stages, uniforms and attributes for the Mesh to be rendered
     * Call this prior to rendering the Mesh
     * 
     * @param renderer
     * @param mesh The mesh to be rendered
     * @param matrices
     * @throws BackendException
     */
    public abstract void update(NucleusRenderer renderer, Mesh mesh, float[][] matrices) throws BackendException;

    /**
     * Updates the pipeline stages, uniforms and attributes for the Mesh to be rendered
     * Call this prior to rendering the Primitive
     * 
     * @param renderer
     * @param gltf
     * @param primitive The primitive to be rendered
     * @param matrices
     * @throws BackendException
     */
    public abstract void update(NucleusRenderer renderer, GLTF gltf, Primitive primitive, float[][] matrices)
            throws BackendException;

    /**
     * Destroys the shaders and program used for the pipeline - this shall release the pipeline related resources
     * 
     * @param renderer
     */
    public abstract void destroy(NucleusRenderer renderer);

}
