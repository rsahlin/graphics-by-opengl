package com.nucleus.vulkan;

import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import com.nucleus.vulkan.VulkanWrapper.PhysicalDeviceFeatures;
import com.nucleus.vulkan.VulkanWrapper.VkQueueFlagBits;

public class LWJGLVulkanDeviceFeatures extends PhysicalDeviceFeatures {

    private int[] surfaceSupportPresent;
    private VkQueueFamilyProperties.Buffer queueProperties;

    public LWJGLVulkanDeviceFeatures(VkPhysicalDevice device, long surface) {
        VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.malloc();
        VK10.vkGetPhysicalDeviceFeatures(device, deviceFeatures);

        tessellationShader = deviceFeatures.tessellationShader();
        geometryShader = deviceFeatures.geometryShader();
        textureCompressionASTC_LDR = deviceFeatures.textureCompressionASTC_LDR();
        textureCompressionBC = deviceFeatures.textureCompressionBC();
        textureCompressionETC2 = deviceFeatures.textureCompressionETC2();

        readQueueProperties(device, surface);

    }

    protected void readQueueProperties(VkPhysicalDevice device, long surface) {
        int[] queueCount = new int[1];
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueCount, null);
        queueProperties = VkQueueFamilyProperties.malloc(queueCount[0]);
        surfaceSupportPresent = new int[queueCount[0]];
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueCount, queueProperties);

        int[] supported = new int[1];
        for (int i = 0; i < surfaceSupportPresent.length; i++) {
            if (KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, supported) != VK10.VK_SUCCESS) {
                throw new IllegalArgumentException("Failed to get device surface support");
            }
            surfaceSupportPresent[i] = supported[0];
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "Queue support: \n";
        queueProperties.rewind();
        for (int present : surfaceSupportPresent) {
            result += VkQueueFlagBits.toString(queueProperties.get().queueFlags()) + " - surface present: " +
                    (present == VK10.VK_TRUE) + "\n";
        }
        return result;
    }

}