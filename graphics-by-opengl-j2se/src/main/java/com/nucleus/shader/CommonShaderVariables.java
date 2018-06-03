package com.nucleus.shader;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * The variable names used for common shaders.
 * These can be used as a common way to find specific variables, for instance in order to share variables between
 * different shader programs.
 */
public enum CommonShaderVariables implements VariableMapping {
    uTexture(ShaderVariable.VariableType.UNIFORM, null),
    uShadowTexture(ShaderVariable.VariableType.UNIFORM, null),
    uMVMatrix(ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Projection matrix that can be applied after snapping to screen coordinates
     */
    uProjectionMatrix(ShaderVariable.VariableType.UNIFORM, null),
    /**
     * The light pov
     */
    uLightMatrix(ShaderVariable.VariableType.UNIFORM, null),
    uScreenSize(ShaderVariable.VariableType.UNIFORM, null),
    /**
     * Normally texture width, height and frames per line
     */
    uTextureData(ShaderVariable.VariableType.UNIFORM, null),
    uAmbientLight(ShaderVariable.VariableType.UNIFORM, null),
    uDiffuseLight(ShaderVariable.VariableType.UNIFORM, null),
    /**
     * The uvData field within the UVData block
     */
    uvData(ShaderVariable.VariableType.UNIFORM, null),
    uPointSize(ShaderVariable.VariableType.UNIFORM, null),
    aVertex(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES_STATIC),
    aTexCoord(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES_STATIC),
    aTranslate(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aRotate(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aScale(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aColor(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aFrameData(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aData1(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES),
    aEmissive(ShaderVariable.VariableType.ATTRIBUTE, BufferIndex.ATTRIBUTES);

    public final VariableType type;
    public final BufferIndex bufferIndex;

    /**
     * @param type Type of variable
     * @param bufferIndex Index of buffer in mesh that holds the variable data
     */
    private CommonShaderVariables(VariableType type, BufferIndex bufferIndex) {
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
