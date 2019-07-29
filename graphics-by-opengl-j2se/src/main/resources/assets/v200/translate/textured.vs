#version 100
/* 
 * Vertex shader for normal vertex transform with position for each vertex.
 * Can be used for instance for line drawing
 */
//Put array declaration after name for GLSL compatibility
uniform mat4 uModelMatrix[3];

attribute vec3 aTranslate; //Position of vertex, offset from matrix
attribute vec3 aVertex;
attribute vec2 aTexCoord;

varying vec2 vTexCoord;

void main() {
    vec4 pos = vec4(aVertex + aTranslate, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    vTexCoord = aTexCoord;
    
}
