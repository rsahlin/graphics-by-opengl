package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.shader.VulkanShaderBinary;

/**
 * Wrapper for VkShaderModuleCreateInfo
 *
 */
public class ShaderModuleCreateInfo {

    private VulkanShaderBinary binary;

    public ShaderModuleCreateInfo(VulkanShaderBinary code) {
        binary = code;
    }

}
