package com.nucleus.shader;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Constants;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.ParameterData;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Name;
import com.nucleus.texturing.TextureParameter.Param;
import com.nucleus.texturing.TextureParameter.Parameter;
import com.nucleus.texturing.TextureParameter.Target;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.TextureUtils;
import com.nucleus.vecmath.Matrix;

/**
 * Shader for second shadow pass
 * This should combine the sprite program with shadow render
 *
 */
public class ShadowPass2Program extends ShadowPassProgram {

    static class Shadow2Categorizer extends Categorizer {

        public Shadow2Categorizer(Pass pass, Shading shading, String category) {
            super(pass, shading, category);
        }

        @Override
        public String getShaderSourceName(int shaderType) {
            switch (shaderType) {
                case GLES20.GL_FRAGMENT_SHADER:
                    // For fragment shader ignore the category
                    return getPassString() + getShadingString();
                default:
                    return super.getShaderSourceName(shaderType);
            }
        }

    }

    public static final String DEPTH_SHADOW_NAME = "DEPTHshadow";

    Texture2D shadow;

    /**
     * 
     * @param objectProgram
     * @param pass
     * @param category
     * @param shading
     * @param shaders
     */
    public ShadowPass2Program(ShaderProgram objectProgram, Pass pass, String category, Texture2D.Shading shading,
            ProgramType shaders) {
        super(objectProgram, new Shadow2Categorizer(Pass.SHADOW2, shading, category), shaders);
        setIndexer(
                objectProgram.variableIndexer != null ? objectProgram.variableIndexer : objectProgram.createIndexer());
        // This defines the texture parameters for the shadow pass.
        // TODO - this should be from a json definition from the scene.
        shadow = TextureFactory.createTexture(TextureType.Texture2D);
        ExternalReference ref = new ExternalReference(ExternalReference.ID_LOOKUP + DEPTH_SHADOW_NAME);
        shadow.setExternalReference(ref);
        TextureParameter texParam = new TextureParameter(
                new Parameter[] { Parameter.LINEAR, Parameter.LINEAR, Parameter.CLAMP, Parameter.CLAMP });
        ParameterData[] extra = new ParameterData[] {
                new ParameterData(Target.TEXTURE_2D, Name.TEXTURE_COMPARE_MODE, Param.COMPARE_REF_TO_TEXTURE),
                new ParameterData(Target.TEXTURE_2D, Name.TEXTURE_COMPARE_FUNC, Param.LESS) };
        texParam.setParameterData(extra);
        shadow.set(texParam);
    }

    @Override
    public void setUniformMatrices(float[][] matrices, Mesh mesh) {
        // Refresh the uniform matrix using light matrix
        System.arraycopy(matrices[Matrices.MODELVIEW.index], 0, uniforms,
                getUniformByName("uMVMatrix").getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(matrices[Matrices.PROJECTION.index], 0, uniforms,
                getUniformByName("uProjectionMatrix").getOffset(),
                Matrix.MATRIX_ELEMENTS);
        ShaderVariable lightMatrix = getUniformByName("uLightMatrix");
        System.arraycopy(matrices[Matrices.RENDERPASS_1.index], 0, uniforms,
                lightMatrix.getOffset(),
                Matrix.MATRIX_ELEMENTS);
    }

    @Override
    public void prepareTextures(GLES20Wrapper gles, Mesh mesh) throws GLException {
        int textureID = shadow.getName();
        if (textureID == Constants.NO_VALUE) {
            AssetManager.getInstance().getIdReference(shadow);
            textureID = shadow.getName();
        }
        /**
         * TODO - make texture names into enums
         */
        int unit = samplers[getUniformByName("uShadowTexture").getOffset()];
        TextureUtils.prepareTexture(gles, shadow, unit);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GLES20.GL_LESS);
        Texture2D texture = mesh.getTexture(Texture2D.TEXTURE_0);
        if (texture != null && texture.textureType != TextureType.Untextured) {
            /**
             * TODO - make texture names into enums
             */
            TextureUtils.prepareTexture(gles, texture,
                    samplers[getUniformByName("uTexture").getOffset()]);
        }
    }

    @Override
    public void initBuffers(Mesh mesh) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateUniformData(float[] destinationUniform, Mesh mesh) {
        objectProgram.updateUniformData(destinationUniform, mesh);
    }

}
