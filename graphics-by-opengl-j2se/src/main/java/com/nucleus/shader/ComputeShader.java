package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D.Shading;

public class ComputeShader extends ShaderProgram {

    public static final String CATEGORY = "compute";

    public ComputeShader(Shading shading, VariableMapping[] mapping) {
        super(null, shading, CATEGORY, mapping);
    }

    @Override
    protected void createShaderSource() {
        vertexShaderName = PROGRAM_DIRECTORY + getVertexShaderSource() + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = "";
    }

    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
        return this;
    }

    @Override
    public void setUniformData(float[] uniforms, Mesh mesh) {

    }

}
