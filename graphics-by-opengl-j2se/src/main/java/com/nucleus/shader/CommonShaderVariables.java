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
    /**
     * The uvData field within the UVData block
     */
    uvData(9, 0, ShaderVariable.VariableType.UNIFORM, null),
    uPointSize(10, 66, ShaderVariable.VariableType.UNIFORM, null),
    aVertex(11, 0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTexCoord(12, 4, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTranslate(13, 0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aRotate(14, 4, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aScale(15, 8, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aColor(16, 12, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aFrameData(17, 16, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aData1(18, 20, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aEmissive(19, 24, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES);

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
