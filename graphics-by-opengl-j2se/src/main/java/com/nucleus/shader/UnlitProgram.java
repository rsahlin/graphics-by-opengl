package com.nucleus.shader;

import com.nucleus.SimpleLogger;
import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.Untextured;
import com.nucleus.vecmath.Matrix;

/**
 * Program for unlit vertices, shader calculates vertex position with position offset and draws pixels using vertex
 * color.
 */
public class UnlitProgram extends ShaderProgram {

    /**
     * The shader names used, the variable names used in shader sources MUST be defined here.
     */
    public enum VARIABLES implements VariableMapping {
        uMVMatrix(0, ShaderVariable.VariableType.UNIFORM, null),
        uProjectionMatrix(1, ShaderVariable.VariableType.UNIFORM, null),
        aPosition(2, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
        aColor(3, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES);

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

    public UnlitProgram(Untextured.Shading shading) {
        super(VARIABLES.values());
        vertexShaderName = PROGRAM_DIRECTORY + shading.name() + VERTEX + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + shading.name() + FRAGMENT + SHADER_SOURCE_SUFFIX;
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
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        // Refresh the uniform matrix
        // TODO prefetch the offsets for the shader variables and store in array.
        System.arraycopy(modelviewMatrix, 0, mesh.getUniforms(), shaderVariables[VARIABLES.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(projectionMatrix, 0, mesh.getUniforms(),
                shaderVariables[VARIABLES.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        bindUniforms(gles, uniforms, mesh.getUniforms());
    }

    @Override
    public void setupUniforms(Mesh mesh) {
        createUniformStorage(mesh, shaderVariables);
    }


    @Override
    public int getPropertyOffset(Property property) {
        ShaderVariable v = null;
        switch (property) {
        case TRANSLATE:
            v = shaderVariables[VARIABLES.aPosition.index];
            break;
        case COLOR:
            v = shaderVariables[VARIABLES.aColor.index];
            break;
        default:
        }
        if (v != null) {
            return v.getOffset();
        } else {
            SimpleLogger.d(getClass(), "No ShaderVariable for " + property);

        }
        return -1;
    }
}
