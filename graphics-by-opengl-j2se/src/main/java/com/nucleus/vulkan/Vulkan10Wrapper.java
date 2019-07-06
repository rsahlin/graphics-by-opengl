package com.nucleus.vulkan;

import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.Vulkan10.ColorSpaceKHR;
import com.nucleus.vulkan.Vulkan10.Extensions;
import com.nucleus.vulkan.Vulkan10.Format;
import com.nucleus.vulkan.Vulkan10.PresentModeKHR;
import com.nucleus.vulkan.Vulkan10.SurfaceFormat;
import com.nucleus.vulkan.VulkanWrapper.VulkanDeviceSelector;
import com.nucleus.vulkan.structs.Extent2D;
import com.nucleus.vulkan.structs.ImageView;
import com.nucleus.vulkan.structs.ImageViewCreateInfo;
import com.nucleus.vulkan.structs.PhysicalDeviceMemoryProperties;
import com.nucleus.vulkan.structs.QueueFamilyProperties;
import com.nucleus.vulkan.structs.QueueFamilyProperties.QueueFlagBits;
import com.nucleus.vulkan.structs.SwapChain;
import com.nucleus.vulkan.structs.PhysicalDeviceProperties.PhysicalDeviceType;

/**
 * Wrapper for Vulkan version 1.0 funtionality
 *
 */
public abstract class Vulkan10Wrapper extends VulkanWrapper implements VulkanDeviceSelector {

    private Format[] defaultFormats = new Format[] { Format.VK_FORMAT_R8G8B8A8_UNORM, Format.VK_FORMAT_B8G8R8A8_UNORM,
            Format.VK_FORMAT_R8G8B8_UNORM, Format.VK_FORMAT_B8G8R8_UNORM, Format.VK_FORMAT_A8B8G8R8_UNORM_PACK32 };

    private PhysicalDevice[] devices;
    private Queue queue;
    private SurfaceFormat surfaceFormat;
    private PresentModeKHR presentMode;
    private PhysicalDeviceMemoryProperties memoryProperties;
    protected ImageView[] imageViews;
    protected SwapChain swapChain;

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
        SimpleLogger.d(getClass(), "Selected device: " + selected.getProperties().getDeviceName());
        QueueFamilyProperties queueFamily = selectQueueInstance(selected);

        createLogicalDevice(selected, queueFamily);
        queue = createQueue(queueFamily);
        surfaceFormat = selectSurfaceFormat(getSurfaceFormats(selected));
        SimpleLogger.d(getClass(), "Selected surface format:" + surfaceFormat);
        presentMode = selectPresentMode(surfaceFormat, getPresentModes(selected));
        SimpleLogger.d(getClass(), "Selected present mode :" + presentMode);
        Extent2D swapExtent = selectSwapExtent(selected);
        SimpleLogger.d(getClass(), "Swapchain extent selected: " + swapExtent);
        swapChain = createSwapChain(selected, surfaceFormat, swapExtent, presentMode);
        SimpleLogger.d(getClass(), "Created swapchain with " + swapChain.imageCount + " images.");
        imageViews = createImageViews(swapChain, surfaceFormat);
        SimpleLogger.d(getClass(), "Created " + imageViews.length + " image views.");
        memoryProperties = getMemoryProperties(selected);

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

    /**
     * Create the queue instance for the selected queue
     * 
     * @param selectedQueue
     * @return
     */
    protected abstract Queue createQueue(QueueFamilyProperties selectedQueue);

    /**
     * Returns an array of available surface formats
     * 
     * @param device
     * @return
     */
    protected abstract ArrayList<SurfaceFormat> getSurfaceFormats(PhysicalDevice device);

    /**
     * Returns an array with the available present modes
     * 
     * @param device
     * @return
     */
    protected abstract ArrayList<PresentModeKHR> getPresentModes(PhysicalDevice device);

    /**
     * Select the swapchain extent
     * 
     * @param device
     * @return
     */
    protected abstract Extent2D selectSwapExtent(PhysicalDevice device);

    /**
     * Creates the swapchain - but not the images needed for the swapchain
     * 
     * @param device
     * @param surfaceFormat
     * @param swapChainExtent
     * @param presentMode
     * @return The SwapChain wrapper, the images in swapchain are not created yet.
     */
    protected abstract SwapChain createSwapChain(PhysicalDevice device, SurfaceFormat surfaceFormat,
            Extent2D swapChainExtent,
            PresentModeKHR presentMode);

