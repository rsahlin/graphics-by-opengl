package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.Texture2D.Shading;

public class ComputeShader extends ShaderProgram {

    public static final String CATEGORY = "compute";

    /**
     * The shader names used
     */
    public enum ComputeVariables implements VariableMapping {
        uConfig(0, 0, ShaderVariable.VariableType.UNIFORM, null);

        /**
         * Index of variable
         */
        public final int index;
        /**
         * Offset into data where variable is, for static variable mapping
         */
        public final int offset;
        public final VariableType type;
        public final BufferIndex bufferIndex;

        /**
         * @param index
         * @param offset Offset into data where variable is. Used for static mapping
         * @param type Type of variable
         * @param bufferIndex Index of buffer in mesh that holds the variable data
         */
        private ComputeVariables(int index, int offset, VariableType type, BufferIndex bufferIndex) {
            this.index = index;
            this.offset = offset;
            this.type = type;
            this.bufferIndex = bufferIndex;
        }

        @Override
        public VariableType getType() {
            return type;
        }

        @Override
        public BufferIndex getBufferIndex() {
            return bufferIndex;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    public ComputeShader(String category, VariableMapping[] mapping) {
        super(null, null, category, mapping, ProgramType.COMPUTE);
    }

    @Override
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        return this;
    }

    @Override
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {

    }

    @Override
    public void initBuffers(Mesh mesh) {
        // TODO Auto-generated method stub

    }

}
