package com.nucleus.convolution;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.vecmath.Matrix;

public class ConvolutionProgram extends ShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;
    protected final float[] matrix = Matrix.createMatrix();

    public ConvolutionProgram() {
        super(null, null, null, ProgramType.VERTEX_FRAGMENT);
    }

    @Override
    public void updateAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        AttributeBuffer buffer = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
        gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER,
                attributeVariables[BufferIndex.ATTRIBUTES_STATIC.index]);
        GLUtils.handleError(gles, "glVertexAttribPointers ");

    }

    @Override
    public void updateUniforms(GLES20Wrapper gles, float[][] matrices, Mesh mesh)
            throws GLException {
        Matrix.mul4(matrices[0], matrices[1], matrix);
        System.arraycopy(matrix, 0, uniforms, 0, Matrix.MATRIX_ELEMENTS);
        uploadUniforms(gles, uniforms, activeUniforms);
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
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {
    }

    @Override
    public void initBuffers(Mesh mesh) {
        // TODO Auto-generated method stub

    }

}
