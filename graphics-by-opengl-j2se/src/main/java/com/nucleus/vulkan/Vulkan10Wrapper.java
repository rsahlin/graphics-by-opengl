package com.nucleus.vulkan;

import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.QueueFamilyProperties.QueueFlagBits;
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
        QueueFamilyProperties queueFamily = selectQueueInstance(selected);

        createLogicalDevice(selected, queueFamily);

        createQueues(queueFamily);

    }

    /**
     * Called by the constructor to fetch physical devices in system.
     * 
     * @return
     */
    protected abstract PhysicalDevice[] fetchDevices();

    /**
     * Creates the logical device using the selected queue, this method is with the result of
     * {@link #selectDevice(PhysicalDevice[])} {@link #selectQueueInstance(PhysicalDevice)} has b
     * 
     * @param device The device returned by {@link #selectDevice(PhysicalDevice[])}
     * @param selectedQueue The queue family returned by {@link #selectQueueInstance(PhysicalDevice)}
     */
    protected abstract void createLogicalDevice(PhysicalDevice device, QueueFamilyProperties selectedQueue);

    protected abstract void createQueues(QueueFamilyProperties selectedQueue);

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
        for (QueueFamilyProperties qp : device.getQueueFamilyProperties()) {
            if ((qp.surfaceSupportsPresent) && qp.hasSupport(QueueFlagBits.VK_QUEUE_GRAPHICS_BIT)) {
                return qp;
            }
        }
        return null;
    }

}
