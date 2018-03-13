package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.vecmath.Matrix;

/**
 * First shadow pass, render all geometry using vertex shader and light matrix then output distance to light in depth
 * buffer
 * Needs to use vertex shader based on the Function of the object being rendered and call that program to set
 * attributes/uniforms
 *
 */
public class ShadowPass1Program extends ShaderProgram {

    /**
     * The program that should be used to render the object casting shadow
     */
    private ShaderProgram objectProgram;

    /**
     * TODO Look into the shader programs using this constructor - maybe they can be unified?
     * 
     * @param objectProgram The program for rendering the object casting shadow
     * @param shading
     * @param category
     */
    public ShadowPass1Program(ShaderProgram objectProgram, Texture2D.Shading shading, String category) {
        super(Pass.SHADOW1, shading, category, CommonShaderVariables.values(), Shaders.VERTEX_FRAGMENT);
        this.objectProgram = objectProgram;
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
            default:
                throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public void setUniformMatrices(float[][] matrices, Mesh mesh) {
        System.arraycopy(matrices[0], 0, uniforms,
                shaderVariables[CommonShaderVariables.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(matrices[Matrices.RENDERPASS_1.index], 0, uniforms,
                shaderVariables[CommonShaderVariables.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
    }

    @Override
    public void updateAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        objectProgram.updateAttributes(gles, mesh);
    }

    @Override
    public void updateUniforms(GLES20Wrapper gles, float[][] matrices, Mesh mesh)
            throws GLException {
        /**
         * Currently calls ShaderProgram#setUniformData() in order to set necessary data from the program int
         * uniform storage.
         * This could potentially break the shadow program if needed uniform data is set in some other method.
         * TODO - Make sure that the interface declares and mandates that uniform data shall be set in #setUniformData()
         */
        objectProgram.setUniformData(uniforms, mesh);
        super.updateUniforms(gles, matrices, mesh);
    }

    /**
     * Returns the global light direction matrix using orthographic projection
     * 
     * @param matrix The result matrix
     * @return matrix that contains the global light direction.
     * 
     */
    public static float[] getLightMatrix(float[] matrix) {
        float[] lightVector = GlobalLight.getInstance().getLightVector();
        Matrix.setRotateEulerM(matrix, 0, lightVector[0], lightVector[1], lightVector[2]);
        return matrix;
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        throw new IllegalArgumentException("Not valid");
    }

    @Override
    public void setUniformData(float[] destinationUniform, Mesh mesh) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initBuffers(Mesh mesh) {
        // TODO Auto-generated method stub

    }

}
