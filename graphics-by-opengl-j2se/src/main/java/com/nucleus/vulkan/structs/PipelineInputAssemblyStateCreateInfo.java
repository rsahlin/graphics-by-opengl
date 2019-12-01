package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.PrimitiveTopology;

public class PipelineInputAssemblyStateCreateInfo {

    // Reserverd for future use VkPipelineInputAssemblyStateCreateFlags flags;
    PrimitiveTopology topology;
    boolean primitiveRestartEnable;
}
