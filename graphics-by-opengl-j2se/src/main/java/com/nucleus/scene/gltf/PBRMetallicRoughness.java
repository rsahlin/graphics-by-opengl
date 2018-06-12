package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * pbrMetallicRoughness
 * A set of parameter values that are used to define the metallic-roughness material model from Physically-Based
 * Rendering (PBR) methodology.
 * 
 * Properties
 * 
 * Type Description Required
 * baseColorFactor number [4] The material's base color factor. No, default: [1,1,1,1]
 * baseColorTexture object The base color texture. No
 * metallicFactor number The metalness of the material. No, default: 1
 * roughnessFactor number The roughness of the material. No, default: 1
 * metallicRoughnessTexture object The metallic-roughness texture. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 * This class can be serialized using gson
 */
public class PBRMetallicRoughness {

    private static final String BASE_COLOR_FACTOR = "baseColorFactor";
    private static final String METALLIC_FACTOR = "metallicFactor";
    private static final String ROUGHNESS_FACTOR = "roughnessFactor";

    @SerializedName(BASE_COLOR_FACTOR)
    private float[] baseColorFactor = new float[] { 1, 1, 1, 1 };

    @SerializedName(METALLIC_FACTOR)
    private float metallicFactor = 1;
    @SerializedName(ROUGHNESS_FACTOR)
    private float roughnessFactor = 1f;

    public float[] getBaseColorFactor() {
        return baseColorFactor;
    }

    public float getMetallicFactor() {
        return metallicFactor;
    }

    public float getRoughnessFactor() {
        return roughnessFactor;
    }

}
