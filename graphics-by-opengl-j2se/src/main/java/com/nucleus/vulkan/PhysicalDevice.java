package com.nucleus.vulkan;

/**
 * Abstraction VkPhysicalDevice for Khronos Vulkan physical device
 *
 */
public interface PhysicalDevice {

    /**
     * Returns the physical device properties
     * 
     * @return
     */
    public PhysicalDeviceProperties getProperties();

    /**
     * Returns the physical device features
     * 
     * @return
     */
    public PhysicalDeviceFeatures getFeatures();

    /**
     * Returns the supported queue families of the device
     * 
     * @return
     */
    public QueueFamilyProperties[] getQueueFamilyProperties();

    /**
     * Returns the supported extensions
     * 
     * @return
     */
    public ExtensionProperties[] getExtensions();

    /**
     * Returns the extension property if this device supportes the named extension
     * 
     * @param extensionName
     * @return The extension property, or null if not supported
     */
    public ExtensionProperties getExtension(String extensionName);

}