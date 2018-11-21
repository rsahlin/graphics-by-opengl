#version 300 es
 /**
 * Vertex shader for points
 */

precision highp float;

//Put array declaration after name for GLSL compatibility
uniform mat4 uModelMatrix[3];

in vec3 aVertex; //Contains xyz
in vec4 aColor;

out vec4 color;

/**
 * Used for objects that uses only position - processed as points
 */
void main() {
    vec4 pos = vec4(aVertex + aTranslate, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    color = aColor;
}
