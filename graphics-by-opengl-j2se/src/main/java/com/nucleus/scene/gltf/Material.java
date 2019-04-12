package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
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
