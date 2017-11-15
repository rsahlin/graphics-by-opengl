package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.Texture2D;
import com.nucleus.vecmath.Matrix;

/**
 * First shadow pass, render all geometry using vertex shader and light matrix then output distance to light in depth buffer
 *
 */
public class ShadowPass1Program extends TransformProgram {

    /**
     * Name of this shader - TODO where should this be defined?
     */
    protected static final String VERTEX_NAME = "transform";
    protected static final String FRAGMENT_NAME = "shadow1";
    
    /**
     * TODO Look into the shader programs using this constructor - maybe they can be unified?
     * 
     * @param pass
     * @param category
     * @param shading
     */
    public ShadowPass1Program(Pass pass, String category, Texture2D.Shading shading) {
        super(pass, category, shading);
    }

    @Override
    protected void setShaderSource(Texture2D.Shading shading) {
        vertexShaderName = PROGRAM_DIRECTORY + VERTEX_NAME + VERTEX_TYPE + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + FRAGMENT_NAME + shading.name() + FRAGMENT_TYPE + SHADER_SOURCE_SUFFIX;
    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[][] matrices, Mesh mesh)
            throws GLException {
        // Refresh the uniform matrix using the light matrix
        System.arraycopy(matrices[0], 0, getUniforms(),
                shaderVariables[ShaderVariables.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(matrices[2], 0, getUniforms(),
                shaderVariables[ShaderVariables.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        setUniforms(gles, sourceUniforms);
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
    
}
