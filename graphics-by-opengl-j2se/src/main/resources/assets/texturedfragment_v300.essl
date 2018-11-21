#version 300 es
/* 
 * Fragment shader for tiled sprite renderer
 */
precision mediump float;

uniform lowp sampler2D uTexture;      //The texture sampler
in vec2 vTexCoord;
out vec4 fragColor;

void main()
{
    fragColor = texture(uTexture, vTexCoord);
}
