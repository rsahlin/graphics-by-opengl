package com.nucleus.shader;

import java.io.File;
import java.nio.FloatBuffer;

import com.nucleus.light.GlobalLight;
import com.nucleus.light.Light;
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

    transient protected String[] commonSourceNames = new String[] { "pbrvertex", "pbrfragment" };

    transient protected ShaderVariable pbrDataUniform;
    transient protected ShaderVariable light0Uniform;
    transient protected float[] pbrData;
    /**
     * The dictionary created from linked program
     */
    protected AccessorDictionary<String> accessorDictionary = new AccessorDictionary<>();

    /**
     * 
     * @param source
     * @param pass
     * @param shading
     * @param category
     * @param shaders
     * @param commonSources Common vertex/fragment shader or null
     */
    public GLTFShaderProgram(String[] source, Pass pass, Shading shading, String category, ProgramType shaders,
            String[] commonSources) {
        super(source, pass, shading, category, shaders);
        commonSourceNames[0] = commonSources[0];
        commonSourceNames[1] = commonSources[1];
    }

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
    protected String[] getCommonShaderName(ShaderType type) {
        switch (type) {
            case VERTEX:
                if (commonSourceNames[ShaderType.VERTEX.index] != null) {
                    return new String[] {
                            PROGRAM_DIRECTORY + function.getCategory() + File.separatorChar
                                    + commonSourceNames[ShaderType.VERTEX.index] };
                }
                break;
            case FRAGMENT:
                if (commonSourceNames[ShaderType.FRAGMENT.index] != null) {
                    return new String[] {
                            PROGRAM_DIRECTORY + function.getCategory() + File.separatorChar
                                    + commonSourceNames[ShaderType.FRAGMENT.index] };
                }
                break;
            default:
                return null;
        }
        return null;
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
        // Init may be called several times
        if (pbrDataUniform == null) {
            pbrDataUniform = getUniformByName(Attributes._PBRDATA.name());
            if (pbrDataUniform != null) {
                // Will be null in vector debug shader
                pbrData = new float[pbrDataUniform.getSizeInFloats()];
            }
            light0Uniform = getUniformByName(Attributes._LIGHT_0.name());
        }
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
        if (light0Uniform != null) {
            Light l = GlobalLight.getInstance().getLight();
            setUniformData(light0Uniform, l.getPosition(), 0);
        }
    }

    /**
     * Read uniforms from material for the primitive and upload.
     * 
     * @param gles
     * @param primitive
     * @throws GLException
     */
    public void updatePBRUniforms(GLES20Wrapper gles, Primitive primitive) throws GLException {
        Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            pbr.calculatePBRData();
            pbr.getPBR(pbrData, 0);
        }
        setUniformData(pbrDataUniform, pbrData, 0);
        uploadUniform(gles, uniforms, pbrDataUniform);
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
