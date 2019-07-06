#version 100
/* 
 * Vertex shader for normal generic vertex transform and a color for flat shading
 * @author Richard Sahlin
 */

//Put array declaration after name for GLSL compatibility
uniform mat4 uModelMatrix[3];

attribute vec3 aVertex;
attribute vec4 aColor;

varying vec4 color;

void main() {
    vec4 pos = vec4(aVertex, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    color = aColor;
}
