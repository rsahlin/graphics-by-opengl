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
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.AccessorDictionary;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Texture.TextureInfo;
import com.nucleus.shader.GLTFShaderProgram.PBRShading.PBRTextures;
import com.nucleus.texturing.TextureUtils;

public class GLTFShaderProgram extends GenericShaderProgram {

    transient protected String[] commonSourceNames = new String[] { "pbrvertex", "pbrfragment" };
    transient protected PBRShading pbrShading;

    transient protected ShaderVariable pbrDataUniform;
    transient protected ShaderVariable light0Uniform;
    transient protected float[] pbrData;
    transient protected IntBuffer samplerUniformBuffer = BufferUtils.createIntBuffer(1);
    transient private boolean renderNormalMap = false;

    /**
     * The dictionary created from linked program
     */
    protected AccessorDictionary<String> accessorDictionary = new AccessorDictionary<>();

    public static class PBRShading {
        public static final int TEXTURE_FLAG = 0x01;
        public static final int NORMAL_MAP_FLAG = 0x02;
        public static final int PBR_METALLIC_ROUGHNESS_MAP_FLAG = 0x04;
        public static final int PBR_OCCLUSION_MAP = 0x08;

        private int flags = 0;

        public enum Texturing {
            flat("flat"),
            texture_1("tex1");

            public final String name;

            private Texturing(String name) {
                this.name = name;
            }
        }

        /**
         * Additional material texture maps
         *
         */
        public enum MaterialTextures {
            none(""),
            normalMap("normal");

            public final String name;

            private MaterialTextures(String name) {
                this.name = name;
            }
        }

        /**
         * PBR texture maps
         *
         */
        public enum PBRTextures {
            none(""),
            /**
             * Metallic Roughness texture
             */
            metallicRoughness("MR"),
            /**
             * Occlusion texture
             */
            occlusion("occl");
            public final String name;

            private PBRTextures(String name) {
                this.name = name;
            }

            /**
             * Returns a string with the names from the PBRTextures - use this to get shadername to use
             * 
             * @param pbrTextures
             * @return
             */
            public static String getNames(PBRTextures[] pbrTextures) {
                StringBuffer sb = new StringBuffer();
                for (PBRTextures pbrTexture : pbrTextures) {
                    sb.append(pbrTexture.name);
                }
                return sb.toString();
            }

        }

        /**
         * Creates a new pbr shading for the primitive
         * 
         * @param primitive
         */
        public PBRShading(Primitive primitive) {
            Material mat = primitive.getMaterial();
            if (mat != null) {
                PBRMetallicRoughness pbr = mat.getPbrMetallicRoughness();
                if (pbr.getBaseColorTexture() != null) {
                    setFlag(TEXTURE_FLAG);
                    // Textured
                    if (mat.getNormalTexture() != null) {
                        setFlag(NORMAL_MAP_FLAG);
                    }
                }
            }
            if (Environment.getInstance().isProperty(Property.FORCE_UNTEXTURED, false)) {
                clearFlag(TEXTURE_FLAG);
            }
        }

        /**
         * One or more flags that are added to the builder
         * 
         * @param flag
         * @return
         */
        public PBRShading setFlag(int flag) {
            flags |= flag;
            return this;
        }

        /**
         * Checks one or more flags - all flags must be set for true to be returned
         * 
         * @param flags One or more flag values to check for
         * @return True if all set flag values are set, false otherwise
         */
        public boolean isFlags(int flags) {
            return (this.flags & flags) == flags;
        }

        /**
         * Checks if one of the flag values is set
         * 
         * @param flags One or more flag values to check for
         * @return True if one or more of the flag values are set - false if none of the flags are set
         */
        public boolean isOneOf(int flags) {
            return (this.flags & flags) != 0;
        }

        /**
         * Clears one or more flags
         * 
         * @param flag
         * @return
         */
        public PBRShading clearFlag(int flag) {
            flags ^= flag;
            return this;
        }

        /**
         * Returns additional material texturing - currently normalmap texture
         * 
         * @return The materialtexture to use
         */
        public MaterialTextures getMaterialTexture() {
            return (flags & NORMAL_MAP_FLAG) != 0 ? MaterialTextures.normalMap : MaterialTextures.none;
        }

        /**
         * Returns the main texture mode, ie if texture sampling shall be used or color shall be taken from
         * pbr basecolor
         * 
         * @return
         */
        public Texturing getTexturing() {
            return (flags & TEXTURE_FLAG) != 0 ? Texturing.texture_1 : Texturing.flat;
        }

        /**
         * Returns the pbr textures that are used
         * 
         * @return
         */
        public PBRTextures[] getPBRTextures() {
            switch (flags & (PBR_METALLIC_ROUGHNESS_MAP_FLAG | PBR_OCCLUSION_MAP)) {
                case PBR_METALLIC_ROUGHNESS_MAP_FLAG | PBR_OCCLUSION_MAP:
                    return new PBRTextures[] { PBRTextures.metallicRoughness, PBRTextures.occlusion };
                case PBR_METALLIC_ROUGHNESS_MAP_FLAG:
                    return new PBRTextures[] { PBRTextures.metallicRoughness };
                case PBR_OCCLUSION_MAP:
                    return new PBRTextures[] { PBRTextures.occlusion };
                case 0:
                    return new PBRTextures[] { PBRTextures.none };
                default:
                    throw new IllegalArgumentException("Invalid");
            }

        }

    }

    /**
     * Creates a new GLTF shaderprogram with the specified pbr shading parameters
     * 
     * @param pbrShading
     */
    public GLTFShaderProgram(PBRShading pbrShading) {
        super(null, Shading.pbr, "gltf", ProgramType.VERTEX_FRAGMENT);
        this.pbrShading = pbrShading;
        init();
    }

    private void init() {
        renderNormalMap = Environment.getInstance().isProperty(Property.RENDER_NORMALMAP, renderNormalMap);
    }

    @Override
    protected String getShaderSourceName(ShaderType type) {
        switch (type) {
            case VERTEX:
            case FRAGMENT:
                return (function.getPath(type) + function.getPassString()) +
                        pbrShading.getTexturing().name + pbrShading.getMaterialTexture().name +
                        PBRTextures.getNames(pbrShading.getPBRTextures());
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
            setUniformData(light0Uniform, l.getLight(), 0);
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
