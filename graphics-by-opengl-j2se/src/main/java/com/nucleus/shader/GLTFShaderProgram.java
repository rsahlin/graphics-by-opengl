package com.nucleus.shader;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.nucleus.common.BufferUtils;
import com.nucleus.common.Environment;
import com.nucleus.common.Environment.Property;
import com.nucleus.environment.Lights;
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
import com.nucleus.scene.gltf.Scene;
import com.nucleus.scene.gltf.Texture.TextureInfo;
import com.nucleus.shader.GLTFShaderProgram.PBRShading.PBRTextures;
import com.nucleus.texturing.TextureUtils;
import com.nucleus.vecmath.Matrix;

public class GLTFShaderProgram extends GenericShaderProgram {

    transient protected String[][] commonSourceNames = new String[][] { { "common_structs.essl", "pbr_v300" },
            { "common_structs.essl", "pbr_v300" } };
    transient protected PBRShading pbrShading;

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
     * Handles the PBR texture map parameters for normals, metallic roughness and occlusion map.
     * Used to return name of shader handling the PBR material used.
     *
     */
    public static class PBRShading {
        public static final String TEXTURE_DEFINE = "TEXTURE";
        public static final String NORMAL_MAP_DEFINE = "NORMAL_MAP";
        public static final String METALROUGH_MAP_DEFINE = "METALROUGH_MAP";
        public static final String OCCLUSION_MAP_DEFINE = "OCCLUSION_MAP";

        public enum Flags {

            TEXTURE(PBRTextures.none, TEXTURE_DEFINE),
            NORMAL_MAP(PBRTextures.normalMap, NORMAL_MAP_DEFINE),
            PBR_METALLIC_ROUGHNESS_MAP(PBRTextures.metallicRoughness, METALROUGH_MAP_DEFINE),
            PBR_OCCLUSION(PBRTextures.occlusion, OCCLUSION_MAP_DEFINE);

            public final PBRTextures texture;
            public final String define;

            Flags(PBRTextures texture, String define) {
                this.texture = texture;
                this.define = define;
            }

        }

        private List<Flags> flags = new ArrayList<>();

        public enum Texturing {
            flat("flat"),
            texture_1("tex1");

            public final String name;

            private Texturing(String name) {
                this.name = name;
            }
        }

