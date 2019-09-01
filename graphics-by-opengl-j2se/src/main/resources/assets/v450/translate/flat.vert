#version 450
/*
 * Vertex shader for normal vertex transform with position for each vertex.
*/
precision highp float;
precision highp int;
include "defines.h"

void main() {
    vec4 pos = vec4(aVertex + aTranslate, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    color = aColor;
}
