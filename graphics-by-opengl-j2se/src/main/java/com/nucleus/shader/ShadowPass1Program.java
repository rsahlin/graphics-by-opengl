package com.nucleus.shader;

import java.nio.FloatBuffer;

import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram.Shading;
import com.nucleus.vecmath.Matrix;

/**
 * First shadow pass, render all geometry using vertex shader and light matrix then output distance to light in depth
 * buffer
 * Needs to use vertex shader based on the Function of the object being rendered and call that program to set
 * attributes/uniforms
 *
 */
public class ShadowPass1Program extends ShadowPassProgram {

    static class Shadow1Categorizer extends Categorizer {

        public Shadow1Categorizer(Pass pass, ShaderProgram.Shading shading, String category) {
            super(pass, shading, category);
        }

        @Override
        public String getShaderSourceName(int shaderType) {
            switch (shaderType) {
                case GLES20.GL_VERTEX_SHADER:
                    // For vertex shader ignore the pass
                    return getPath(shaderType) + getShadingString();
                default:
                    return null;

            }
        }
    }

    /**
     * @param objectProgram The program for rendering the object casting shadow
     * @param categorizer
     * @param shaders
     */
    public ShadowPass1Program(ShaderProgram objectProgram, Categorizer categorizer, ShaderProgram.ProgramType shaders) {
        super(objectProgram, categorizer, shaders);
    }

    @Override
    public void setUniformMatrices(float[][] matrices) {
        if (modelUniform == null) {
            modelUniform = getUniformByName(Matrices.MODEL.name);
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
    public void updateUniformData(FloatBuffer destinationUniform) {
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
    }

}
