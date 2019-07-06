package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.VulkanWrapper;
import com.nucleus.vulkan.VulkanWrapper.APIVersion;

/**
 * Abstraction of VkPhysicalDeviceProperties for physical device properties
 *
 */
public interface PhysicalDeviceProperties {

    public enum PhysicalDeviceType {
        VK_PHYSICAL_DEVICE_TYPE_OTHER(0),
        VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1),
        VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2),
        VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3),
        VK_PHYSICAL_DEVICE_TYPE_CPU(4);

        public final int value;

        private PhysicalDeviceType(int value) {
            this.value = value;
        }

        public static PhysicalDeviceType get(int value) {
            for (PhysicalDeviceType type : PhysicalDeviceType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

    };

    /**
     * Returns the device type
     * 
     * @return The VkPhysicalDeviceType of the device
     */
    public PhysicalDeviceType getDeviceType();

    /**
     * Returns the name of the device
     * 
     * @return
     */
    public String getDeviceName();

    /**
     * Returns the API version as defined in VkPhysicalDevice
     * 
     * @return
     */
    public APIVersion getAPIVersion();

    /**
     * Returns the device limits
     * 
     * @return
     */
    public DeviceLimits getLimits();

}