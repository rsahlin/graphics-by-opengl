#version 300 es
 /**
 * Fragment shader for flat (unshaded) rendering
 */
precision mediump float;

in vec2 vTexCoord;
in vec4 color;

out vec4 fragColor;

void main()
{
    fragColor = color;
}
