package com.nucleus.shader;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * The shader names used
 */
public enum ShaderVariables implements VariableMapping {
    uTexture(0, ShaderVariable.VariableType.UNIFORM, null),
    uShadowTexture(1, ShaderVariable.VariableType.UNIFORM, null),
    uMVMatrix(2, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Projection matrix that can be applied after snapping to screen coordinates
     */
    uProjectionMatrix(3, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * The light pov
     */
    uLightMatrix(4, ShaderVariable.VariableType.UNIFORM, null),
    uScreenSize(5, ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Normally texture width, height and frames per line
     */
    uTextureData(6, ShaderVariable.VariableType.UNIFORM, null),
    uAmbientLight(7, ShaderVariable.VariableType.UNIFORM, null),
    uDiffuseLight(8, ShaderVariable.VariableType.UNIFORM, null),
    aVertex(9, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTexCoord(10, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.VERTICES),
    aTranslate(11, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aRotate(12, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aScale(13, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aColor(14, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aFrameData(15, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aData1(16, ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES);
    
    public final int index;
    public final VariableType type;
    public final BufferIndex bufferIndex;

    /**
     * @param index Index of the shader variable
     * @param type Type of variable
     * @param bufferIndex Index of buffer in mesh that holds the variable data
     */
    private ShaderVariables(int index, VariableType type, BufferIndex bufferIndex) {
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
