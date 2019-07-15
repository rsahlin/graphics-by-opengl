package com.nucleus;

import com.nucleus.assets.Assets;
import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.shader.Shader;

/**
 * Programmable stages of processing for the processor(s),
 * This encapsulates the objects needed to perform different types of programmable processing.
 * Main stages are graphics and compute
 *
 */
public interface Pipeline<T extends Shader> {

    /**
     * Internal method - do not use directly - call {@link Assets#getGraphicsPipeline(NucleusRenderer, Shader)} instead
     * 
     * Compile and links the pipeline to be used with the specified shader.
     * This method shall only be called once, it is an error to re-compile an already compiled pipeline.
     * 
     * @param renderer
     * @param shader
     * @throws BackendException If the pipeline already has been compiled or if there is an error compiling/linking
     */
    public void compile(NucleusRenderer renderer, T shader) throws BackendException;

    /**
     * Enable this pipeline, any processing commands issued after this call will use this pipeline
     * 
     * @param renderer
     * @throws BackendException
     */
    public void enable(NucleusRenderer renderer) throws BackendException;

    /**
     * Updates the pipeline stages, uniforms and attributes for the Mesh to be rendered
     * Call this prior to rendering the Mesh
     * 
     * @param renderer
     * @param mesh The mesh to be rendered
     * @param matrices
     * @throws BackendException
     */
    public void update(NucleusRenderer renderer, Mesh mesh, float[][] matrices) throws BackendException;

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
    public void update(NucleusRenderer renderer, GLTF gltf, Primitive primitive, float[][] matrices)
            throws BackendException;

    /**
     * Destroys the shaders and program used for the pipeline - this shall release the pipeline related resources
     * 
     * @param renderer
     */
    public void destroy(NucleusRenderer renderer);

}
