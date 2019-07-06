#include "defines.h"
/**
 * Functions for gltf PBR model
 */ 

layout(binding = 0) uniform sampler2D uTexture0; //The texture 0 sampler
layout(binding = 1) uniform sampler2D uTextureNormal; //normal texture sampler
layout(binding = 2) uniform sampler2D uTextureMR; //Metallic roughness texture sampler
layout(binding = 3) uniform sampler2D uTextureOcclusion; //Occlusion texture sampler

layout(location = TEXCOORD0_LOCATION) in vec2 vTexCoord0;
layout(location = TEXNORMAL_LOCATION) in vec2 vTexNormal;
layout(location = TEXMR_LOCATION) in vec2 vTexMR;
layout(location = TEXOCCL_LOCATION) in vec2 vTexOccl;

layout(location = WORLDPOS_LOCATION) in vec4 vWorldPos;
layout(location = TANGENTLIGHT_LOCATION) in mat3 mTangentLight;
layout(location = MATERIAL_LOCATION) in Material material;
layout(location = LIGHT_LOCATION) in Light light;

layout(location = FRAGCOLOR_LOCATION) out vec4 fragColor;

/**
 * Returns the BRDF to use for materials that have per vertex properties, ie not normal/metallicrough/occlusion map
 */
BRDF getPerVertexBRDF() {
    BRDF brdf;
    brdf.normal = material.normal;
    brdf.NdotL = material.NdotL;
    brdf.NdotH = material.NdotH;
    brdf.NdotV = material.NdotV;
    brdf.mro.b = pbrdata._PBRDATA[0].x;
    brdf.mro.g = pbrdata._PBRDATA[0].y;
    brdf.mro.r = 1.0;
    return brdf;
}

BRDF getPerPixelBRDF(const vec3 normal) {
    BRDF brdf;
    brdf.normal = normal;
    brdf.NdotL = clamp(dot(normal, light.direction), 0.0, 1.0);
    brdf.NdotH = clamp(dot(normal, material.H), 0.0, 1.0);
    brdf.NdotV = abs(dot(normal, light.eye)) + 1e-5;
    return brdf;
}

/**
 * Takes the incoming pbr calculated pixel, applies exposure and gamma correction then writes to fragColor
 * using the alpha from the materials diffuse color.
 * @param pbr The pbr calculated pixel - including alpha from texture if used.
 */
void outputPixel(vec4 pbr) {
    vec3 hdrExposure = vec3(1.0) - exp(-(pbr.rgb) * pbrdata._PBRDATA[0].z);
    fragColor = vec4(pow(hdrExposure, vec3(pbrdata._PBRDATA[0].w)), pbrdata._PBRDATA[3].a); 
}

vec3 F_Schlick(const vec3 F0, const BRDF brdf) {
    return F0 + (1.0 - F0) * pow(1.0 - material.VdotH, 5.0);
}

float V_SmithGGXCorrelated(const float linearRoughness, const BRDF brdf) {
    // Heitz 2014, "Understanding the Masking-Shadowing Function in Microfacet-Based BRDFs"
    float a2 = linearRoughness * linearRoughness;
    // TODO: lambdaV can be pre-computed for all the lights, it should be moved out of this function
    float lambdaV = brdf.NdotL * sqrt((brdf.NdotV - a2 * brdf.NdotV) * brdf.NdotV + a2);
    float lambdaL = brdf.NdotV * sqrt((brdf.NdotL - a2 * brdf.NdotL) * brdf.NdotL + a2);
    float v = 0.5 / (lambdaV + lambdaL);
    // a2=0 => v = 1 / 4*NoL*NoV   => min=1/4, max=+inf
    // a2=1 => v = 1 / 2*(NoL+NoV) => min=1/4, max=+inf
    // clamp to the maximum value representable in mediump
    return clamp(v, 0.0, 1.0);
}


Shading D_GGX(const float linearRoughness, const BRDF brdf) {
    vec3 NxH = cross(brdf.normal, material.H);
    float a = brdf.NdotH * linearRoughness;
    float k = linearRoughness / (dot(NxH, NxH) + a * a);
    float d = k * k * (1.0 / pi);
    Shading shading;
    shading.NDF = max(d, 0.0);
    return shading;
}

Shading D_GLTF(float roughness, const BRDF brdf) {
    vec3 NxH = cross(brdf.normal, material.H);
    float a = roughness * roughness;
    float a2 = a * a;
    Shading shading;
    shading.specularLobe = 1.0 - dot(NxH, NxH);
    shading.NDF = ((a2) / (pi * pow((shading.specularLobe * (a2 - 1.0) + 1.0),2.0)));
    return shading;
}

