#version 320 es
precision highp float;

/**
 * Geometry shader to output untextured quads
 */

//Put array declaration after name for GLSL compatibility

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

in vec4 vColor[]; // Output from vertex shader for each vertex
in vec4 vRect[];
in vec4 vLightPos[];

out vec4 color; // Output to fragment shader
out vec4 lightPos; //Output to fragment shader


/**
 * Used for objects that uses only position, plus color, for instance lines
 */
void main() {
    color = vColor[0];
    lightPos = vLightPos[0];
    
    gl_Position = gl_in[0].gl_Position + vec4(vRect[0].x,vRect[0].y,0.0,0.0);
    EmitVertex();
    gl_Position = gl_in[0].gl_Position + vec4(vRect[0].x,vRect[0].y + vRect[0].w,0.0,0.0);
    EmitVertex();
    gl_Position = gl_in[0].gl_Position + vec4(vRect[0].x + vRect[0].z,vRect[0].y,0.0,0.0);
    EmitVertex();
    gl_Position = gl_in[0].gl_Position + vec4(vRect[0].x + vRect[0].z,vRect[0].y + vRect[0].w,0.0,0.0);
    EmitVertex();
    EndPrimitive();
}
