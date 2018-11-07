package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * The pbrMetallicRoughness as it is loaded using the glTF format.
 * 
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

    private static final String BASE_COLOR_TEXTURE = "baseColorTexture";
    private static final String BASE_COLOR_FACTOR = "baseColorFactor";
    private static final String METALLIC_FACTOR = "metallicFactor";
    private static final String ROUGHNESS_FACTOR = "roughnessFactor";

    public static final float[] DEFAULT_COLOR_FACTOR = new float[] { 1, 1, 1, 1 };

    @SerializedName(BASE_COLOR_FACTOR)
    private float[] baseColorFactor = DEFAULT_COLOR_FACTOR;

    @SerializedName(METALLIC_FACTOR)
    private float metallicFactor = 1;
    @SerializedName(ROUGHNESS_FACTOR)
    private float roughnessFactor = 1f;
    @SerializedName(BASE_COLOR_TEXTURE)
    private Texture.TextureInfo baseColorTexture;

    /**
     * Store base color [4 values], metallic factor, roughness factor
     * 
     * @param pbrData
     * @param index
     */
    public void getPBR(float[] pbrData, int index) {
        pbrData[index++] = baseColorFactor[0];
        pbrData[index++] = baseColorFactor[1];
        pbrData[index++] = baseColorFactor[2];
        pbrData[index++] = baseColorFactor[3];
        pbrData[index++] = metallicFactor;
        pbrData[index] = roughnessFactor;
    }

    public final static void setDefaultPBR(float[] pbrData, int index) {
        pbrData[index++] = 1f;
        pbrData[index++] = 1f;
        pbrData[index++] = 1f;
        pbrData[index++] = 1f;
        pbrData[index++] = 1f;
        pbrData[index++] = 1f;
    }

    public float[] getBaseColorFactor() {
        return baseColorFactor;
    }

    public float getMetallicFactor() {
        return metallicFactor;
    }

    public float getRoughnessFactor() {
        return roughnessFactor;
    }

    public Texture.TextureInfo getBaseColorTexture() {
        return baseColorTexture;
    }

}
