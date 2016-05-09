package com.nucleus.convolution;

import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableMapping;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.Matrix;

public class ConvolutionProgram extends ShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;
    private final static int ATTRIBUTES_PER_VERTEX = 5;

    protected enum VARIABLES implements VariableMapping {
        uMVPMatrix(0, 0, ShaderVariable.VariableType.UNIFORM, null),
        uKernel(1, 16, ShaderVariable.VariableType.UNIFORM, null),
        aPosition(2, 0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
        aTexCoord(3, 3, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES);

        private final int index;
        private final VariableType type;
        public final int offset;
        private final BufferIndex bufferIndex;

        /**
         * @param index Index of the shader variable
         * @param offset Offset into data array where the variable data source is
         * @param type Type of variable
         * @param bufferIndex Index of buffer in mesh that holds the variable data
         */
        private VARIABLES(int index, int offset, VariableType type, BufferIndex bufferIndex) {
            this.index = index;
            this.type = type;
            this.offset = offset;
            this.bufferIndex = bufferIndex;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public int getOffset() {
            return offset;
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
        attributesPerVertex = ATTRIBUTES_PER_VERTEX;
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
        VertexBuffer buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, variablesPerBuffer[BufferIndex.VERTICES.index]);
        GLUtils.handleError(gles, "glVertexAttribPointers ");

    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        Matrix.mul4(modelviewMatrix, projectionMatrix);
        System.arraycopy(modelviewMatrix, 0, mesh.getUniforms(), 0, Matrix.MATRIX_ELEMENTS);
        bindUniforms(gles, uniforms, mesh.getUniforms());
    }

    /**
     * Builds the mesh to use for convolution
     * 
     * @param mesh The mesh to build buffers for
     * @param texture The texture source, if tiling shall be used it must be {@link TiledTexture2D}
     * @param width The width of the mesh
     * @param height The height of the mesh
     * @param zPos The zpos for the mesh
     * @param kernel The normalized kernel values, must be 3*3
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public void buildMesh(Mesh mesh, Texture2D texture, float width, float height, float zPos, float[] kernel) {

        float[] quadPositions = MeshBuilder.createQuadPositionsUV(width, height, zPos, -width / 2, -height / 2);
        MeshBuilder.buildQuadMeshFan(mesh, this, quadPositions, 0);
        mesh.setTexture(texture, Texture2D.TEXTURE_0);
        System.arraycopy(kernel, 0, mesh.getUniforms(), VARIABLES.uKernel.offset, kernel.length);
        float deltaU = 1f / texture.getWidth();
        float deltaV = 1f / texture.getHeight();
        float[] uvOffsets = new float[] { -deltaU, 0, deltaU, -deltaU, 0, deltaU, -deltaU, 0, deltaU, -deltaV, 0,
                deltaV, -deltaV, 0, deltaV, -deltaV, 0, deltaV };
        System.arraycopy(uvOffsets, 0, mesh.getUniforms(), VARIABLES.uKernel.offset + 9, uvOffsets.length);
    }

    @Override
    public void setupUniforms(Mesh mesh) {
        createUniformStorage(mesh, shaderVariables);
    }

    @Override
    public int getAttributeOffset(int vertex) {
        return vertex * ATTRIBUTES_PER_VERTEX;
    }

    @Override
    public int getPropertyOffset(Property property) {
        // TODO Auto-generated method stub
        return 0;
    }
}
