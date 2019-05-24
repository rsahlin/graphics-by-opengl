package com.nucleus.opengl.shader;

import java.nio.FloatBuffer;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;

public class ComputeShader extends ShaderProgram {

    public static final String CATEGORY = "compute";

    public ComputeShader(String category) {
        super(null, null, category, ShaderProgram.ProgramType.COMPUTE);
    }

    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, ShaderProgram.Shading shading) {
        return this;
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
    }

}
