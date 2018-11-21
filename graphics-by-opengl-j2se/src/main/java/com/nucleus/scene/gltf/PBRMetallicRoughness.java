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
    public static final float MIN_ROUGHNESS = 0.01f;
    public static final float[] DIALECTRIC_SPECULAR_COLOR = new float[] { DIALECTRIC_SPECULAR, DIALECTRIC_SPECULAR,
            DIALECTRIC_SPECULAR };
    public static final float[] BLACK = new float[] { 0f, 0f, 0f };

    public static final int F0_INDEX = 0;
    public static final int ONE_MINUS_F0_INDEX = 4;
    public static final int DIFFUSE_INDEX = 8;
    public static final int METALLIC_INDEX = 12;
    public static final int ROUGHNESS_INDEX = 13;
    public static final int ROUGHNESS_SQUARED_INDEX = 14;

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

    transient private float[] pbrData = new float[15];

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
     * F0 as vec4
     * 1 - F0 : vec4
     * diffuse : vec4 [diffuse = cDiff / pi]
     * metallic : float
     * roughness : float
     * roughness ^2 : float
     * 
     * cdiff = lerp(baseColor.rgb * (1 - dielectricSpecular.r), black, metallic)
     * F0 = lerp(dielectricSpecular, baseColor.rgb, metallic)
     * α = roughness ^ 2
     */

    public void calculatePBRData() {
        roughnessFactor = Math.max(MIN_ROUGHNESS, roughnessFactor);

        Lerp.lerpVec3(DIALECTRIC_SPECULAR_COLOR, baseColorFactor, metallicFactor, pbrData, F0_INDEX);
        pbrData[F0_INDEX + 3] = baseColorFactor[3];

        pbrData[ONE_MINUS_F0_INDEX] = 1 - pbrData[F0_INDEX];
        pbrData[ONE_MINUS_F0_INDEX + 1] = 1 - pbrData[F0_INDEX + 1];
        pbrData[ONE_MINUS_F0_INDEX + 2] = 1 - pbrData[F0_INDEX + 2];
        pbrData[ONE_MINUS_F0_INDEX + 3] = baseColorFactor[3];

        float[] diffuse = new float[3];
        diffuse[0] = (float) (baseColorFactor[0] / Math.PI);
        diffuse[1] = (float) (baseColorFactor[1] / Math.PI);
        diffuse[2] = (float) (baseColorFactor[2] / Math.PI);

        Lerp.lerpVec3(diffuse, BLACK, metallicFactor, pbrData, DIFFUSE_INDEX);

        pbrData[METALLIC_INDEX] = metallicFactor;
        pbrData[ROUGHNESS_INDEX] = roughnessFactor;
        pbrData[ROUGHNESS_SQUARED_INDEX] = roughnessFactor * roughnessFactor;
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
