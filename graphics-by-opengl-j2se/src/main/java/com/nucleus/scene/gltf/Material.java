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
    private float[] emissiveFactor = new float[] { 0, 0, 0 };
    private AlphaMode alphaMode = AlphaMode.OPAQUE;
    private float alphaCutoff = 0.5f;
    private boolean doubleSided = false;

    public String getName() {
        return name;
    }

    public PBRMetallicRoughness getPbrMetallicRoughness() {
        return pbrMetallicRoughness;
    }

}
