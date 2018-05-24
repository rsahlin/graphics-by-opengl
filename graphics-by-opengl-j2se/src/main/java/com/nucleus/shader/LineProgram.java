package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;

/**
 * Program for rendering lines and similar.
 *
 */
public class LineProgram extends ShaderProgram {

    public static final String CATEGORY = "line";

    ShaderVariable uPointSize;

    private float pointSize = 1;

    public LineProgram(Texture2D.Shading shading) {
        super(null, shading, CATEGORY, CommonShaderVariables.values(), Shaders.VERTEX_FRAGMENT);
    }

    @Override
    protected String getSourceNameVersion(Renderers version, int type) {
        if (version.major >= 3) {
            return ShaderSource.V300;
        }
        return super.getSourceNameVersion(version, type);
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
