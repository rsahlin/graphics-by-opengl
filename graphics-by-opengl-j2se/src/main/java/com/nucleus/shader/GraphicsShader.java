package com.nucleus.shader;

import java.nio.FloatBuffer;

import com.nucleus.GraphicsPipeline;
import com.nucleus.opengl.shader.NamedShaderVariable;

/**
 * Holds the buffers and methods needed for a specific graphics shader program.
 *
 */
public interface GraphicsShader extends Shader {

    /**
     * Creates buffer store for uniform data that fits this shader program.
     * 
     * @param floatSize Number of floats to allocate buffer for
     * @return The buffer to hold uniform data, uninitialized
     */
    public void createUniformBuffer(int floatSize);

    /**
     * Initializes the uniform data for this program - this shall init the buffer create in
     * {@link #createUniformBuffer(int)}
     * Is called after program is linked and uniform buffers are created, variables and blocks are resolved.
     * 
     */
    public void initUniformData();

    /**
     * Updates the shader program specific uniform data, this shall update the uniform buffer before rendering
     * the current frame.
     * 
     */
    public void updateUniformData();

    /**
     * 
     * Sets the data for the uniform matrices needed by the program - the default implementation will set the modelview
     * and projection matrices. Will NOT set uniforms to backend api, only update the uniform array store
     * 
     * @param matrices Source matrices to set to uniform data array.
     */
    public void setUniformMatrices(float[][] matrices);

    /**
     * Sets the float values from data at the offset from variable, use this to set more than one value.
     * 
     * @param variable The shader variable to set uniform data to
     * @param data The uniform data to set
     * @param sourceOffset Offset into data where values are read
     */
    public void setUniformData(NamedShaderVariable variable, float[] data, int sourceOffset);

    /**
     * Returns the uniform data, this shall be mapped to shader
     * 
     * @return The floatbuffer holding uniform data
     */
    @Override
    public FloatBuffer getUniformData();

    /**
     * Inits this shader to be used with the pipeline
     * 
     * @param pipeline
     */
    public void initShader(GraphicsPipeline<?> pipeline);

    /**
     * Returns the pipeline that has been set by calling {@link #initShader(GraphicsPipeline)}
     * 
     * @return
     */
    public GraphicsPipeline<?> getPipeline();

}
