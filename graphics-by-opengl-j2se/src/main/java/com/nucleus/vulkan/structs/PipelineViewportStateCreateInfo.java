package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkPipelineViewportStateCreateInfo
 * 
 */
public class PipelineViewportStateCreateInfo {

    // Reserved for future use - VkPipelineViewportStateCreateFlags flags;
    Viewport[] viewports;
    Rect2D pScissors;

}
