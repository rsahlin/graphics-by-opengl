#version 300 es
#line 3
/* 
 * Vertex shader for textured glTF asset without texture, with normal map and possibly metallicRoughness
 */
 precision highp float;
 
//Put array declaration after name for GLSL compatibility

void main() {
#ifdef NORMAL_MAP
    positionLightTexNormal();
#else
    positionLight();
#endif
}