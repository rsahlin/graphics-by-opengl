package com.nucleus;

import com.nucleus.assets.Assets;
import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.shader.Shader;
import com.nucleus.shader.Shader.Categorizer;
import com.nucleus.shader.Shader.ShaderType;
import com.nucleus.shader.ShaderBinary;

/**
 * Programmable stages of processing for the processor(s),
 * This encapsulates the objects needed to perform different types of programmable processing, shader, (framebuffer)
 * targets and blend modes.
 * Main stages are graphics and compute.
 * Remember that Vulkan pipelines are mostly immutable, if shader, targets or blend mode needs to change then the
 * pipeline needs to be re-created.
 * 
 * @param <T> The shader class that this pipeline will use, this depends on what shaders this pipeline will use.
 * @param <S> Shader binary/source object that this pipeline will use
 *
 */
public interface Pipeline<T extends Shader, S extends ShaderBinary> {

    /**
     * Internal method - do not use directly - call {@link Assets#getGraphicsPipeline(NucleusRenderer, Shader)} instead
     * 
     * Compile and links the pipeline.
     * This method shall only be called once, it is an error to re-compile an already compiled pipeline.
     * 
     * @param renderer
     * @param shader
     * @throws BackendException If the pipeline already has been compiled or if there is an error compiling/linking
     */
    public void compile(NucleusRenderer renderer, T shader) throws BackendException;

    /**
     * Returns the name of the shader source for the specified type, this is used in the compile/linking
     * step
     * 
     * @param version Highest GL version that is supported, used to fetch versioned source name.
     * @param function
     * @param type The shader type to return source for
     * @return The shader source to be used when compiling/linking the shader
     */
    public S getShaderSource(Renderers version, Categorizer function, ShaderType type);

    /**
     * Returns the shader filename suffix to be used for the specified shader type.
     * 
     * @param type
     * @return
     */
    public String getFilenameSuffix(ShaderType type);

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
