#version 450
precision mediump float;
precision mediump sampler2D;

#include "common_structs.glsl"
#include "layout_frag.glsl"
#include "pbr_frag.glsl"
/* 
 * Fragment shader for glTF asset
 * @author Richard Sahlin
 */

void main() {
#ifdef NORMAL_MAP
    // Scale of normal map not supported yet - ideally this should only be done if scale != 1.0, eg
    // #ifdef NORMAL_SCALE 
    // normalize((<sampled normal texture value> * 2.0 - 1.0) * vec3(<normal scale>, <normal scale>, 1.0))
    // #endif
    BRDF brdf = getPerPixelBRDF(normalize(vec3(texture(uTextureNormal, vTexNormal) * 2.0 - 1.0) * mTangentLight));
#else
    BRDF brdf = getPerVertexBRDF();
#endif

#ifdef METALROUGH_MAP
    vec3[2] diffuseSpecular = calculateFresnelDiffuse(brdf, vec2(texture(uTextureMR, vTexMR).gb));
#else
#ifdef OCCLUSION_MAP
    vec3[2] diffuseSpecular = calculateFresnelDiffuse(brdf, texture(uTextureOcclusion, vTexOccl).r);
#else
    vec3[2] diffuseSpecular = calculateFresnelDiffuse(brdf);
#endif
#endif 

#ifdef TEXTURE
    outputPixel(vec4(diffuseSpecular[0] * texture(uTexture0, vTexCoord0).rgb + diffuseSpecular[1], 1.0));
#else
    outputPixel(vec4(diffuseSpecular[0] + diffuseSpecular[1], 1.0));
#endif
}