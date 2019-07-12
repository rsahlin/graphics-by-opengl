package com.nucleus.shader;

import com.nucleus.BackendException;
import com.nucleus.renderer.NucleusRenderer;

/**
 * The resources needed for a programmable stage of the pipeline
 *
 */
public interface Shader {

    /**
     * Different type of shadings that needs to be supported in shaders
     *
     */
    public enum Shading {
    flat(),
    textured(),
    pbr(),
    shadow1(),
    shadow2();
    }

    /**
     * Create the programs for the shader program implementation.
     * This method must be called before the program is used, or the other methods are called.
     * How the program is cread depends on API backend (GL/Vulkan)
     * 
     * @param renderer The render backend to use when compiling and linking program.
     * @throws BackendException If program could not be compiled and linked, possibly due to IOException
     */
    public void createProgram(NucleusRenderer renderer) throws BackendException;

    /**
     * Returns the key value for this shader program, this is the classname and possible name of shader used.
     * The key shall be a unique (in this context) identifier for the shader - it does not need to be globally unique.
     * 
     * @return Key value for this shader program.
     */
    public String getKey();

    /**
     * If set then variable offsets, in the program ShaderVariables will be set from this indexer.
     * If null then offsets will set based on found variable sizes.
     * 
     * @param variableIndexer
     */
    public void setIndexer(VariableIndexer variableIndexer);

}
