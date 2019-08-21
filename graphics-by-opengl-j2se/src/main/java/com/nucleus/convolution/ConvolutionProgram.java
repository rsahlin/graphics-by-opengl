package com.nucleus.convolution;

import com.nucleus.shader.GenericShaderProgram;
import com.nucleus.shader.Shader;
import com.nucleus.vecmath.Matrix;

public class ConvolutionProgram extends GenericShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;
    protected final float[] matrix = Matrix.createMatrix();
    private static final String VERTEX_SHADER_NAME = "convolution";
    private static final String FRAGMENT_SHADER_NAME = "convolution";

    public ConvolutionProgram() {
        super.init(new String[] { VERTEX_SHADER_NAME, FRAGMENT_SHADER_NAME }, null, Shading.textured, null,
                Shader.ProgramType.VERTEX_FRAGMENT);
    }

    @Override
    public void updateUniformData() {
    }

    @Override
    public void initUniformData() {
    }

}
