package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
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
     * @param shading
     */
    public ShadowPass1Program(Texture2D.Shading shading) {
        super(shading, ShaderVariables.values());
    }
    
    @Override
    protected void setShaderSource(Texture2D.Shading shading) {
        vertexShaderName = PROGRAM_DIRECTORY + VERTEX_NAME + VERTEX + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + FRAGMENT_NAME + shading.name() + FRAGMENT + SHADER_SOURCE_SUFFIX;
    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        // Refresh the uniform matrix using the light matrix
        System.arraycopy(modelviewMatrix, 0, getUniforms(),
                shaderVariables[ShaderVariables.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(getLightMatrix(), 0, getUniforms(),
                shaderVariables[ShaderVariables.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        setUniforms(gles, sourceUniforms);
    }

    public static float[] getLightMatrix() {
        //Modelview is facing into screen.
        float[] lightPOV = new float[16];
        float[] result = new float[16];
        float[] lightVector = GlobalLight.getInstance().getLightVector();
        Matrix.setRotateEulerM(result, 0, lightVector[0], lightVector[1], lightVector[2]);
        Matrix.orthoM(lightPOV, 0, -0.8889f, 0.8889f, -0.5f, 0.5f, 0f, 10f);
        Matrix.mul4(result, lightPOV);
        return result;
    }
    
}
