package com.nucleus.vulkan;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.nucleus.vulkan.VulkanWrapper.PhysicalDeviceFeatures;

public class LWJGLVulkanDeviceFeatures extends PhysicalDeviceFeatures {

    public LWJGLVulkanDeviceFeatures(VkPhysicalDevice device, long surface) {
        VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.malloc();
        VK10.vkGetPhysicalDeviceFeatures(device, deviceFeatures);

        tessellationShader = deviceFeatures.tessellationShader();
        geometryShader = deviceFeatures.geometryShader();
        textureCompressionASTC_LDR = deviceFeatures.textureCompressionASTC_LDR();
        textureCompressionBC = deviceFeatures.textureCompressionBC();
        textureCompressionETC2 = deviceFeatures.textureCompressionETC2();
    }

}