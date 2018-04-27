package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Program for translated vertices, shader calculates vertex position with position offset
 * Can be used to draw lines, polygons or similar - objects cannot be independently rotated or scaled
 * - use {@link TransformProgram}
 */
public class TranslateProgram extends ShaderProgram {

    ShaderVariable uPointSize;

    private float pointSize = 1;

    public TranslateProgram(Texture2D.Shading shading) {
        super(null, shading, null, CommonShaderVariables.values(), Shaders.VERTEX_FRAGMENT);
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
            default:
                throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }

    @Override
    protected void setShaderVariable(ShaderVariable variable) {
        super.setShaderVariable(variable);
        if (variable.getName().contentEquals(CommonShaderVariables.uPointSize.name())) {
            uPointSize = variable;
        }
    }

    @Override
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {
        if (uPointSize != null) {
            destinationUniform[uPointSize.getOffset()] = pointSize;
        }
    }

    @Override
    public void initBuffers(Mesh mesh) {
        // TODO Auto-generated method stub

    }

}
