package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.SampleCountFlagBits;

/**
 * Wrapper for VkPipelineMultisampleStateCreateInfo
 *
 */
public class PipelineMultisampleStateCreateInfo {
    // Reserved for future use - VkPipelineMultisampleStateCreateFlags flags;
    SampleCountFlagBits rasterizationSamples;
    boolean sampleShadingEnable;
    float minSampleShading;
    int[] sampleMask;
    boolean alphaToCoverageEnable;
    boolean alphaToOneEnable;
}
