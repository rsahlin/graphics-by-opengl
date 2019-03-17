package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;
import com.nucleus.vecmath.Lerp;

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

    public static final float DIALECTRIC_SPECULAR = 0.04f;
    public static final float[] DIALECTRIC_SPECULAR_COLOR = new float[] { DIALECTRIC_SPECULAR, DIALECTRIC_SPECULAR,
            DIALECTRIC_SPECULAR };
    public static final float[] BLACK = new float[] { 0f, 0f, 0f };

    public static final int METALLIC_INDEX = 0;
    public static final int ROUGHNESS_INDEX = 1;

    public static final int F0_INDEX = 4;
    public static final int CDIFF_INDEX = 8;
    public static final int DIFFUSE_INDEX = 12;
    public static final int K_INDEX = 16;
    public static final int ALPHA_SQUARED_INDEX = 17;
    public static final int EXPOSURE_INDEX = 18;
    public static final int GAMMA_INDEX = 19;
    public static final int PBR_DATASIZE = 20;

    private static final String BASE_COLOR_TEXTURE = "baseColorTexture";
    private static final String BASE_COLOR_FACTOR = "baseColorFactor";
    private static final String METALLIC_FACTOR = "metallicFactor";
    private static final String ROUGHNESS_FACTOR = "roughnessFactor";
    private static final String METALLIC_ROUGHNESS_TEXTURE = "metallicRoughnessTexture";

    public static final float[] DEFAULT_COLOR_FACTOR = new float[] { 1, 1, 1, 1 };

    @SerializedName(BASE_COLOR_FACTOR)
    private float[] baseColorFactor = DEFAULT_COLOR_FACTOR;

    @SerializedName(METALLIC_FACTOR)
    private float metallicFactor = 1;
    @SerializedName(ROUGHNESS_FACTOR)
    private float roughnessFactor = 1f;
    @SerializedName(BASE_COLOR_TEXTURE)
    private Texture.TextureInfo baseColorTexture;
    @SerializedName(METALLIC_ROUGHNESS_TEXTURE)
    private Texture.TextureInfo metallicRoughnessTexture;

    transient private float[] pbrData = new float[PBR_DATASIZE];
    transient private static float exposure = 1f;
    transient private static float oneByGamma = 1.0f / 2.2f;

    /**
     * Copies precomputed bpr data into array - call {@link #calculatePBRData()} before calling this method.
     * 
     * @param pbrData
     * @param index
     */
    public void getPBR(float[] pbrData, int index) {
        System.arraycopy(this.pbrData, 0, pbrData, index, this.pbrData.length);
    }

    /**
     * Precomputes the pbr data - call this method once at start or when pbr parameters have changed.
     * metallic, roughness : float
     * F0 as vec3
     * 1 - F0 : vec3
     * diffuse : vec4
     * k, alpha^2, exposure, gamma
     */

    public void calculatePBRData() {

        Lerp.lerpVec3(DIALECTRIC_SPECULAR_COLOR, baseColorFactor, metallicFactor, pbrData, F0_INDEX);

        float[] diffuse = new float[3];
        diffuse[0] = (baseColorFactor[0] * (1 - DIALECTRIC_SPECULAR));
        diffuse[1] = (baseColorFactor[1] * (1 - DIALECTRIC_SPECULAR));
        diffuse[2] = (baseColorFactor[2] * (1 - DIALECTRIC_SPECULAR));

        System.arraycopy(baseColorFactor, 0, pbrData, DIFFUSE_INDEX, 4);
        Lerp.lerpVec3(diffuse, BLACK, metallicFactor, pbrData, CDIFF_INDEX);

        pbrData[METALLIC_INDEX] = metallicFactor;
        pbrData[ROUGHNESS_INDEX] = roughnessFactor;
        pbrData[K_INDEX] = (float) (roughnessFactor * Math.sqrt(2 / Math.PI));
        float rSquared = roughnessFactor * roughnessFactor;
        pbrData[ALPHA_SQUARED_INDEX] = rSquared * rSquared;
        pbrData[EXPOSURE_INDEX] = exposure;
        pbrData[GAMMA_INDEX] = oneByGamma;
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

    public Texture.TextureInfo getMetallicRoughnessTexture() {
        return metallicRoughnessTexture;
    }

    /**
     * Sets the exposure value, this will change the exposure for all PBR metallic roughness objects.
     * 
     * @param exposure
     */
    public static void setExposure(float exposure) {
        PBRMetallicRoughness.exposure = exposure;
    }

    /**
     * Sets the gamma correction value, this will change the value for all PBR metallic roughness objects
     * 
     * @param gamma
     */
    public static void setGamma(float gamma) {
        PBRMetallicRoughness.oneByGamma = 1.0f / gamma;
    }

}
