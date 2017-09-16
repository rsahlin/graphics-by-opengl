package com.nucleus.convolution;

import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableMapping;
import com.nucleus.vecmath.Matrix;

public class ConvolutionProgram extends ShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;

    protected enum VARIABLES implements VariableMapping {
        uMVPMatrix(0, ShaderVariable.VariableType.UNIFORM, null),
        uKernel(1, ShaderVariable.VariableType.UNIFORM, null),
        aPosition(2, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
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
        super(VARIABLES.values());
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
    public void bindAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        AttributeBuffer buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attributeVariables[BufferIndex.VERTICES.index]);
        GLUtils.handleError(gles, "glVertexAttribPointers ");

    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        Matrix.mul4(modelviewMatrix, projectionMatrix);
        System.arraycopy(modelviewMatrix, 0, mesh.getUniforms(), 0, Matrix.MATRIX_ELEMENTS);
        bindUniforms(gles, uniforms, mesh.getUniforms());
    }

    @Override
    public void setupUniforms(Mesh mesh) {
        createUniformStorage(mesh, shaderVariables);
    }

    @Override
    public int getPropertyOffset(Property property) {
        // TODO Auto-generated method stub
        return 0;
    }
}
