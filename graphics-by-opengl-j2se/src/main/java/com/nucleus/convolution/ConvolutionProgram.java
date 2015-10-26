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
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;

public class ConvolutionProgram extends ShaderProgram {

    protected final static int DEFAULT_COMPONENTS = 3;
    /**
     * Index into uniform char data where the texture fraction s (width) is
     */
    protected final static int UNIFORM_TEX_FRACTION_S_INDEX = 0;
    /**
     * Index into uniform charmap data where the texture fraction t (height) is
     */
    protected final static int UNIFORM_TEX_FRACTION_T_INDEX = 1;

    /**
     * Index into uniform charmap data where 1 / texture fraction w - this is used to calculate y pos from frame index
     */
    protected final static int UNIFORM_TEX_ONEBY_S_INDEX = 2;

    /**
     * Number of float data per vertex
     */
    protected final static int ATTRIBUTES_PER_VERTEX = 8;

    protected final static int ATTRIBUTE_1_OFFSET = 0;
    protected final static int ATTRIBUTE_2_OFFSET = 4;

    public enum VARIABLES {
        uMVPMatrix(0, ShaderVariable.VariableType.UNIFORM, 0),
        uKernel(1, ShaderVariable.VariableType.UNIFORM, 32),
        aPosition(2, ShaderVariable.VariableType.ATTRIBUTE, 0),
        aTexCoord(3, ShaderVariable.VariableType.ATTRIBUTE, 3);

        public final int index;
        protected final VariableType type;
        protected final int offset;

        /**
         * 
         * @param index Index of the shader variable
         * @param type Type of variable
         * @param offset Offset into data array where the variable data source is
         */
        private VARIABLES(int index, VariableType type, int offset) {
            this.index = index;
            this.type = type;
            this.offset = offset;
        }

    }

    private final static String VERTEX_SHADER_NAME = "assets/convolutionvertex.essl";
    private final static String FRAGMENT_SHADER_NAME = "assets/convolutionfragment.essl";

    ShaderVariable[] attribs;
    int[] offsets;

    public ConvolutionProgram() {
        super();
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
        // TODO - make into generic method that can be shared with TiledSpriteProgram
        VertexBuffer buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attribs, offsets);
        GLUtils.handleError(gles, "glVertexAttribPointers ");

    }

    @Override
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, Mesh mesh) throws GLException {
        ShaderVariable mvp = getShaderVariable(VARIABLES.uMVPMatrix.index);
        System.arraycopy(modelviewMatrix, 0, mesh.getUniformMatrices(), 0, modelviewMatrix.length);
        gles.glUniformMatrix4fv(getShaderVariable(VARIABLES.uMVPMatrix.index).getLocation(), mvp.getSize(), false,
                mesh.getUniformMatrices(), VARIABLES.uMVPMatrix.index);
        GLUtils.handleError(gles, "glUniformMatrix4fv ");

        ShaderVariable kernel = getShaderVariable(VARIABLES.uKernel.index);
        gles.glUniformMatrix3fv(getShaderVariable(VARIABLES.uKernel.index).getLocation(), kernel.getSize(), false,
                mesh.getUniformMatrices(), VARIABLES.uKernel.offset);
        GLUtils.handleError(gles, "glUniformMatrix4fv ");

    }

    /**
     * Builds a mesh with data that can be rendered using a tiled charmap renderer, this will draw a number of
     * charmaps using one drawcall.
     * Vertex buffer will have storage for XYZ + UV.
     * Before using the mesh the chars needs to be positioned, this call just creates the buffers. All chars will
     * have a position of 0.
     * 
     * @param mesh The mesh to build buffers for
     * @param texture The texture source, if tiling shall be used it must be {@link TiledTexture2D}
     * @param width The width of a char, the char will be left aligned.
     * @param height The height of a char, the char will be top aligned.
     * @param zPos The zpos for the mesh, all chars will have this zpos.
     * @param kernel The normalized kernel values, must be 3*3
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public void buildMesh(Mesh mesh, Texture2D texture, float width, float height, float zPos, float[] kernel) {

        float[] quadPositions = MeshBuilder.buildQuadPositionsUV(width, height, zPos, width / 2, height / 2);
        MeshBuilder.buildQuadMeshFan(mesh, this, quadPositions, 0);

        createUniformStorage(mesh, shaderVariables);
        mesh.setTexture(texture, Texture2D.TEXTURE_0);
        System.arraycopy(kernel, 0, mesh.getUniformMatrices(), VARIABLES.uKernel.offset, kernel.length);
        float deltaU = 1f / texture.getWidth();
        float deltaV = 1f / texture.getHeight();
        float[] uvOffsets = new float[] { -deltaU, 0, deltaU, -deltaU, 0, deltaU, -deltaU, 0, deltaU, -deltaV, 0,
                deltaV, -deltaV, 0, deltaV, -deltaV, 0, deltaV };
        System.arraycopy(uvOffsets, 0, mesh.getUniformMatrices(), VARIABLES.uKernel.offset + 9, uvOffsets.length);
    }

    @Override
    public void createProgram(GLES20Wrapper gles) {
        createProgram(gles, VERTEX_SHADER_NAME, FRAGMENT_SHADER_NAME);

        attribs = new ShaderVariable[] { getShaderVariable(VARIABLES.aPosition.index),
                getShaderVariable(VARIABLES.aTexCoord.index) };
        offsets = new int[] { VARIABLES.aPosition.offset, VARIABLES.aTexCoord.offset };

    }

}
