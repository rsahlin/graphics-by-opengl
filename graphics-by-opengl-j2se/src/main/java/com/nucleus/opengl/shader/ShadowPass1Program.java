package com.nucleus.opengl.shader;

import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.vecmath.Matrix;

/**
 * First shadow pass, render all geometry using vertex shader and light matrix then output distance to light in depth
 * buffer
 * Needs to use vertex shader based on the Function of the object being rendered and call that program to set
 * attributes/uniforms
 *
 */
public class ShadowPass1Program extends ShadowPassProgram {

    public ShadowPass1Program(Pass pass, Shading shading, String category, ProgramType shaders) {
        super(pass, shading, category, shaders);
    }

    static class Shadow1Categorizer extends Categorizer {

        public Shadow1Categorizer(Pass pass, Shading shading, String category) {
            super(pass, shading, category);
        }

        @Override
        public String getShaderSourceName(ShaderType type) {
            switch (type) {
                case VERTEX:
                    // For vertex shader ignore the pass
                    return getPath(type) + getShadingString();
                default:
                    return null;

            }
        }
    }

    @Override
    public void setUniformMatrices(float[][] matrices) {
        if (modelUniform == null) {
            modelUniform = getUniformByName(Matrices.Name);
        }
        uniforms.position(modelUniform.getOffset());
        uniforms.put(matrices[Matrices.MODEL.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.VIEW.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.RENDERPASS_2.index], 0, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Returns the global light direction matrix using orthographic projection
     * 
     * @param matrix The result matrix
     * @return matrix that contains the global light direction.
     * 
     */
    public static float[] getLightMatrix(float[] matrix) {
        // TODO implement light position/vector properly
        // float[] lightVector = GlobalLight.getInstance().getLightVector();
        // Matrix.setRotateM(matrix, 0, 0, lightVector[0], lightVector[1], lightVector[2]);
        // GlobalLight.getInstance().getLightMatrix(matrix);
        Matrix.setIdentity(matrix, 0);
        return matrix;
    }

    @Override
    public void updateUniformData() {
    }

    @Override
    public void initUniformData() {
    }

}
