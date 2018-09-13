package com.nucleus.shader;

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
     * @param source Source names for shaders, must match number of shader types in Shader. Eg if
     * @param shading
     * @param category
     * @param shaders
     * {@link ProgramType#VERTEX_FRAGMENT} then this must contain 2 values.
     * @param shaders
     */
    public GenericShaderProgram(String[] source, Pass pass, Shading shading, String category, ProgramType shaders) {
        super(pass, shading, category, shaders);
        this.source = source;
    }

    public GenericShaderProgram(Pass pass, Shading shading, String category, ProgramType shaders) {
        super(pass, shading, category, shaders);
    }

    public GenericShaderProgram(Categorizer function, ProgramType shaders) {
        super(function, shaders);
    }

    @Override
    protected String getShaderSourceName(int shaderType) {
        if (source == null) {
            return super.getShaderSourceName(shaderType);
        }
        ShaderType t = ShaderType.getFromType(shaderType);
        return function.getPath(shaderType) + source[t.index];
    }

    @Override
    public void updateUniformData(float[] destinationUniform) {
    }

    @Override
    public void initUniformData(float[] destinationUniforms) {
    }

}
