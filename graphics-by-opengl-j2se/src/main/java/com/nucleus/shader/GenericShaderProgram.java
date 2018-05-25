package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Generic shader program - use this when a specific shader source shall be specified.
 * Loads from /assets folder and appends 'vertex', 'fragment', 'geometry' after source name.
 *
 */
public class GenericShaderProgram extends ShaderProgram {

    /**
     * Creates a shader program that will load shaders from default location
     * 
     * @param source
     * @param mapping Variable mapping to specify separate buffers or null.
     * @param shaders
     */
    public GenericShaderProgram(String source, VariableMapping[] mapping, Shaders shaders) {
        super(null, null, source, mapping, shaders);
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        return this;
    }

    @Override
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {
    }

    @Override
    public void initBuffers(Mesh mesh) {
    }

}
