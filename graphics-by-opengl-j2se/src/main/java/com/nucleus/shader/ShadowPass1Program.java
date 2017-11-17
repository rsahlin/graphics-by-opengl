package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
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
        super(Pass.SHADOW1, shading, category, ShaderVariables.values());
        this.objectProgram = objectProgram;
    }

    @Override
    protected void setShaderSource() {
        vertexShaderName = PROGRAM_DIRECTORY + "transform" + VERTEX_TYPE
                + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + Pass.SHADOW1.name().toLowerCase() + function.shading.name()
                + FRAGMENT_TYPE
                + SHADER_SOURCE_SUFFIX;
    }

    @Override
    public void setUniformMatrices(float[] uniforms, float[][] matrices, Mesh mesh) {
        System.arraycopy(matrices[0], 0, getUniforms(),
                shaderVariables[ShaderVariables.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(matrices[2], 0, getUniforms(),
                shaderVariables[ShaderVariables.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
    }

    @Override
    public void updateAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        objectProgram.updateAttributes(gles, mesh);
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
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
        throw new IllegalArgumentException("Not valid");
    }

    @Override
    public void setUniformData(float[] uniforms, Mesh mesh) {
        // TODO Auto-generated method stub

    }
    
}
