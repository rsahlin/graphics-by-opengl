package com.nucleus.shader;

import java.nio.FloatBuffer;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Constants;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.texturing.ParameterData;
import com.nucleus.texturing.Texture2D;
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

    protected ShaderVariable lightUniform;

    static class Shadow2Categorizer extends Categorizer {

        public Shadow2Categorizer(Pass pass, ShaderProgram.Shading shading, String category) {
            super(pass, shading, category);
        }

        @Override
        public String getShaderSourceName(ShaderType type) {
            switch (type) {
                case FRAGMENT:
                    // For fragment shader ignore the category
                    return getPassString() + getShadingString();
                default:
                    return super.getShaderSourceName(type);
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
    public ShadowPass2Program(ShaderProgram objectProgram, Pass pass, String category, ShaderProgram.Shading shading,
            ShaderProgram.ProgramType shaders) {
        super(objectProgram, new Shadow2Categorizer(Pass.SHADOW2, shading, category), shaders);
        setIndexer(
                objectProgram.variableIndexer != null ? objectProgram.variableIndexer : objectProgram.createIndexer());
        // This defines the texture parameters for the shadow pass.
        // TODO - this should be from a json definition from the scene.
        shadow = TextureFactory.getInstance().createTexture(TextureType.Texture2D);
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
    public void setUniformMatrices(float[][] matrices) {
        if (modelUniform == null) {
            modelUniform = getUniformByName(Matrices.MODEL.name);
            lightUniform = getUniformByName("uLightMatrix");
        }
        uniforms.position(modelUniform.getOffset());
        uniforms.put(matrices[Matrices.MODEL.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.VIEW.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.PROJECTION.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.position(lightUniform.getOffset());
        uniforms.put(matrices[Matrices.RENDERPASS_1.index], 0, Matrix.MATRIX_ELEMENTS);
    }

    @Override
    public void prepareTexture(GLES20Wrapper gles, Texture2D texture) throws GLException {
        int textureID = shadow.getName();
        if (textureID == Constants.NO_VALUE) {
            AssetManager.getInstance().getIdReference(shadow);
            textureID = shadow.getName();
        }
        /**
         * TODO - make texture names into enums
         */
        int unit = samplers.get(getUniformByName("uShadowTexture").getOffset());
        TextureUtils.prepareTexture(gles, shadow, unit);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
        gles.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GLES20.GL_LESS);
        if (texture != null && texture.textureType != TextureType.Untextured) {
            /**
             * TODO - make texture names into enums
             */
            TextureUtils.prepareTexture(gles, texture,
                    samplers.get(getUniformByName("uTexture").getOffset()));
        }
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
        objectProgram.updateUniformData(destinationUniform);
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
    }

}
