#version 300 es
/* 
 * Vertex shader for normal generic vertex transform and one set of uv
 */
precision highp float;
 
//Put array declaration after name for GLSL compatibility
uniform mat4 uModelMatrix[3];

in vec3 aVertex;
in vec2 aTexCoord;

out vec2 vTexCoord;

void main() {
    vec4 pos = vec4(aVertex, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    vTexCoord = aTexCoord;
}
