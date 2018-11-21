#version 300 es
 /**
 * Fragment shader for shadow pass of untextured 
 */
precision mediump float;

uniform lowp sampler2DShadow uShadowTexture;

in vec4 lightPos;
in vec4 color;

out vec4 fragColor;

void main()
{
    float shadow = texture(uShadowTexture, vec3(lightPos.xy,gl_FragCoord.z));
    fragColor = vec4(color.rgb * shadow, 1);
}
