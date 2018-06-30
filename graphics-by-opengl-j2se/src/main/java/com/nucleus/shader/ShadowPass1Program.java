package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES32;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.vecmath.Matrix;

/**
 * First shadow pass, render all geometry using vertex shader and light matrix then output distance to light in depth
 * buffer
 * Needs to use vertex shader based on the Function of the object being rendered and call that program to set
 * attributes/uniforms
 *
 */
public class ShadowPass1Program extends ShadowPassProgram {

    /**
     * TODO Look into the shader programs using this constructor - maybe they can be unified?
     * 
     * @param objectProgram The program for rendering the object casting shadow
     * @param shading
     * @param category
     * @param shaders
     */
    public ShadowPass1Program(ShaderProgram objectProgram, Texture2D.Shading shading, String category,
            ProgramType shaders) {
        super(objectProgram, Pass.SHADOW1, shading, category, shaders);
    }

    @Override
    protected ShaderSource getShaderSource(Renderers version, int type) {
        switch (type) {
            case GLES20.GL_FRAGMENT_SHADER:
                if (function.getPass() != null) {
                    return new ShaderSource(
                            PROGRAM_DIRECTORY + function.getPassString() + function.getShadingString() + FRAGMENT_TYPE,
                            objectProgram.getSourceNameVersion(version, type), type);
                } else {
                    return super.getShaderSource(version, type);
                }
            case GLES20.GL_VERTEX_SHADER:
                return objectProgram.getShaderSource(version, type);
            case GLES32.GL_GEOMETRY_SHADER:
                return objectProgram.getShaderSource(version, type);
            default:
                throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public void setUniformMatrices(float[][] matrices, Mesh mesh) {
        System.arraycopy(matrices[Matrices.MODELVIEW.index], 0, uniforms,
                getUniformByName("uMVMatrix").getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(matrices[Matrices.RENDERPASS_2.index], 0, uniforms,
                getUniformByName("uProjectionMatrix").getOffset(),
                Matrix.MATRIX_ELEMENTS);
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
        GlobalLight.getInstance().getLightMatrix(matrix);
        Matrix.setIdentity(matrix, 0);
        return matrix;
    }

    @Override
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initBuffers(Mesh mesh) {
        // TODO Auto-generated method stub

    }

}
