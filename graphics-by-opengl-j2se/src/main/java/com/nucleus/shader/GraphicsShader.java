package com.nucleus.shader;

import java.nio.FloatBuffer;

public interface GraphicsShader extends Shader {

    /**
     * Initializes the uniform data for this program.
     * Is called after program is linked and uniform buffers are created, variables and blocks are resolved.
     * 
     * @param destinationUniforms
     */
    public abstract void initUniformData(FloatBuffer destinationUniforms);

    /**
     * Updates the shader program specific uniform data, storing in in the uniformData array or
     * {@link #uniformBlockBuffers}
     * Subclasses shall set any uniform data needed - but not matrices which is set in
     * {@link #setUniformMatrices(float[][])}
     * 
     * @param destinationUniforms
     */
    public abstract void updateUniformData(FloatBuffer destinationUniform);

}
