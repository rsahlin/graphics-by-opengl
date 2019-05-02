#version 300
 /**
 * Fragment shader for 3 X 3 convolution kernel
 * @author Richard Sahlin
 */

precision mediump float;
uniform sampler2D uTexture;      //The texture sampler
uniform mat3 uKernel[3];    //0=kernel, 1=uoffset, 2=voffset
in vec2 vTexCoord;
out vec4 fragColor;

void main()
{
    vec3 pixel = vec3(texture2D(uTexture, vTexCoord + vec2(uKernel[1][0][0],uKernel[2][0][0])) * uKernel[0][0][0] + 
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][0][1],uKernel[2][0][1])) * uKernel[0][0][1] + 
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][0][2],uKernel[2][0][2])) * uKernel[0][0][2] +
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][1][0],uKernel[2][1][0])) * uKernel[0][1][0] + 
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][1][1],uKernel[2][1][1])) * uKernel[0][1][1] + 
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][1][2],uKernel[2][1][2])) * uKernel[0][1][2] +
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][2][0],uKernel[2][2][0])) * uKernel[0][2][0] + 
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][2][1],uKernel[2][2][1])) * uKernel[0][2][1] + 
        texture2D(uTexture, vTexCoord + vec2(uKernel[1][2][2],uKernel[2][2][2])) * uKernel[0][2][2]);
    fragColor = vec4(pixel, 1.0);
}