    /**
     * Creates the swapchain image views
     * 
     * @param swapChain
     * @param surfaceFormat
     * @return The image views for the swapchain
     */
    protected abstract ImageView[] createImageViews(SwapChain swapChain, SurfaceFormat surfaceFormat);

    /**
     * Creates an ImageView based on the createinfo
     * 
     * @param createInfo
     * @return
     */
    protected abstract ImageView createImageView(ImageViewCreateInfo createInfo);

    /**
     * Creates the memory properties
     * 
     * @param device
     * @return
     */
    protected abstract PhysicalDeviceMemoryProperties getMemoryProperties(PhysicalDevice device);

    @Override
    public PhysicalDevice selectDevice(PhysicalDevice[] devices) {
        ArrayList<PhysicalDevice> validDevices = new ArrayList<>();
        for (PhysicalDevice d : devices) {
            if (d.getExtension(Extensions.VK_KHR_swapchain.name()) != null) {
                for (QueueFamilyProperties qp : d.getQueueFamilyProperties()) {
                    if (qp.isSurfaceSupportsPresent()) {
                        validDevices.add(d);
                    }
                }
            }
        }
        if (validDevices.size() == 0) {
            return null;
        }
        // Try to find discreet device.
        for (PhysicalDevice d : validDevices) {
            if (d.getProperties().getDeviceType() == PhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                return d;
            }
        }
        // Try to find integrated device.
        for (PhysicalDevice d : validDevices) {
            if (d.getProperties().getDeviceType() == PhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
                return d;
            }
        }
        return validDevices.get(0);
    }

    @Override
    public QueueFamilyProperties selectQueueInstance(PhysicalDevice device) {
        for (QueueFamilyProperties qp : device.getQueueFamilyProperties()) {
            if ((qp.isSurfaceSupportsPresent()) && qp.hasSupport(QueueFlagBits.VK_QUEUE_GRAPHICS_BIT)) {
                return qp;
            }
        }
        return null;
    }

    protected ArrayList<SurfaceFormat> filterByColorspace(ArrayList<SurfaceFormat> formats,
            ColorSpaceKHR[] colorSpace) {
        ArrayList<SurfaceFormat> result = new ArrayList<>();
        for (SurfaceFormat sf : formats) {
            for (ColorSpaceKHR c : colorSpace) {
                if (sf.space == c) {
                    result.add(sf);
                }
            }
        }
        return result;
    }

    protected SurfaceFormat getByFormat(ArrayList<SurfaceFormat> formats, Format format) {
        for (SurfaceFormat sf : formats) {
            if (sf.format == format) {
                return sf;
            }
        }
        return null;
    }

    protected SurfaceFormat getByFormats(ArrayList<SurfaceFormat> formats, Format[] list) {
        for (Format f : list) {
            SurfaceFormat selected = getByFormat(formats, f);
            if (selected != null) {
                return selected;
            }
        }
        return null;
    }

    /**
     * Selects the display chain surfaceformat
     * 
     * @param formats
     * @return
     * @throws IllegalArgumentException If no suitable surfaceformat could be found
     */
    protected SurfaceFormat selectSurfaceFormat(ArrayList<SurfaceFormat> formats) {
        ArrayList<SurfaceFormat> srgb = filterByColorspace(formats, new ColorSpaceKHR[] {
                ColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR, ColorSpaceKHR.VK_COLORSPACE_SRGB_NONLINEAR_KHR });
        SurfaceFormat selected = getByFormats(srgb, defaultFormats);
        if (selected == null) {
            throw new IllegalArgumentException("No surfaceformat");
        }
        return selected;
    }

    protected boolean hasPresentMode(ArrayList<PresentModeKHR> presentModes, PresentModeKHR mode) {
        for (PresentModeKHR m : presentModes) {
            if (m == mode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the desired present mode
     * 
     * @param surfaceFormat
     * @param presentModes
     * @return
     */
    protected PresentModeKHR selectPresentMode(SurfaceFormat surfaceFormat, ArrayList<PresentModeKHR> presentModes) {
        if (hasPresentMode(presentModes, PresentModeKHR.VK_PRESENT_MODE_FIFO_RELAXED_KHR)) {
            return PresentModeKHR.VK_PRESENT_MODE_FIFO_RELAXED_KHR;
        }
        // Fallback to required presentmode
        return PresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
    }

}
