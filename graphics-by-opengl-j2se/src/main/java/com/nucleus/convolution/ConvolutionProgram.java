package com.nucleus.convolution;

import static com.nucleus.geometry.VertexBuffer.STRIP_QUAD_VERTICES;

import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.Mesh.Mode;
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
        VertexBuffer buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
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
    @Deprecated
    public void buildMesh(Mesh mesh, Texture2D texture, float width, float height, float zPos, float[] kernel) {

        float[] quadPositions = MeshBuilder.createQuadPositionsUV(width, height, zPos, -width / 2, -height / 2);
        buildQuadMeshFan(mesh, this, quadPositions, 0);
        mesh.setTexture(texture, Texture2D.TEXTURE_0);
        System.arraycopy(kernel, 0, mesh.getUniforms(), shaderVariables[VARIABLES.uKernel.index].getOffset(),
                kernel.length);
        float deltaU = 1f / texture.getWidth();
        float deltaV = 1f / texture.getHeight();
        float[] uvOffsets = new float[] { -deltaU, 0, deltaU, -deltaU, 0, deltaU, -deltaU, 0, deltaU, -deltaV, 0,
                deltaV, -deltaV, 0, deltaV, -deltaV, 0, deltaV };
        System.arraycopy(uvOffsets, 0, mesh.getUniforms(), shaderVariables[VARIABLES.uKernel.index].getOffset() + 9,
                uvOffsets.length);
    }

    /**
     * This method is deprecated, do not create attribute buffers in this builder.
     * Builds a quad mesh using a fan, the mesh can be rendered using glDrawArrays
     * 
     * @param mesh
     * @param program The program to use for the material in the mesh
     * @param quadPositions
     * @param attribute2Size
     */
    @Deprecated
    public void buildQuadMeshFan(Mesh mesh, ShaderProgram program, float[] quadPositions, int attribute2Size) {
        int attributeBuffers = 1;
        if (attribute2Size > 0) {
            attributeBuffers = 2;
        }
        VertexBuffer[] attributes = program.createAttributeBuffers(mesh, STRIP_QUAD_VERTICES);
        attributes[BufferIndex.VERTICES.index].setPositionUV(quadPositions, 0, 0, STRIP_QUAD_VERTICES);
        Material material = new Material();
        material.setProgram(program);
        mesh.setupVertices(attributes, material, null);
        mesh.setMode(Mode.TRIANGLE_FAN);
        program.setupUniforms(mesh);
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
