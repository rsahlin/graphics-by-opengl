/**
 * Source for structs shared between vertex and fragment stage (possibly other stages)
 * Currently a naming based locations are used in favour of numbered layouts - this is simply because
 * keeping track of locations is cumbersome as long as number of fields/parameters change.
 *
 * DO NOT ADD DEPENDENCY TO defines here - this file shall be included BEFORE defines.
 *
 */
 
const float MIN_ROUGHNESS = 0.03;
const float MIN_METALLIC = 0.0;
const float MAX_METALLIC = 1.0;
// Mixes diffuse->specular channel
const float SPECULAR_LOBE_TINT = 0.85;
const float pi = 3.141592;
const float sqrtTwoByPi = sqrt(2.0 / pi);
const float oneByPI = 1.0 / pi;
const float twoByPI = 2.0 / pi;
const float gamma = 2.2;
const float oneByGamma = 1.0 / gamma;
const vec3 dielectricSpecular = vec3(0.04, 0.04, 0.04);
const vec3 black = vec3(0.0, 0.0, 0.0);
 
/**
 * PBR material properties, if possible calculate on a per vertex basis
 */ 
struct Material {
    // Per vertex attributes
    vec3 normal;
    vec3 H;
    float NdotH;
    float NdotL;
    float NdotV;
    float VdotH;
    float LdotH;
};
#define MATERIAL_LOCATIONS 7

/**
 * Light properties, if possible calculate on a per vertex basis
 */
struct Light {
    // Location of eye
    vec3 eye;
    // Direction vector of light
    vec3 direction;
    // Color and intensity of light
    vec4 color;
};
#define LIGHT_LOCATIONS 3
/**
 * Data needed when shading using the BRDF, used in fragment step
 * Normally calculated per fragment
 */
struct BRDF {
    vec3 normal;
    //Todo store as Vec to simplify setting.
    float NdotH;
    float NdotL;
    float NdotV;
    // Fresnel part
    vec3 F0;
    // Diffuse part (1 - F0) * baseColor
    vec3 diffuse;
    // Metallic, roughness, occlusion
    vec3 mro;
};

/**
 * BRDF shading variables, calculated within the BRDF
 */
struct Shading {
    // Specular lobe (fallof)
    float specularLobe;
    // Normal distribution function
    float NDF;
};
