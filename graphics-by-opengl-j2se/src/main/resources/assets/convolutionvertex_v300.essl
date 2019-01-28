 #version 300 es
 /**
 * Fragment shader for convolution kernel
 * @author Richard Sahlin
 */
precision highp float;
uniform mat4 uMVPMatrix;
attribute vec3 aTranslate;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;

void main() {
    gl_Position = vec4(aTranslate,1.0) * uMVPMatrix;
    vTexCoord = aTexCoord;
}
