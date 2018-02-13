#version 310 es
layout(std430) buffer; // Sets the default layout for SSBOs
layout (local_size_x = 8, local_size_y = 8, local_size_z = 1) in;


struct AttribData {
      vec4 vec;
};

// Declare input/output buffer from/to wich we will read/write data.
// In this particular shader we only write data into the buffer.
// If you do not want your data to be aligned by compiler try to use:
// packed or shared instead of std140 keyword.
// We also bind the buffer to index 0. You need to set the buffer binding
// in the range [0..3] – this is the minimum range approved by Khronos.
// Notice that various platforms might support more indices than that.

layout(std430, binding = 0) writeonly buffer Output {
    AttribData data[];
} outBuffer;

layout(std430, binding = 1) readonly buffer Input {
    AttribData data[];
} inBuffer;


// glDispatchCompute is called from the application.
void main() {
      // Read current global position for this thread
      uint offset = gl_GlobalInvocationID.x * gl_GlobalInvocationID.y * gl_GlobalInvocationID.z;
      for (int i = 0; i < 4; i++) {
          outBuffer.data[offset].vec = inBuffer.data[offset].vec;
          offset += 4u;
      }
}
