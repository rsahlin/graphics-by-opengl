package com.nucleus.vulkan;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

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

    @Override
    public void copy(Object destination) {
        VkPhysicalDeviceFeatures dest = (VkPhysicalDeviceFeatures) destination;
        dest.tessellationShader(tessellationShader);
        dest.geometryShader(geometryShader);
        dest.textureCompressionASTC_LDR(textureCompressionASTC_LDR);
        dest.textureCompressionBC(textureCompressionBC);
        dest.textureCompressionETC2(textureCompressionETC2);
    }

}