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

    protected static final String CATEGORY = "transform";

    public ShadowPass1Program(Texture2D.Shading shading) {
        super(Pass.SHADOW1, shading, CATEGORY);
    }
    
    /**
     * TODO Look into the shader programs using this constructor - maybe they can be unified?
     * 
     * @param shading
     * @param category
     */
    public ShadowPass1Program(Texture2D.Shading shading, String category) {
        super(Pass.SHADOW1, shading, category);
    }

    @Override
    protected void setShaderSource() {
        vertexShaderName = PROGRAM_DIRECTORY + CATEGORY + VERTEX_TYPE + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + Pass.SHADOW1.name().toLowerCase() + sourceName.shading.name()
                + FRAGMENT_TYPE
                + SHADER_SOURCE_SUFFIX;
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
