package com.nucleus.vulkan;

import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.Vulkan10.Extensions;
import com.nucleus.vulkan.VulkanWrapper.VulkanDeviceSelector;

/**
 * Wrapper for Vulkan version 1.0 funtionality
 *
 */
public abstract class Vulkan10Wrapper extends VulkanWrapper implements VulkanDeviceSelector {

    private PhysicalDevice[] devices;

    /**
     * Will call {@link #fetchDevices()} and then {@link #selectDevice(PhysicalDevice[])}
     * 
     * @param version
     */
    protected Vulkan10Wrapper(Renderers version) {
        super(version);
    }

    /**
     * This method MUST be called by subclasses - after they have done initialization. This method will
     * trigger the device fetch and selection.
     */
    protected void init() {
        devices = fetchDevices();
        if (devices == null || devices.length == 0) {
            throw new IllegalArgumentException("Failed to fetch devices");
        }
        SimpleLogger.d(getClass(), "Found " + devices.length + " devices.");
        for (int i = 0; i < devices.length; i++) {
            SimpleLogger.d(getClass(), "Found device: " + devices[i]);
        }

        PhysicalDevice selected = selectDevice(devices);
        if (selected == null) {
            throw new IllegalArgumentException("No suitable Vulkan physical device");
        }
    }

    /**
     * Called by the constructor to fetch physical devices in system.
     * 
     * @return
     */
    protected abstract PhysicalDevice[] fetchDevices();

    @Override
    public PhysicalDevice selectDevice(PhysicalDevice[] devices) {
        for (PhysicalDevice d : devices) {
            if (d.getExtension(Extensions.VK_KHR_swapchain.name()) != null) {
                for (QueueFamilyProperties qp : d.getQueueFamilyProperties()) {
                    if (qp.isSurfaceSupportsPresent()) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public QueueFamilyProperties selectQueueInstance(PhysicalDevice device) {
        return device.getQueueFamilyProperties()[0];
    }

}
