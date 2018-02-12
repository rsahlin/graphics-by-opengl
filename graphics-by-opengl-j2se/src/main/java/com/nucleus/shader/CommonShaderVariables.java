package com.nucleus.shader;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * The variable names used for common shaders.
 * These can be used as a common way to find specific variables, for instance in order to share variables between
 * different shader programs.
 */
public enum CommonShaderVariables implements VariableMapping {
    uTexture(0, ShaderVariable.VariableType.UNIFORM, null),
    uShadowTexture(2, ShaderVariable.VariableType.UNIFORM, null),
    uMVMatrix(4, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Projection matrix that can be applied after snapping to screen coordinates
     */
    uProjectionMatrix(20, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * The light pov
     */
    uLightMatrix(36, ShaderVariable.VariableType.UNIFORM, null),
    uScreenSize(52, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Normally texture width, height and frames per line
     */
    uTextureData(54, ShaderVariable.VariableType.UNIFORM, null),
    uAmbientLight(58, ShaderVariable.VariableType.UNIFORM, null),
    uDiffuseLight(62, ShaderVariable.VariableType.UNIFORM, null),
    aVertex(0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTexCoord(3, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTranslate(0, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aRotate(4, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aScale(8, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aColor(12, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aFrameData(16, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aData1(20, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES);

    /**
     * Index of variable
     */
    public int index = -1;
    /**
     * Offset into data where variable is, for static variable mapping
     */
    public final int offset;
    public final VariableType type;
    public final BufferIndex bufferIndex;

    /**
     * @param offset Offset into data where variable is. Used for static mapping
     * @param type Type of variable
     * @param bufferIndex Index of buffer in mesh that holds the variable data
     */
    private CommonShaderVariables(int offset, VariableType type, BufferIndex bufferIndex) {
        this.offset = offset;
        this.type = type;
        this.bufferIndex = bufferIndex;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
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
