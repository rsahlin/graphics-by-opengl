#version 450
/* 
 * Fragment shader for tiled sprite renderer
 */
precision mediump float;
precision mediump int;

#include "layout_frag.glsl"

void main()
{
    fragColor = texture(uTexture, vTexCoord);
}
