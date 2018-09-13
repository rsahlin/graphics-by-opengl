package com.nucleus.shader;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D.Shading;

public class ComputeShader extends ShaderProgram {

    public static final String CATEGORY = "compute";

    public ComputeShader(String category) {
        super(null, null, category, ProgramType.COMPUTE);
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        return this;
    }

    @Override
    public void updateUniformData(float[] destinationUniform) {
    }

    @Override
    public void initUniformData(float[] destinationUniforms) {
    }

}
