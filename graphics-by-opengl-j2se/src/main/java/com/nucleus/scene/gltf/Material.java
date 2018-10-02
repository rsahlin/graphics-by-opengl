package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

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
public class Material {

    public final static AlphaMode DEFAULT_ALPHA_MODE = AlphaMode.OPAQUE;
    public final static float DEFAULT_ALPHA_CUTOFF = 0.5f;
    public final static boolean DEFAULT_DOUBLE_SIDED = false;
    public final static float[] DEFAULT_EMISSIVE_FACTOR = new float[] { 0, 0, 0 };

    public enum AlphaMode {
        OPAQUE(),
        MASK(),
        BLEND();
    }

    private static final String NAME = "name";
    private static final String PBR_METALLIC_ROUGHNESS = "pbrMetallicRoughness";

    @SerializedName(NAME)
    private String name;
    @SerializedName(PBR_METALLIC_ROUGHNESS)
    private PBRMetallicRoughness pbrMetallicRoughness;
    private float[] emissiveFactor = DEFAULT_EMISSIVE_FACTOR;
    private AlphaMode alphaMode = DEFAULT_ALPHA_MODE;
    private float alphaCutoff = DEFAULT_ALPHA_CUTOFF;
    private boolean doubleSided = DEFAULT_DOUBLE_SIDED;

    public String getName() {
        return name;
    }

    public PBRMetallicRoughness getPbrMetallicRoughness() {
        return pbrMetallicRoughness;
    }

    public float[] getEmissiveFactor() {
        return emissiveFactor;
    }

    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    public float getAlphaCutoff() {
        return alphaCutoff;
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }

}
