package com.nucleus.vulkan.structs;

public class ShaderStageFlagBits {
    public static final int VK_SHADER_STAGE_VERTEX_BIT = 0x00000001;
    public static final int VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT = 0x00000002;
    public static final int VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT = 0x00000004;
    public static final int VK_SHADER_STAGE_GEOMETRY_BIT = 0x00000008;
    public static final int VK_SHADER_STAGE_FRAGMENT_BIT = 0x00000010;
    public static final int VK_SHADER_STAGE_COMPUTE_BIT = 0x00000020;
    public static final int VK_SHADER_STAGE_ALL_GRAPHICS = 0x0000001F;
    public static final int VK_SHADER_STAGE_ALL = 0x7FFFFFFF;
    public static final int VK_SHADER_STAGE_RAYGEN_BIT_NV = 0x00000100;
    public static final int VK_SHADER_STAGE_ANY_HIT_BIT_NV = 0x00000200;
    public static final int VK_SHADER_STAGE_CLOSEST_HIT_BIT_NV = 0x00000400;
    public static final int VK_SHADER_STAGE_MISS_BIT_NV = 0x00000800;
    public static final int VK_SHADER_STAGE_INTERSECTION_BIT_NV = 0x00001000;
    public static final int VK_SHADER_STAGE_CALLABLE_BIT_NV = 0x00002000;
    public static final int VK_SHADER_STAGE_TASK_BIT_NV = 0x00000040;
    public static final int VK_SHADER_STAGE_MESH_BIT_NV = 0x00000080;
    public static final int VK_SHADER_STAGE_FLAG_BITS_MAX_ENUM = 0x7FFFFFFF;
}
