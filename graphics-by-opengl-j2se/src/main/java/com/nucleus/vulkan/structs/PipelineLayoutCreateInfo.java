package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkPipelineLayoutCreateInfo
 *
 */
public class PipelineLayoutCreateInfo {
    // Reserved for future use - VkPipelineLayoutCreateFlags flags;
    DescriptorSetLayout[] setLayouts;
    PushConstantRange[] pushConstantRanges;

}
