package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.CullModeFlagBits;
import com.nucleus.vulkan.Vulkan10.FrontFace;
import com.nucleus.vulkan.Vulkan10.PolygonMode;

/**
 * Wrapper for VkPipelineRasterizationStateCreateInfo
 *
 */
public class PipelineRasterizationStateCreateInfo {

    // Reserved for future use - VkPipelineRasterizationStateCreateFlags flags;
    boolean depthClampEnable;
    boolean rasterizerDiscardEnable;
    PolygonMode polygonMode;
    CullModeFlagBits cullMode;
    FrontFace frontFace;
    boolean depthBiasEnable;
    float depthBiasConstantFactor;
    float depthBiasClamp;
    float depthBiasSlopeFactor;
    float lineWidth;

}
