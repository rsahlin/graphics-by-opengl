package com.nucleus.opengl.shader;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.Environment;
import com.nucleus.common.Environment.Property;
import com.nucleus.environment.Lights;
import com.nucleus.light.Light;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.AccessorDictionary;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.Material.ShadingMaps;
import com.nucleus.scene.gltf.Material.ShadingMaps.Flags;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.scene.gltf.Texture.TextureInfo;
import com.nucleus.shader.Shader.Shading;
import com.nucleus.vecmath.Matrix;

public class GLTFShaderProgram extends GenericShaderProgram {

    transient protected String[][] commonSourceNames = new String[][] { { "common_structs.essl", "pbr" },
            { "common_structs.essl", "pbr" } };
    transient protected ShadingMaps pbrShading;

    transient protected ShaderVariable pbrDataUniform;
    transient protected ShaderVariable light0Uniform;
    transient protected ShaderVariable viewPosUniform;
    transient protected float[] pbrData;
    transient protected IntBuffer samplerUniformBuffer = BufferUtils.createIntBuffer(1);
    transient private boolean renderNormalMap = false;
    transient private boolean renderMRMap = false;

    /**
     * The dictionary created from linked program
     */
    protected AccessorDictionary<String> accessorDictionary = new AccessorDictionary<>();

    /**
     * Creates a new GLTF shaderprogram with the specified pbr shading parameters
     * 
     * @gles
     * @param pbrShading
     */
    public GLTFShaderProgram(ShadingMaps pbrShading) {
        super(null, Shading.pbr, "gltf", ProgramType.VERTEX_FRAGMENT);
        this.pbrShading = pbrShading;
        init();
    }

    private void init() {
        renderNormalMap = Environment.getInstance().isProperty(Property.RENDER_NORMALMAP, renderNormalMap);
        renderMRMap = Environment.getInstance().isProperty(Property.RENDER_MRMAP, renderMRMap);
    }

