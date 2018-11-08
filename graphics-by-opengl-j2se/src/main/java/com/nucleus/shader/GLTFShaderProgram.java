package com.nucleus.shader;

import java.nio.FloatBuffer;

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

    transient protected ShaderVariable pbrDataUniform;
    transient protected ShaderVariable light0Uniform;
    transient protected float[] pbrData;
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
    public void initUniformData(FloatBuffer destinationUniforms) {
        // Init may be called several times
        if (pbrDataUniform == null) {
            pbrDataUniform = getUniformByName(Attributes._PBRDATA.name());
            pbrData = new float[pbrDataUniform.getSizeInFloats()];
            light0Uniform = getUniformByName(Attributes._LIGHT_0.name());
        }
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
        if (light0Uniform != null) {
            setUniformData(light0Uniform, GlobalLight.getInstance().getLightPosition(), 0);
        }
    }

    /**
     * Read uniforms from material for the primitive and upload.
     * 
     * @param gles
     * @param primitive
     * @throws GLException
     */
    public void updatePrimitiveUniforms(GLES20Wrapper gles, Primitive primitive) throws GLException {
        Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            pbr.calculatePBRData();
            pbr.getPBR(pbrData, 0);
        }
        setUniformData(pbrDataUniform, pbrData, 0);
        uploadUniform(gles, uniforms, pbrDataUniform);
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
    protected void uploadUniforms(GLES20Wrapper gles, FloatBuffer uniformData, ShaderVariable[] activeUniforms)
            throws GLException {
        uploadUniform(gles, uniformData, modelUniform);
        // uploadUniform(gles, uniformData, viewUniform);
        // uploadUniform(gles, uniformData, projectionUniform);
        // uploadUniform(gles, uniformData, light0Uniform);
    }

}
