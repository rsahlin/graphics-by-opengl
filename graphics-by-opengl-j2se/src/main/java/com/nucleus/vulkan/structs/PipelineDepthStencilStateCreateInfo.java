package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.CompareOp;

/**
 * Wrapper For VkPipelineDepthStencilStateCreateInfo
 *
 */
public class PipelineDepthStencilStateCreateInfo {

    // Reserved for future use - VkPipelineDepthStencilStateCreateFlags flags;
    boolean depthTestEnable;
    boolean depthWriteEnable;
    CompareOp depthCompareOp;
    boolean depthBoundsTestEnable;
    boolean stencilTestEnable;
    StencilOpState front;
    StencilOpState back;
    float minDepthBounds;
    float maxDepthBounds;

}
