package com.nucleus.shader;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.nucleus.common.BufferUtils;
import com.nucleus.common.Environment;
import com.nucleus.common.Environment.Property;
import com.nucleus.light.GlobalLight;
import com.nucleus.light.Light;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.Pass;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.AccessorDictionary;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Texture.TextureInfo;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureUtils;

public class GLTFShaderProgram extends GenericShaderProgram {

    transient protected String[] commonSourceNames = new String[] { "pbrvertex", "pbrfragment" };

    transient protected ShaderVariable pbrDataUniform;
    transient protected ShaderVariable light0Uniform;
    transient protected float[] pbrData;
    transient protected IntBuffer samplerUniformBuffer = BufferUtils.createIntBuffer(1);
    transient private boolean renderNormalMap = false;

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
        init();
    }

    public GLTFShaderProgram(Pass pass, Shading shading, String category, ProgramType shaders) {
        super(pass, shading, category, shaders);
        init();
    }

    private void init() {
        renderNormalMap = Environment.getInstance().isProperty(Property.RENDER_NORMALMAP, renderNormalMap);
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
     * @param gles
     * @param texture
     * @throws GLException
     */
    public void prepareTexture(GLES20Wrapper gles, GLTF gltf, Primitive primitive, ShaderVariable attribute,
            ShaderVariable texUniform, TextureInfo texInfo)
            throws GLException {
        if (texInfo == null || (attribute == null && texUniform == null)) {
            return;
        }
        TextureUtils.prepareTexture(gles, gltf.getTexture(texInfo), texInfo.getIndex());
        Accessor accessor = primitive.getAccessor(Attributes.getTextureCoord(texInfo.getTexCoord()));
        gles.glVertexAttribPointer(this, accessor, attribute);
        samplerUniformBuffer.put(texInfo.getIndex());
        samplerUniformBuffer.rewind();
        gles.glUniform1iv(texUniform.getLocation(), texUniform.getSize(), samplerUniformBuffer);
        GLUtils.handleError(gles, "glUniform1iv - " + attribute.getName());

    }

    /**
     * Prepares the textures needed for this primitive
     * 
     * @param gles
     * @param gltf
     * @param material
     * @throws GLException
     */
    public void prepareTextures(GLES20Wrapper gles, GLTF gltf, Primitive primitive, Material material)
            throws GLException {
        if (renderNormalMap && material.getNormalTexture() != null
                && material.getPbrMetallicRoughness().getBaseColorTexture() != null) {
            prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDNORMAL.name()),
                    getUniformByName("uTexture0"),
                    material.getNormalTexture());
        } else {
            prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes.TEXCOORD_0.name()),
                    getUniformByName("uTexture0"),
                    material.getPbrMetallicRoughness().getBaseColorTexture());
        }
        prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDNORMAL.name()),
                getUniformByName("uTextureNormal"), material.getNormalTexture());
        prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDMR.name()),
                getUniformByName("uTextureMR"), material.getPbrMetallicRoughness().getMetallicRoughnessTexture());
    }

}
