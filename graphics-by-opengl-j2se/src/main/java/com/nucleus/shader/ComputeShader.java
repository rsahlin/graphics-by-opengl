package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D.Shading;

public class ComputeShader extends ShaderProgram {

    public static final String CATEGORY = "compute";

    public ComputeShader(Shading shading, VariableMapping[] mapping) {
        super(null, shading, CATEGORY, mapping, Shaders.COMPUTE);
    }

    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
        return this;
    }

    @Override
    public void setUniformData(float[] uniforms, Mesh mesh) {

    }

}
