package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkPipelineMultisampleStateCreateInfo
 *
 */
public class PipelineMultisampleStateCreateInfo {
    // Reserved for future use - VkPipelineMultisampleStateCreateFlags flags;
    SampleCountFlags rasterizationSamples;
    boolean sampleShadingEnable;
    float minSampleShading;
    int[] sampleMask;
    boolean alphaToCoverageEnable;
    boolean alphaToOneEnable;
}
