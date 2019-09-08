#version 450
precision highp float;

#include "common_structs.glsl"
#include "layout_vert.glsl" 
#include "pbr_vert.glsl"

/* 
 * Vertex shader for textured glTF asset without texture, with normal map and possibly metallicRoughness
 */

void main() {
#ifdef NORMAL_MAP
    positionLightTexNormal();
#else
    positionLight();
#endif
}