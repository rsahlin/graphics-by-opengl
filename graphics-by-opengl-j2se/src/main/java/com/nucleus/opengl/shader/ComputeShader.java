package com.nucleus.opengl.shader;

import java.nio.FloatBuffer;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;

public class ComputeShader extends GLShaderProgram {

    public static final String CATEGORY = "compute";

    public ComputeShader(String category) {
        super(null, null, category, GLShaderProgram.ProgramType.COMPUTE);
    }

    @Override
    public GLShaderProgram getProgram(NucleusRenderer renderer, Pass pass, GLShaderProgram.Shading shading) {
        return this;
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
    }

}
