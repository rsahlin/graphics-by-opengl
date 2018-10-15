package com.nucleus.shader;

import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.AccessorDictionary;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.texturing.Texture2D.Shading;

public class GLTFShaderProgram extends GenericShaderProgram {

    transient protected ShaderVariable color0Uniform;
    transient protected ShaderVariable light0Uniform;

    /**
     * The dictionary created from linked program
     */
    protected AccessorDictionary<String> accessorDictionary = new AccessorDictionary<>();

    public GLTFShaderProgram(Pass pass, Shading shading, String category, ProgramType shaders) {
        super(pass, shading, category, shaders);
    }

    /**
     * Returns the program accessor dictionary, this is created after linking the program and stores accessors
     * using shader variable name.
     * 
     * @return
     */
    public AccessorDictionary<String> getAccessorDictionary() {
        return accessorDictionary;
    }

    @Override
    public void updateUniformData(float[] destinationUniform) {
        if (color0Uniform == null) {
            color0Uniform = getUniformByName(Attributes.COLOR_0.name());
            light0Uniform = getUniformByName(Attributes._LIGHT_0.name());
        }
        setUniformData(light0Uniform, GlobalLight.getInstance().getLightPosition(), 0);
    }

    public void updatePrimitiveUniforms(GLES20Wrapper gles, Primitive primitive) throws GLException {
        Material material = primitive.getMaterial();
        if (material != null) {
            setUniformData(color0Uniform, material.getPbrMetallicRoughness().getBaseColorFactor(), 0);
        } else {
            setUniformData(color0Uniform, PBRMetallicRoughness.DEFAULT_COLOR_FACTOR, 0);
        }
        uploadUniform(gles, uniforms, color0Uniform);
    }

    @Override
    public void updateUniforms(GLES20Wrapper gles, float[][] matrices)
            throws GLException {
        // GLTF will likely have multiple primitives for the same program within one node - split update of matrices.
        setUniformMatrices(matrices);
        updateUniformData(uniforms);
        uploadUniforms(gles, uniforms, activeUniforms);
    }

    /**
     * Upload the uniform matrices to GL
     * 
     * @param gles
     * @param uniformData
     * @param activeUniforms
     * @throws GLException
     */
    @Override
    protected void uploadUniforms(GLES20Wrapper gles, float[] uniformData, ShaderVariable[] activeUniforms)
            throws GLException {
        uploadUniform(gles, uniformData, modelUniform);
        uploadUniform(gles, uniformData, viewUniform);
        uploadUniform(gles, uniformData, projectionUniform);
        uploadUniform(gles, uniformData, light0Uniform);
    }

}