float V_GLTF(const float roughness, const BRDF brdf) {
    float k = roughness * sqrtTwoByPi;
    float GL = clamp(material.LdotH / (material.LdotH * (1.0 - k) + k), 0.0, 1.0);
    float GN = clamp(brdf.NdotH /(brdf.NdotH * (1.0 - k) + k), 0.0, 1.0);
    return (GL * GN) / max(1.0, 4.0 * brdf.NdotL * brdf.NdotV);
}

vec3 Diffuse_GLTF(vec3 fresnel, vec3 cDiff) {
    return (1.0 - fresnel) * cDiff;
}


/**
 * cdiff = lerp(baseColor.rgb * (1 - dielectricSpecular.r), black, metallic) 
 * F0 = lerp(dielectricSpecular, baseColor.rgb, metallic) 
 * α = roughness ^ 2
 * F = F0 + (1 - F0) * (1.0 - V * H)^5
 * diffuse = (1 - F) * cdiff
 */
vec3[2] internalCalculatePBR(const BRDF brdf, const vec3 F0, const vec3 cDiff, const float roughness) {
    float a = roughness * roughness;
    vec3 fresnel = F_Schlick(F0, brdf);
    Shading shading = D_GLTF(roughness, brdf);
    float V = V_GLTF(roughness, brdf);
    vec3[2] result;
    vec3 specular = fresnel * shading.NDF * V;
    vec3 diffuse = (1.0 - fresnel) * cDiff * oneByPI;
    vec3 illumination = light.color.rgb * (light.color.a * brdf.NdotL);
    float NdotH = brdf.NdotH;
    // TODO - what is the value for the cutoff from specular color to tint?
    float step = smoothstep(SPECULAR_LOBE_TINT, 1.0, (shading.specularLobe) * (1.0 - a * a) * (1.0 - brdf.mro.b * brdf.mro.b));
    result[0] = (diffuse + mix(black, specular, 1.0 - step)) * illumination;
    result[1] = mix(black, specular, step) * illumination;
    return result;
    
}

/**
 * Calculate BRDF using values that are set in vertex shader.
 * This is used for materials that do not have normal/metallicrough/occlusion texture maps
 */
vec3[2] calculateFresnelDiffuse(const BRDF brdf) {
    return internalCalculatePBR(brdf, pbrdata._PBRDATA[1].rgb, pbrdata._PBRDATA[2].rgb, max(MIN_ROUGHNESS, pbrdata._PBRDATA[0].y));
}

/**
 * cdiff = lerp(baseColor.rgb * (1 - dielectricSpecular.r), black, metallic) 
 * F0 = lerp(dielectricSpecular, baseColor.rgb, metallic) 
 * α = roughness ^ 2
 * F = F0 + (1 - F0) * (1.0 - V * H)^5
 */
vec3[2] calculateFresnelDiffuse(in BRDF brdf, const vec3 metallicRoughness) {
    float roughness = max(MIN_ROUGHNESS,pbrdata._PBRDATA[0].y * metallicRoughness.g);
    float metal =  clamp(pbrdata._PBRDATA[0].x * metallicRoughness.b, MIN_METALLIC, MAX_METALLIC);
    vec3 F0 = mix(dielectricSpecular, pbrdata._PBRDATA[3].rgb, metal);
    brdf.mro.b = metal;
    brdf.mro.g = roughness;
    brdf.mro.r = metallicRoughness.r;
    return internalCalculatePBR(brdf, F0, pbrdata._PBRDATA[3].rgb, roughness);
}

vec3[2] calculateFresnelDiffuse(in BRDF brdf, const vec2 metallicRoughness) {
    float roughness = max(MIN_ROUGHNESS,pbrdata._PBRDATA[0].y * metallicRoughness.r);
    float metal =  clamp(pbrdata._PBRDATA[0].x * metallicRoughness.g, MIN_METALLIC, MAX_METALLIC);
    vec3 F0 = mix(dielectricSpecular, pbrdata._PBRDATA[3].rgb, metal);
    brdf.mro.b = metal;
    brdf.mro.g = roughness;
    return internalCalculatePBR(brdf, F0, pbrdata._PBRDATA[3].rgb, roughness);
}


vec3[2] calculateFresnelDiffuse(in BRDF brdf, const float occlusion) {
    brdf.mro.r = occlusion;
    return internalCalculatePBR(brdf,  pbrdata._PBRDATA[1].rgb, pbrdata._PBRDATA[2].rgb, max(MIN_ROUGHNESS, pbrdata._PBRDATA[0].y));
}



