 #version 100
 /**
 * Fragment shader for convolution kernel
 * @author Richard Sahlin
 */
precision highp float;
//Put array declaration after name for GLSL compatibility
uniform mat4 uModelMatrix[1];
in vec2 aTexCoord;
in vec3 aVertex;

out vec2 vTexCoord;

void main() {
    vec4 pos = vec4(aVertex + aTranslate, 1.0) * uModelMatrix[0] * uModelMatrix[1];
    gl_Position = pos * uModelMatrix[2];
    vTexCoord = aTexCoord;
}