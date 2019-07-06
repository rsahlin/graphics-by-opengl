package com.nucleus.vulkan;

import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class LWJGLVkQueueFamilyProperties extends com.nucleus.vulkan.structs.QueueFamilyProperties {

    public LWJGLVkQueueFamilyProperties(org.lwjgl.vulkan.VkQueueFamilyProperties queueProperties, int index,
            VkPhysicalDevice device, long surface) {
        queueIndex = index;
        queueFlags = queueProperties.queueFlags();
        queueCount = queueProperties.queueCount();
        VkExtent3D minImage = queueProperties.minImageTransferGranularity();
        minImageTransferGranularity = new com.nucleus.vulkan.structs.Extent3D(minImage.width(),
                minImage.height(), minImage.depth());
        timestampValidBits = queueProperties.timestampValidBits();
        int[] supported = new int[1];
        if (KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, index, surface, supported) != VK10.VK_SUCCESS) {
            throw new IllegalArgumentException("Failed to get device surface support");
        }
        surfaceSupportsPresent = (supported[0] == VK10.VK_TRUE);
    }

}
