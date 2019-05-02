#version 300 es
/* 
 * Vertex shader for textured glTF asset with texture and normal map
 */
 precision highp float;
 
//Put array declaration after name for GLSL compatibility

void main() {
    positionLightTexNormal();
}