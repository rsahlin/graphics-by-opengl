package com.nucleus.scene.gltf;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.Environment;
import com.nucleus.common.Environment.Property;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.scene.gltf.Texture.NormalTextureInfo;

/**
 * The Material as it is loaded using the glTF format.
 * 
 * The material appearance of a primitive.
 * 
 * Properties
 * 
 * Type Description Required
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * pbrMetallicRoughness object A set of parameter values that are used to define the metallic-roughness material model
 * from Physically-Based Rendering (PBR) methodology. When not specified, all the default values of pbrMetallicRoughness
 * apply. No
 * normalTexture object The normal map texture. No
 * occlusionTexture object The occlusion map texture. No
 * emissiveTexture object The emissive map texture. No
 * emissiveFactor number [3] The emissive color of the material. No, default: [0,0,0]
 * alphaMode string The alpha rendering mode of the material. No, default: "OPAQUE"
 * alphaCutoff number The alpha cutoff value of the material. No, default: 0.5
 * doubleSided boolean Specifies whether the material is double sided. No, default: false
 * 
 * This class can be serialized using gson
 */
public class Material extends GLTFNamedValue implements RuntimeResolver {

    public final static AlphaMode DEFAULT_ALPHA_MODE = AlphaMode.OPAQUE;
    public final static float DEFAULT_ALPHA_CUTOFF = 0.5f;
    public final static boolean DEFAULT_DOUBLE_SIDED = false;
    public final static float[] DEFAULT_EMISSIVE_FACTOR = new float[] { 0, 0, 0 };

    private static final String NAME = "name";
    private static final String PBR_METALLIC_ROUGHNESS = "pbrMetallicRoughness";
    private static final String NORMAL_TEXTURE = "normalTexture";
    private static final String OCCLUSION_TEXTURE = "occlusionTexture";
    private static final String EMISSIVE_FACTOR = "emissiveFactor";
    private static final String ALPHA_MODE = "alphaMode";
    private static final String ALPHA_CUTOFF = "alphaCutoff";
    private static final String DOUBLE_SIDED = "doubleSided";

    public enum AlphaMode {
        OPAQUE(),
        MASK(),
        BLEND();
    }

    @SerializedName(PBR_METALLIC_ROUGHNESS)
    private PBRMetallicRoughness pbrMetallicRoughness;
    @SerializedName(NORMAL_TEXTURE)
    private Texture.NormalTextureInfo normalTexture;
    @SerializedName(OCCLUSION_TEXTURE)
    private Texture.OcclusionTextureInfo occlusionTexture;
    @SerializedName(EMISSIVE_FACTOR)
    private float[] emissiveFactor = DEFAULT_EMISSIVE_FACTOR;
    @SerializedName(ALPHA_MODE)
    private AlphaMode alphaMode = DEFAULT_ALPHA_MODE;
    @SerializedName(ALPHA_CUTOFF)
    private float alphaCutoff = DEFAULT_ALPHA_CUTOFF;
    @SerializedName(DOUBLE_SIDED)
    private boolean doubleSided = DEFAULT_DOUBLE_SIDED;

    /**
     * Keep track what shading maps are used for normals, metallic roughness and occlusion map.
     * To mark a map as needed by the material call {@link #addFlag(Flags)}
     * Query by calling {@link #isFlag(Flags)}
     *
     */
    public static class ShadingMaps {
        public static final String TEXTURE_DEFINE = "TEXTURE";
        public static final String NORMAL_MAP_DEFINE = "NORMAL_MAP";
        public static final String METALROUGH_MAP_DEFINE = "METALROUGH_MAP";
        public static final String OCCLUSION_MAP_DEFINE = "OCCLUSION_MAP";

        /**
         * The flags that can be set to define what texture maps are used by a material
         */
        public enum Flags {

            TEXTURE(TextureMaps.none, TEXTURE_DEFINE),
            NORMAL_MAP(TextureMaps.normalMap, NORMAL_MAP_DEFINE),
            PBR_MR_MAP(TextureMaps.metallicRoughness, METALROUGH_MAP_DEFINE),
            PBR_OCCLUSION_MAP(TextureMaps.occlusion, OCCLUSION_MAP_DEFINE);

