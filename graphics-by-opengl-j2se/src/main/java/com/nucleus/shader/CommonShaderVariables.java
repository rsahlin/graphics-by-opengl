package com.nucleus.shader;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * The variable names used for common shaders.
 * These can be used as a common way to find specific variables, for instance in order to share variables between
 * different shader programs.
 */
public enum CommonShaderVariables implements VariableMapping {
    uTexture(0, 0, ShaderVariable.VariableType.UNIFORM, null),
    uShadowTexture(1, 2, ShaderVariable.VariableType.UNIFORM, null),
    uMVMatrix(2, 4, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Projection matrix that can be applied after snapping to screen coordinates
     */
    uProjectionMatrix(3, 20, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * The light pov
     */
    uLightMatrix(4, 36, ShaderVariable.VariableType.UNIFORM, null),
    uScreenSize(5, 52, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Normally texture width, height and frames per line
     */
    uTextureData(6, 54, ShaderVariable.VariableType.UNIFORM, null),
    uAmbientLight(7, 58, ShaderVariable.VariableType.UNIFORM, null),
    uDiffuseLight(8, 62, ShaderVariable.VariableType.UNIFORM, null),
    aVertex(9, 0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTexCoord(10, 3, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTranslate(11, 0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aRotate(12, 4, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aScale(13, 8, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aColor(14, 12, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aFrameData(15, 16, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aData1(16, 20, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES);

    /**
     * Index (id) of variable
     */
    public final int index;
    /**
     * Offset into data where variable is, for static variable mapping
     */
    public final int offset;
    public final VariableType type;
    public final BufferIndex bufferIndex;

    /**
     * @param index Index (id) of variable, can be used to locate the variable in an array.
     * @param offset Offset into data where variable is. Used for static mapping
     * @param type Type of variable
     * @param bufferIndex Index of buffer in mesh that holds the variable data
     */
    private CommonShaderVariables(int index, int offset, VariableType type, BufferIndex bufferIndex) {
        this.index = index;
        this.offset = offset;
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

    @Override
    public String getName() {
        return name();
    }

    @Override
    public int getOffset() {
        return offset;
    }
}
