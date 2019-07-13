package com.nucleus.shader;

import java.nio.FloatBuffer;

import com.nucleus.environment.Lights;

/**
 * Shared class for shaderprograms - regardless of platform implementation (GL/Vulkan)
 *
 */
public abstract class ShaderProgram implements Shader {

    /**
     * The basic function
     */
    protected Categorizer function;

    protected Lights globalLight = Lights.getInstance();

    /**
     * Uniforms, used when rendering - uniforms array shall belong to program since uniforms are a property of the
     * program. This data is quite small and the size depends on what program is used - and not the mesh.
     * The same mesh may be rendered with different programs, for instance different shadow passes and will have
     * different number of uniforms depending on the program.
     * 
     */
    transient protected FloatBuffer uniforms;

    protected ShaderVariable modelUniform;

    @Override
    public FloatBuffer getUniformData() {
        return uniforms;
    }

    /**
     * Sets the float values from data at the offset from variable, use this to set more than one value.
     * 
     * @param variable The shader variable to set uniform data to
     * @param data The uniform data to set
     * @param sourceOffset Offset into data where values are read
     */
    public void setUniformData(ShaderVariable variable, float[] data, int sourceOffset) {
        uniforms.position(variable.getOffset());
        uniforms.put(data, sourceOffset, variable.getSizeInFloats());
    }

}