            public final TextureMaps texture;
            public final String define;

            Flags(TextureMaps texture, String define) {
                this.texture = texture;
                this.define = define;
            }

        }

        private List<Flags> flags = new ArrayList<>();

        /**
         * What basecolor textures are used, ie the number of color texture units (and samplers) used
         *
         */
        public enum Texturing {
            flat("flat"),
            texture_1("tex1");

            public final String name;

            private Texturing(String name) {
                this.name = name;
            }
        }

        /**
         * What texture maps are used by a material
         *
         */
        public enum TextureMaps {
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

            private TextureMaps(String name) {
                this.name = name;
            }

            /**
             * Returns a string with the names from the PBRTextures - use this to get shadername to use
             * 
             * @param pbrTextures
             * @return
             */
            public static String getNames(TextureMaps[] pbrTextures) {
                StringBuffer sb = new StringBuffer();
                for (TextureMaps pbrTexture : pbrTextures) {
                    sb.append(pbrTexture.name);
                }
                return sb.toString();
            }

        }

        /**
         * Creates a new pbr shading for the material
         * 
         * @param material
         */
        public ShadingMaps(Material material) {
            if (material != null) {
                PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
                if (pbr.getBaseColorTexture() != null) {
                    addFlag(Flags.TEXTURE);
                    // Textured
                    if (material.getNormalTexture() != null &&
                            !Environment.getInstance().isProperty(Property.FORCE_NO_NORMALMAP, false)) {
                        addFlag(Flags.NORMAL_MAP);
                    }
                    if (pbr.getMetallicRoughnessTexture() != null
                            && !Environment.getInstance().isProperty(Property.FORCE_NO_METALLICROUGHNESSMAP, false)) {
                        addFlag(Flags.PBR_MR_MAP);
                    }
                    if (material.getOcclusionTexture() != null
                            && !Environment.getInstance().isProperty(Property.FORCE_NO_NOOCCLUSIONMAP, false)) {
                        addFlag(Flags.PBR_OCCLUSION_MAP);
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
        public ShadingMaps addFlag(Flags flag) {
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
        public ShadingMaps clearFlag(Flags flag) {
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
         * @return Texturing mode for material
         */
        public Texturing getTexturing() {
            return isFlag(Flags.TEXTURE) ? Texturing.texture_1 : Texturing.flat;
        }

        /**
         * Returns the set flags as a text string.
         * 
         * @return
         */
        public String getFlags() {
            StringBuffer sb = new StringBuffer();
            for (Flags f : Flags.values()) {
                if (isFlag(f)) {
                    sb.append(f.name());
                }
            }
            return sb.toString();
        }

        /**
         * Returns the pbr textures that are used
         * 
         * @return
         */
        public TextureMaps[] getPBRTextures() {
            if (flags.size() == 0) {
                return new TextureMaps[] { TextureMaps.none };
            }
            TextureMaps[] textures = new TextureMaps[flags.size()];

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
     * Returns the pbr object
     * 
     * @return
     */
    public PBRMetallicRoughness getPbrMetallicRoughness() {
        return pbrMetallicRoughness;
    }

    /**
     * Returns the normal texture, if defined
     * 
     * @return Normal texture, or null if not defined
     */
    public NormalTextureInfo getNormalTexture() {
        return normalTexture;
    }

    /**
     * Returns the occlusion texture if defined
     * 
     * @return Occlusion texture, or null if not defined
     */
    public Texture.OcclusionTextureInfo getOcclusionTexture() {
        return occlusionTexture;
    }

    /**
     * Returns the emissive color of the material
     * 
     * @return
     */
    public float[] getEmissiveFactor() {
        return emissiveFactor;
    }

    /**
     * Returns the alpha rendering mode of the material
     * 
     * @return
     */
    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    /**
     * Returns the alpha cutoff value of the material
     * 
     * @return
     */
    public float getAlphaCutoff() {
        return alphaCutoff;
    }

    /**
     * Returns true if this is a doublesided material
     * 
     * @return
     */
    public boolean isDoubleSided() {
        return doubleSided;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {

    }

}
