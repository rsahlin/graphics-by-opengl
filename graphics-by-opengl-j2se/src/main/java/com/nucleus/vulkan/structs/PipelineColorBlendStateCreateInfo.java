package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.LogicOp;

public class PipelineColorBlendStateCreateInfo {

    // Reserved for future use - VkPipelineColorBlendStateCreateFlags flags;
    boolean logicOpEnable;
    LogicOp logicOp;
    PipelineColorBlendAttachmentState[] pAttachments;
    float blendConstants[];

}
