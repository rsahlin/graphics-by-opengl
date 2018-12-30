#version 300 es
 /**
 */

precision highp float;

//Put array declaration after name for GLSL compatibility
uniform mat4 uModelMatrix[3];

in vec3 POSITION;
in vec3 NORMAL; //Normals
in vec3 TANGENT; //Input array with vectors
in vec3 BITANGENT;

out vec4 vNormal;
out vec4 vTangent;
out vec4 vBitangent;

/**
 * Used to debug computed vectors, for instance the tangent, bitangent, normal vectors.
 * 
 */
void main() {
    //TODO - this shall be same calculations as in pbrvertex
    vec4 pos = vec4(POSITION, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    
    vNormal = vec4(vec4(NORMAL, 0.0) * uModelMatrix[0] * uModelMatrix[1] * uModelMatrix[2]);
    vTangent = vec4(vec4(TANGENT, 0.0) * uModelMatrix[0] * uModelMatrix[1] * uModelMatrix[2]);
    vBitangent = vec4(vec4(BITANGENT, 0.0) * uModelMatrix[0] * uModelMatrix[1] * uModelMatrix[2]);
}

