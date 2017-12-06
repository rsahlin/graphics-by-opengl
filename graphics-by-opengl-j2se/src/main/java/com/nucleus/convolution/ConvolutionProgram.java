package com.nucleus.convolution;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableMapping;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.vecmath.Matrix;

public class ConvolutionProgram extends ShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;

    protected enum VARIABLES implements VariableMapping {
        uMVPMatrix(0, ShaderVariable.VariableType.UNIFORM, null),
        uKernel(1, ShaderVariable.VariableType.UNIFORM, null),
        aTranslate(2, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
        aTexCoord(3, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES);

        private final int index;
        private final VariableType type;
        private final BufferIndex bufferIndex;

        /**
         * @param index Index of the shader variable
         * @param type Type of variable
         * @param bufferIndex Index of buffer in mesh that holds the variable data
         */
        private VARIABLES(int index, VariableType type, BufferIndex bufferIndex) {
            this.index = index;
            this.type = type;
            this.bufferIndex = bufferIndex;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public VariableType getType() {
            return type;
        }

        @Override
        public BufferIndex getBufferIndex() {
            return bufferIndex;
        }

    }

    private final static String VERTEX_SHADER_NAME = "assets/convolutionvertex.essl";
    private final static String FRAGMENT_SHADER_NAME = "assets/convolutionfragment.essl";

    public ConvolutionProgram() {
        super(null, null, null, VARIABLES.values());
        vertexShaderName = VERTEX_SHADER_NAME;
        fragmentShaderName = FRAGMENT_SHADER_NAME;
    }

    @Override
    public VariableMapping getVariableMapping(ShaderVariable variable) {
        return VARIABLES.valueOf(getVariableName(variable));
    }

    @Override
    public int getVariableCount() {
        return VARIABLES.values().length;
    }

    @Override
    public void updateAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        AttributeBuffer buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attributeVariables[BufferIndex.VERTICES.index]);
        GLUtils.handleError(gles, "glVertexAttribPointers ");

    }

    @Override
    public void updateUniforms(GLES20Wrapper gles, float[][] matrices, Mesh mesh)
            throws GLException {
        Matrix.mul4(matrices[0], matrices[1]);
        System.arraycopy(matrices[0], 0, getUniforms(), 0, Matrix.MATRIX_ELEMENTS);
        setUniforms(gles, uniforms, sourceUniforms);
    }

    @Override
    public ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
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
    public void setUniformData(float[] uniforms, Mesh mesh) {
    }

}
