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

    protected String[] source;

    /**
     * Creates a shader program that will load shaders from default location
     * 
     * @param source Use as category for the shader types, must shader types in Shader. Eg if
     * {@link ProgramType#VERTEX_FRAGMENT} then this must contain 2 values.
     * @param shaders
     */
    public GenericShaderProgram(String[] source, ProgramType shaders) {
        super(null, null, null, null, shaders);
        this.source = source;
    }

    @Override
    protected String getShaderSource(int shaderType) {
        ShaderType t = ShaderType.getFromType(shaderType);
        return source[t.index];
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
