package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.PipelineCreateFlagBits;

/**
 * Wrapper for VkGraphicsPipelineCreateInfo
 *
 */
public class GraphicsPipelineCreateInfo {

    PipelineCreateFlagBits[] flags;
    int stageCount;
    PipelineShaderStageCreateInfo[] stages;
    PipelineVertexInputStateCreateInfo vertexInputState;
    PipelineInputAssemblyStateCreateInfo inputAssemblyState;
    PipelineTesselationStateCreateInfo tesselationState;
    PipelineViewportStateCreateInfo viewportState;
    PipelineRasterizationStateCreateInfo rasterizationState;
    PipelineMultisampleStateCreateInfo multisampleState;
    PipelineDepthStencilStateCreateInfo depthStencilState;
    PipelineColorBlendStateCreateInfo colorBlendState;
    PipelineDynamicStateCreateInfo dynamicState;
    PipelineLayoutCreateInfo layoutInfo;
    // VkPipelineLayout layout;
    RenderPassCreateInfo renderPassInfo;
    // VkRenderPass renderPass;
    int subpass;
    // VkPipeline basePipelineHandle;
    int basePipelineIndex;

}