        /**
         * Texture maps
         *
         */
        public enum PBRTextures {
            none(""),
            /**
             * Normal map texture
             */
            normalMap("normal"),
            /**
             * Metallic Roughness texture
             */
            metallicRoughness("mr"),
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
                    setFlag(Flags.TEXTURE);
                    // Textured
                    if (mat.getNormalTexture() != null &&
                            !Environment.getInstance().isProperty(Property.FORCE_NO_NORMALMAP, false)) {
                        setFlag(Flags.NORMAL_MAP);
                    }
                    if (pbr.getMetallicRoughnessTexture() != null
                            && !Environment.getInstance().isProperty(Property.FORCE_NO_METALLICROUGHNESSMAP, false)) {
                        setFlag(Flags.PBR_METALLIC_ROUGHNESS_MAP);
                    }
                    if (mat.getOcclusionTexture() != null
                            && !Environment.getInstance().isProperty(Property.FORCE_NO_NOOCCLUSIONMAP, false)) {
                        setFlag(Flags.PBR_OCCLUSION);
                    }
                }
            }
            if (Environment.getInstance().isProperty(Property.FORCE_UNTEXTURED, false)) {
                clearFlag(Flags.TEXTURE);
            }
        }

        /**
         * Add a flag
         * 
         * @param flag The flag to set
         * @return
         */
        public PBRShading setFlag(Flags flag) {
            if (!flags.contains(flag)) {
                flags.add(flag);
            }
            return this;
        }

        /**
         * Clears a flag
         * 
         * @param flag Flag to clear
         * @return
         */
        public PBRShading clearFlag(Flags flag) {
            if (flags.contains(flag)) {
                flags.remove(flag);
            }
            return this;
        }

        /**
         * Returns true if the specified flag is set
         * 
         * @param flag
         * @return
         */
        public boolean isFlag(Flags flag) {
            return flags.contains(flag);
        }

        /**
         * Returns the main texture mode, ie if texture sampling shall be used or color shall be taken from
         * pbr basecolor
         * 
         * @return
         */
        public Texturing getTexturing() {
            return isFlag(Flags.TEXTURE) ? Texturing.texture_1 : Texturing.flat;
        }

        /**
         * Returns the defines to turn on PBR functions for this material
         * 
         * @return
         */
        public String getDefines() {
            StringBuffer sb = new StringBuffer();
            for (Flags f : Flags.values()) {
                if (isFlag(f)) {
                    sb.append(ShaderSource.DEFINE + " " + f.define + " 1\n");
                }
            }
            return sb.toString();
        }

        /**
         * Returns the pbr textures that are used
         * 
         * @return
         */
        public PBRTextures[] getPBRTextures() {
            if (flags.size() == 0) {
                return new PBRTextures[] { PBRTextures.none };
            }
            PBRTextures[] textures = new PBRTextures[flags.size()];

            int index = 0;
            for (Flags f : Flags.values()) {
                if (isFlag(f)) {
                    textures[index++] = f.texture;
                }
            }
            return textures;
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
        renderMRMap = Environment.getInstance().isProperty(Property.RENDER_MRMAP, renderMRMap);
    }

    @Override
    protected String getShaderSourceName(ShaderType type) {
        switch (type) {
            case VERTEX:
                return (function.getPath(type) + function.getPassString()) +
                        pbrShading.getTexturing().name;
            case FRAGMENT:
                return (function.getPath(type) + function.getPassString()) +
                        pbrShading.getTexturing().name +
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
                    String[] result = new String[commonSourceNames[ShaderType.VERTEX.index].length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = PROGRAM_DIRECTORY + function.getCategory() + File.separatorChar
                                + commonSourceNames[ShaderType.VERTEX.index][i];
                    }
                    return result;
                }
                break;
            case FRAGMENT:
                if (commonSourceNames[ShaderType.FRAGMENT.index] != null) {
                    String[] result = new String[commonSourceNames[ShaderType.FRAGMENT.index].length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = PROGRAM_DIRECTORY + function.getCategory() + File.separatorChar
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
        return pbrShading.getDefines();
    }

    @Override
    protected ShaderSource createCommonSource(String sourceName, String sourceNameVersion, ShaderType type) {
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
     * @param gles
     * @param scene
     */
    public void updateEnvironmentUniforms(GLES20Wrapper gles, Scene scene) {
        if (light0Uniform != null) {
            Light l = Lights.getInstance().getLight();
            setUniformData(light0Uniform, l.getLight(), 0);
        }
        if (viewPosUniform != null) {
            float[] viewPos = new float[] { 0, 0, 0 };
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
     * @param gles
     * @param texture
     * @throws GLException
     */
    public void prepareTexture(GLES20Wrapper gles, GLTF gltf, Primitive primitive, ShaderVariable attribute,
            ShaderVariable texUniform, TextureInfo texInfo)
            throws GLException {
        if (texInfo == null || attribute == null || texUniform == null) {
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
        } else if (renderMRMap && material.getPbrMetallicRoughness().getMetallicRoughnessTexture() != null
                && material.getPbrMetallicRoughness().getBaseColorTexture() != null) {
            prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDMR.name()),
                    getUniformByName("uTexture0"),
                    material.getPbrMetallicRoughness().getMetallicRoughnessTexture());
        } else {
            prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes.TEXCOORD_0.name()),
                    getUniformByName("uTexture0"),
                    material.getPbrMetallicRoughness().getBaseColorTexture());
        }
        prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDNORMAL.name()),
                getUniformByName("uTextureNormal"), material.getNormalTexture());
        prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDMR.name()),
                getUniformByName("uTextureMR"), material.getPbrMetallicRoughness().getMetallicRoughnessTexture());
        prepareTexture(gles, gltf, primitive, getAttributeByName(Attributes._TEXCOORDOCCLUSION.name()),
                getUniformByName("uTextureOcclusion"), material.getOcclusionTexture());
    }

}
