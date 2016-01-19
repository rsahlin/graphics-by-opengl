package com.nucleus.convolution;

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

public class ConvolutionProgram extends ShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;

    protected enum VARIABLES implements VariableMapping {
        uMVPMatrix(0, 0, ShaderVariable.VariableType.UNIFORM),
        uKernel(1, 16, ShaderVariable.VariableType.UNIFORM),
        aPosition(2, 0, ShaderVariable.VariableType.ATTRIBUTE),
        aTexCoord(3, 3, ShaderVariable.VariableType.ATTRIBUTE);

        public final int index;
        protected final VariableType type;
        protected final int offset;

        /**
         * 
         * @param index Index of the shader variable
         * @param offset Offset into data array where the variable data source is
         * @param type Type of variable
         */
        private VARIABLES(int index, int offset, VariableType type) {
            this.index = index;
            this.type = type;
            this.offset = offset;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public int getOffset() {
            return offset;
        }

    }

    private final static String VERTEX_SHADER_NAME = "assets/convolutionvertex.essl";
    private final static String FRAGMENT_SHADER_NAME = "assets/convolutionfragment.essl";

    ShaderVariable[] attribs;
    int[] offsets;

    public ConvolutionProgram() {
        super();
        vertexShaderName = VERTEX_SHADER_NAME;
        fragmentShaderName = FRAGMENT_SHADER_NAME;
        // attributesPerVertex = ATTRIBUTES_PER_VERTEX;
        uniforms = new VariableMapping[] { VARIABLES.uMVPMatrix, VARIABLES.uKernel };

    }

    @Override
    public int getVariableIndex(ShaderVariable variable) {
        return VARIABLES.valueOf(getVariableName(variable)).index;
    }

    @Override
    public int getVariableCount() {
        return VARIABLES.values().length;
    }

    @Override
    public void bindAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        VertexBuffer buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attribs, offsets);
        GLUtils.handleError(gles, "glVertexAttribPointers ");

    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, Mesh mesh) throws GLException {
        System.arraycopy(modelviewMatrix, 0, mesh.getUniforms(), 0, modelviewMatrix.length);
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

        float[] quadPositions = MeshBuilder.buildQuadPositionsUV(width, height, zPos, -width / 2, -height / 2);
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
    public void createProgram(GLES20Wrapper gles) {
        createProgram(gles, VERTEX_SHADER_NAME, FRAGMENT_SHADER_NAME);

        attribs = new ShaderVariable[] { getShaderVariable(VARIABLES.aPosition),
                getShaderVariable(VARIABLES.aTexCoord) };
        offsets = new int[] { VARIABLES.aPosition.offset, VARIABLES.aTexCoord.offset };
    }

    @Override
    public void setupUniforms(Mesh mesh) {
        createUniformStorage(mesh, shaderVariables);
    }
}