    @Override
    protected String getShaderSourceName(ShaderType type) {
        switch (type) {
            case VERTEX:
                return (function.getPath(type) + function.getPassString()) + "main";
            case FRAGMENT:
                return (function.getPath(type) + function.getPassString()) + "main";
            case COMPUTE:
                return "";
            case GEOMETRY:
                return "";
            default:
                throw new IllegalArgumentException("Not implemented for type: " + type);

        }
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
    protected String[] getCommonShaderName(Renderers version, ShaderType type) {
        switch (type) {
            case VERTEX:
                if (commonSourceNames[ShaderType.VERTEX.index] != null) {
                    String[] result = new String[commonSourceNames[ShaderType.VERTEX.index].length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = PROGRAM_DIRECTORY + getSourceNameVersion(version, type.value)
                                + function.getCategory()
                                + File.separatorChar
                                + commonSourceNames[ShaderType.VERTEX.index][i];
                    }
                    return result;
                }
                break;
            case FRAGMENT:
                if (commonSourceNames[ShaderType.FRAGMENT.index] != null) {
                    String[] result = new String[commonSourceNames[ShaderType.FRAGMENT.index].length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = PROGRAM_DIRECTORY + getSourceNameVersion(version, type.value)
                                + function.getCategory() + File.separatorChar
                                + commonSourceNames[ShaderType.FRAGMENT.index][i];
                    }
                    return result;
                }
                break;
            default:
                return null;
        }
        return null;
    }

    @Override
    protected String getDefines(ShaderType type) {
        return getDefines();
    }

    @Override
    protected ShaderSource createCommonSource(String sourceName, ShaderType type) {
        return new ShaderSource(sourceName, type);
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
            viewPosUniform = getUniformByName(Attributes._VIEWPOS.name());
        }
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {

    }

    /**
     * Update the global environment variables, camera and directional light
     * 
     * @param renderer
     * @param scene
     */
    public void updateEnvironmentUniforms(NucleusRenderer renderer, Scene scene) {
        if (light0Uniform != null) {
            Light l = Lights.getInstance().getLight();
            setUniformData(light0Uniform, l.getLight(), 0);
        }
        if (viewPosUniform != null) {
            float[] viewPos = new float[viewPosUniform.getSizeInFloats()];
            float[] cameraMatrix = scene.getCameraInstance().updateMatrix();
            Matrix.getTranslate(cameraMatrix, viewPos, 0);
            setUniformData(viewPosUniform, viewPos, 0);
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
        uploadUniform(gles, uniformData, light0Uniform);
        uploadUniform(gles, uniformData, viewPosUniform);
        // uploadUniform(gles, uniformData, viewUniform);
        // uploadUniform(gles, uniformData, projectionUniform);
    }

    @Override
    public void setSamplers() {
        ArrayList<ShaderVariable> samplersList = getSamplers(activeUniforms);
        if (samplersList.size() > 0) {
            for (int i = 0; i < samplersList.size(); i++) {

            }
        }
    }

    /**
     * Prepares a texture used before rendering starts.
     * This shall set texture parameters to used textures, ie activate texture, bind texture then set parameters.
     * 
     * @param renderer
     * @param texture
     * @throws BackendException
     */
    public void prepareTexture(NucleusRenderer renderer, GLTF gltf, Primitive primitive, ShaderVariable attribute,
            ShaderVariable texUniform, TextureInfo texInfo) throws BackendException {
        if (texInfo == null || attribute == null || texUniform == null) {
            return;
        }
        samplerUniformBuffer.put(texInfo.getIndex());
        samplerUniformBuffer.rewind();
        Accessor accessor = primitive.getAccessor(Attributes.getTextureCoord(texInfo.getTexCoord()));
        renderer.prepareTexture(gltf.getTexture(texInfo), texInfo.getIndex(), accessor, attribute, texUniform,
                samplerUniformBuffer);

    }

    /**
     * Prepares the textures needed for this primitive
     * 
     * @param gles
     * @param gltf
     * @param material
     * @throws BackendException
     */
    public void prepareTextures(NucleusRenderer renderer, GLTF gltf, Primitive primitive, Material material)
            throws BackendException {
        if (renderNormalMap && material.getNormalTexture() != null
                && material.getPbrMetallicRoughness().getBaseColorTexture() != null) {
            prepareTexture(renderer, gltf, primitive, getAttributeByName(Attributes._TEXCOORDNORMAL.name()),
                    getUniformByName("uTexture0"),
                    material.getNormalTexture());
        } else if (renderMRMap && material.getPbrMetallicRoughness().getMetallicRoughnessTexture() != null
                && material.getPbrMetallicRoughness().getBaseColorTexture() != null) {
            prepareTexture(renderer, gltf, primitive, getAttributeByName(Attributes._TEXCOORDMR.name()),
                    getUniformByName("uTexture0"),
                    material.getPbrMetallicRoughness().getMetallicRoughnessTexture());
        } else {
            prepareTexture(renderer, gltf, primitive, getAttributeByName(Attributes.TEXCOORD_0.name()),
                    getUniformByName("uTexture0"),
                    material.getPbrMetallicRoughness().getBaseColorTexture());
        }
        prepareTexture(renderer, gltf, primitive, getAttributeByName(Attributes._TEXCOORDNORMAL.name()),
                getUniformByName("uTextureNormal"), material.getNormalTexture());
        prepareTexture(renderer, gltf, primitive, getAttributeByName(Attributes._TEXCOORDMR.name()),
                getUniformByName("uTextureMR"), material.getPbrMetallicRoughness().getMetallicRoughnessTexture());
        prepareTexture(renderer, gltf, primitive, getAttributeByName(Attributes._TEXCOORDOCCLUSION.name()),
                getUniformByName("uTextureOcclusion"), material.getOcclusionTexture());
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName() + pbrShading.getFlags();
    }

    /**
     * Returns the defines to turn on PBR functions for this material
     * 
     * @return
     */
    public String getDefines() {
        StringBuffer sb = new StringBuffer();
        for (Flags f : Flags.values()) {
            if (pbrShading.isFlag(f)) {
                sb.append(ShaderSource.DEFINE + " " + f.define + " 1\n");
            } else {
                sb.append(ShaderSource.UNDEF + " " + f.define + "\n");
            }
        }
        return sb.toString();
    }

}
