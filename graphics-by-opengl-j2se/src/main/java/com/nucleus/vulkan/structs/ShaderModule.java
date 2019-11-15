package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkShaderModule
 *
 */
public class ShaderModule {

    private ShaderModuleCreateInfo info;
    private long shaderModule;

    public ShaderModule(ShaderModuleCreateInfo info, long shaderModule) {
        this.info = info;
        this.shaderModule = shaderModule;
    }

}
