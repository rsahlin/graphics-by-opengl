#version 300 es
 /**
 * Fragment shader for shadow pass, textured
 */
precision mediump float;

uniform lowp sampler2D uTexture; //The texture sampler
uniform lowp sampler2DShadow uShadowTexture;

in vec2 vTexCoord;
in vec4 vLightPos;

out vec4 fragColor;

void main()
{
    float shadow = texture(uShadowTexture, vec3(vLightPos.xyz));
    fragColor = texture(uTexture, vTexCoord) * vec4(shadow,shadow,shadow,1.0);
}
