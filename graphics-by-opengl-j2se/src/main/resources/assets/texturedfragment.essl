#version 100
/* 
 * Vertex shader for normal vertex transform with position for each vertex.
 * Can be used for instance for line drawing
 */
precision mediump float;

uniform sampler2D uTexture;      //The texture sampler
varying vec2 vTexCoord;

void main()
{
    gl_FragColor = texture2D(uTexture, vTexCoord);
}
