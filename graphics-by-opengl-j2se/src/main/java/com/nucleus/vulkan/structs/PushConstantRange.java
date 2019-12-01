package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.ShaderStageFlagBits;

/**
 * Wrapper for VkPushConstantRange
 *
 */
public class PushConstantRange {
    ShaderStageFlagBits[] stageFlags;
    int offset;
    int size;
}
