#version 450
precision highp float;
 
#include "common_structs.essl"
#include "pbr.vert"

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