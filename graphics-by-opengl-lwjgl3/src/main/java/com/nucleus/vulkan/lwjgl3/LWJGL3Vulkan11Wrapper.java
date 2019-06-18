package com.nucleus.vulkan.lwjgl3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.DeviceLimits;
import com.nucleus.vulkan.ExtensionProperties;
import com.nucleus.vulkan.Extent2D;
import com.nucleus.vulkan.LWJGLPhysicalDeviceMemoryProperties;
import com.nucleus.vulkan.LWJGLQueue;
import com.nucleus.vulkan.LWJGLVkQueueFamilyProperties;
import com.nucleus.vulkan.LWJGLVulkanDeviceFeatures;
import com.nucleus.vulkan.LWJGLVulkanLimits;
import com.nucleus.vulkan.PhysicalDevice;
import com.nucleus.vulkan.PhysicalDeviceFeatures;
import com.nucleus.vulkan.PhysicalDeviceMemoryProperties;
import com.nucleus.vulkan.PhysicalDeviceProperties;
import com.nucleus.vulkan.Queue;
import com.nucleus.vulkan.QueueFamilyProperties;
import com.nucleus.vulkan.SwapChainBuffer;
import com.nucleus.vulkan.Vulkan10;
import com.nucleus.vulkan.Vulkan10.ColorSpaceKHR;
import com.nucleus.vulkan.Vulkan10.Extensions;
import com.nucleus.vulkan.Vulkan10.Format;
import com.nucleus.vulkan.Vulkan10.PresentModeKHR;
import com.nucleus.vulkan.Vulkan10.SurfaceFormat;
import com.nucleus.vulkan.Vulkan11Wrapper;

public class LWJGL3Vulkan11Wrapper extends Vulkan11Wrapper {

    private static final ByteBuffer KHR_swapchain = MemoryUtil.memASCII(Vulkan10.Extensions.VK_KHR_swapchain.name());
    private static final ByteBuffer EXT_debug_report = MemoryUtil
            .memASCII(Vulkan10.Extensions.VK_EXT_debug_report.name());

    private final PointerBuffer logicalDeviceExtensions = MemoryUtil.memAllocPointer(8);
    private final PointerBuffer pointer = MemoryUtil.memAllocPointer(1);
    private final PointerBuffer instanceExtensions = MemoryUtil.memAllocPointer(64);
    private SwapChainBuffer[] buffers;

    IntBuffer ib = BufferUtils.createIntBuffer(1);
    LongBuffer lb = BufferUtils.createLongBuffer(1);

    public class Device implements PhysicalDevice {

        private Device(long deviceAdress, long surface) {
            device = new VkPhysicalDevice(deviceAdress, instance);
            readExtensions(device);
            deviceProperties = new DeviceProperties(device);
            deviceFeatures = new LWJGLVulkanDeviceFeatures(device, surface);
            readQueueProperties(device, surface);
        }

        protected void readQueueProperties(VkPhysicalDevice device, long surface) {
            int[] queueCount = new int[1];
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueCount, null);
            org.lwjgl.vulkan.VkQueueFamilyProperties.Buffer queueProperties = org.lwjgl.vulkan.VkQueueFamilyProperties
                    .malloc(queueCount[0]);
            queueFamilyProperties = new QueueFamilyProperties[queueCount[0]];
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueCount, queueProperties);
            queueProperties.rewind();
            for (int i = 0; i < queueFamilyProperties.length; i++) {
                queueFamilyProperties[i] = new LWJGLVkQueueFamilyProperties(queueProperties.get(), i,
                        device, surface);
            }
        }

        protected void readExtensions(VkPhysicalDevice device) {
            VK10.vkEnumerateDeviceExtensionProperties(device, (String) null, ib, null);
            if (ib.get(0) > 0) {
                int count = ib.get(0);
                extensionProperties = new ExtensionProperties[count];
                VkExtensionProperties.Buffer extensions = VkExtensionProperties.mallocStack(count);
                VK10.vkEnumerateDeviceExtensionProperties(device, (String) null, ib, extensions);
                for (int i = 0; i < count; i++) {
                    extensions.position(i);
                    extensionProperties[i] = new ExtensionProperties(extensions.extensionNameString(),
                            extensions.specVersion());
                }
            }
        }

        private VkPhysicalDevice device;
        private DeviceProperties deviceProperties;
        private com.nucleus.vulkan.LWJGLVulkanDeviceFeatures deviceFeatures;
        private QueueFamilyProperties[] queueFamilyProperties;
        private ExtensionProperties[] extensionProperties;

        protected VkPhysicalDevice getVkPhysicalDevice() {
            return device;
        }

        @Override
        public String toString() {
            String result = deviceProperties.toString() + "\n";
            result += deviceFeatures.toString();
            result += "Queue support: \n";
            for (int i = 0; i < queueFamilyProperties.length; i++) {
                result += queueFamilyProperties[i].toString();
            }
            ExtensionProperties[] extensions = getExtensions();
            if (extensions != null) {
                result += "Extension support: \n";
                for (ExtensionProperties ep : extensions) {
                    result += ep.getName() + "\n";
                }
            }
            return result;
        }

        @Override
        public PhysicalDeviceProperties getProperties() {
            return deviceProperties;
        }

        @Override
        public PhysicalDeviceFeatures getFeatures() {
            return deviceFeatures;
        }

        @Override
        public QueueFamilyProperties[] getQueueFamilyProperties() {
            return queueFamilyProperties;
        }

        @Override
        public ExtensionProperties[] getExtensions() {
            return extensionProperties;
        }

        @Override
        public ExtensionProperties getExtension(String extensionName) {
            if (extensionProperties != null) {
                for (ExtensionProperties ep : extensionProperties) {
                    if (extensionName.equalsIgnoreCase(ep.getName())) {
                        return ep;
                    }
                }
            }
            return null;
        }
    }

    public class DeviceProperties implements PhysicalDeviceProperties {

        private VkPhysicalDeviceProperties deviceProperties;
        private APIVersion apiVersion;
        private LWJGLVulkanLimits limits;

        public DeviceProperties(VkPhysicalDevice device) {
            deviceProperties = VkPhysicalDeviceProperties.malloc();
            VK10.vkGetPhysicalDeviceProperties(device, deviceProperties);
            apiVersion = new APIVersion(deviceProperties.apiVersion());
            limits = new LWJGLVulkanLimits(deviceProperties.limits());
        }

        @Override
        public PhysicalDeviceType getDeviceType() {
            return PhysicalDeviceType.get(deviceProperties.deviceType());
        }

        @Override
        public String getDeviceName() {
            return deviceProperties.deviceNameString();
        }

        @Override
        public APIVersion getAPIVersion() {
            return apiVersion;
        }

        @Override
        public DeviceLimits getLimits() {
            return limits;
        }

        @Override
        public String toString() {
            String result = getDeviceName() + ", " + getAPIVersion() + ", "
                    + getDeviceType() + "\n" + "Device limits:\n" +
                    getLimits().toString();
            return result;

        }

    }

    private VkInstance instance;
    private long surface;
    private VkDevice deviceInstance;
    private long swapChain;
    private int swapChainImageCount;

    /**
     * Internal constructor - DO NOT USE
     * 
     * @param version
     * @param window
     */
    public LWJGL3Vulkan11Wrapper(Renderers version, long window) {
        super(version);
        init(window);
        init();
    }

    protected void init(long window) {
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new AssertionError("GLFW failed to find the Vulkan loader");
        }

        /* Look for instance extensions */
        PointerBuffer requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        if (requiredExtensions == null) {
            throw new AssertionError("Failed to find list of required Vulkan extensions");
        }

        addExtensions(instanceExtensions, requiredExtensions);
        instanceExtensions.put(EXT_debug_report);

        instanceExtensions.flip();
        instance = createInstance(instanceExtensions);
        SimpleLogger.d(getClass(), "Created Vulkan");
        MemoryUtil.memFree(instanceExtensions);

        long[] s = new long[1];
        if (GLFWVulkan.glfwCreateWindowSurface(instance, window, null, s) != VK10.VK_SUCCESS) {
            throw new IllegalArgumentException("Could not create GLFW window surface");
        }
        surface = s[0];
    }

    protected void addExtensions(PointerBuffer extensionNames, PointerBuffer requiredExtensions) {
        for (int i = 0; i < requiredExtensions.capacity(); i++) {
            extensionNames.put(requiredExtensions.get(i));
        }
    }

    @Override
    protected PhysicalDevice[] fetchDevices() {
        int[] deviceCount = new int[1];
        if (VK10.vkEnumeratePhysicalDevices(instance, deviceCount, null) == VK10.VK_SUCCESS) {
            Device[] devices = new Device[deviceCount[0]];
            PointerBuffer pb = null;
            try {
                pb = MemoryUtil.memAllocPointer(deviceCount[0]);
                if (VK10.vkEnumeratePhysicalDevices(instance, deviceCount, pb) == VK10.VK_SUCCESS) {
                    fetchDevices(pb, devices);
                } else {
                    throw new IllegalArgumentException("Failed to enumerate physical devices");
                }
                return devices;
            } finally {
                MemoryUtil.memFree(pb);
            }
        } else {
            throw new IllegalArgumentException("Failed to enumerate number of physical devices in system.");
        }
    }

    private void fetchDevices(PointerBuffer pb, Device[] devices) {
        pb.rewind();
        int index = 0;
        while (pb.remaining() > 0) {
            devices[index] = new Device(pb.get(), surface);
            index++;
        }
    }

    private VkInstance createInstance(PointerBuffer extensions) {
        VkApplicationInfo appInfo = VkApplicationInfo.calloc()
                .sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(MemoryUtil.memUTF8("LWJGL Vulkan Demo"))
                .pEngineName(MemoryUtil.memUTF8("graphics-by-vulkan"))
                .apiVersion(VK10.VK_MAKE_VERSION(1, 0, 2));

        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
                .sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO) // <- identifies what kind of struct this is (this
                                                                    // is
                // useful for extending the struct type later)
                .pNext(MemoryUtil.NULL) // <- must always be NULL until any next Vulkan version tells otherwise
                .pApplicationInfo(appInfo) // <- the application info we created above
                .ppEnabledExtensionNames(extensions); // <- and the extension names themselves
        // created VkInstance
        int err = VK10.vkCreateInstance(pCreateInfo, null, pointer);
        long instance = pointer.get(0); // <- get the VkInstance handle
        // Check whether we succeeded in creating the VkInstance
        if (err != VK10.VK_SUCCESS) {
            throw new AssertionError("Failed to create VkInstance: " + err);
        }
        // Create an object-oriented wrapper around the simple VkInstance long handle
        // This is needed by LWJGL to later "dispatch" (i.e. direct calls to) the right Vukan functions.
        VkInstance ret = new VkInstance(instance, pCreateInfo);

        // Now we can free/deallocate everything
        pCreateInfo.free();
        MemoryUtil.memFree(appInfo.pApplicationName());
        MemoryUtil.memFree(appInfo.pEngineName());
        appInfo.free();
        return ret;
    }

    @Override
    public void destroy() {
        if (instance != null) {
            VK10.vkDestroyInstance(instance, null);
            instance = null;
        }
    }

    @Override
    protected void createLogicalDevice(PhysicalDevice device, QueueFamilyProperties selectedQueue) {

        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc();
        device.getFeatures().copy(features);

        FloatBuffer prios = BufferUtils.createFloatBuffer(selectedQueue.getQueueCount());
        prios.rewind();
        VkDeviceQueueCreateInfo.Buffer queue = VkDeviceQueueCreateInfo.mallocStack(1)
                .sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .queueFamilyIndex(selectedQueue.getQueueIndex())
                .pQueuePriorities(prios);

        logicalDeviceExtensions.put(KHR_swapchain);
        logicalDeviceExtensions.flip();
        VkDeviceCreateInfo deviceInfo = VkDeviceCreateInfo.malloc();
        deviceInfo.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .pQueueCreateInfos(queue)
                .ppEnabledLayerNames(null)
                .ppEnabledExtensionNames(logicalDeviceExtensions)
                .pEnabledFeatures(features);

        VkPhysicalDevice physicalDevice = ((Device) device).getVkPhysicalDevice();

        assertResult(VK10.vkCreateDevice(physicalDevice, deviceInfo, null, pointer));

        deviceInstance = new VkDevice(pointer.get(0), physicalDevice, deviceInfo);
        SimpleLogger.d(getClass(), "Created Vulkan instance");
    }

    @Override
    protected Queue createQueue(QueueFamilyProperties selectedQueue) {
        VK10.vkGetDeviceQueue(deviceInstance, selectedQueue.getQueueIndex(), 0, pointer);
        VkQueue queue = new VkQueue(pointer.get(0), deviceInstance);
        return new LWJGLQueue(queue);
    }

    @Override
    protected ArrayList<SurfaceFormat> getSurfaceFormats(PhysicalDevice device) {
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vkDevice, surface, ib, null));
        VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.malloc(ib.get(0));
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vkDevice, surface, ib, surfaceFormats));

        ArrayList<SurfaceFormat> result = new ArrayList<SurfaceFormat>();

        surfaceFormats.rewind();
        while (surfaceFormats.hasRemaining()) {
            VkSurfaceFormatKHR vkSF = surfaceFormats.get();
            Format format = Format.get(vkSF.format());
            ColorSpaceKHR space = ColorSpaceKHR.get(vkSF.colorSpace());
            if (format != null && space != null) {
                SurfaceFormat sf = new SurfaceFormat(format, space);
                result.add(sf);
                SimpleLogger.d(getClass(), "Added surfaceformat: " + sf);
            } else {
                SimpleLogger.d(getClass(), "Error - could not find surfaceformat or colorspace");
            }
        }
        return result;
    }

    @Override
    protected ArrayList<PresentModeKHR> getPresentModes(PhysicalDevice device) {
        ArrayList<PresentModeKHR> result = new ArrayList<>();
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(vkDevice, surface, ib, null));
        IntBuffer presentModes = BufferUtils.createIntBuffer(ib.get(0));
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(vkDevice, surface, ib, presentModes));

        presentModes.rewind();
        while (presentModes.hasRemaining()) {
            int v = presentModes.get();
            PresentModeKHR mode = PresentModeKHR.get(v);
            if (mode != null) {
                SimpleLogger.d(getClass(), "Added presentmode " + mode);
                result.add(mode);
            } else {
                SimpleLogger.d(getClass(), "Error - no presentmode for: " + v);
            }
        }
        return result;
    }

    @Override
    protected Extent2D selectSwapExtent(PhysicalDevice device) {
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        VkSurfaceCapabilitiesKHR surfaceCaps = VkSurfaceCapabilitiesKHR.malloc();
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkDevice, surface, surfaceCaps));

        Extent2D result = new Extent2D(surfaceCaps.currentExtent().width(), surfaceCaps.currentExtent().height());

        if (surfaceCaps.currentExtent().width() == 0xFFFFFFFF) {
            // If the surface size is undefined, the size is set to the size
            // of the images requested, which must fit within the minimum and
            // maximum values.
            if (result.getWidth() < surfaceCaps.minImageExtent().width()) {
                result.setWidth(surfaceCaps.minImageExtent().width());
            } else if (result.getWidth() > surfaceCaps.maxImageExtent().width()) {
                result.setWidth(surfaceCaps.maxImageExtent().width());
            }

            if (result.getHeight() < surfaceCaps.minImageExtent().height()) {
                result.setHeight(surfaceCaps.minImageExtent().height());
            } else if (result.getHeight() > surfaceCaps.maxImageExtent().height()) {
                result.setHeight(surfaceCaps.maxImageExtent().height());
            }
        }
        return result;
    }

    @Override
    protected int createSwapChain(PhysicalDevice device, SurfaceFormat surfaceFormat, Extent2D swapChainExtent,
            PresentModeKHR presentMode) {

        if (device.getExtension(Extensions.VK_KHR_swapchain.name()) == null) {
            throw new IllegalArgumentException("Device has no support for " + Extensions.VK_KHR_swapchain);
        }

        VkExtent2D scExtent = VkExtent2D.malloc();
        scExtent.set(swapChainExtent.getWidth(), swapChainExtent.getHeight());
        VkSwapchainCreateInfoKHR swapchainInfo = VkSwapchainCreateInfoKHR.calloc()
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .surface(surface)
                .minImageCount(1)
                .imageFormat(surfaceFormat.getFormat().value)
                .imageColorSpace(surfaceFormat.getColorSpace().value)
                .imageExtent(scExtent)
                .imageArrayLayers(1)
                .imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode.value)
                .clipped(true);

        assertResult(KHRSwapchain.vkCreateSwapchainKHR(deviceInstance, swapchainInfo, null, lb));
        swapChain = lb.get(0);

        // If we just re-created an existing swapchain, we should destroy the old
        // swapchain at this point.

        assertResult(KHRSwapchain.vkGetSwapchainImagesKHR(deviceInstance, swapChain, ib, null));
        swapChainImageCount = ib.get(0);
        return swapChainImageCount;
    }

    @Override
    protected void createSwapBuffers(int bufferCount, SurfaceFormat surfaceFormat) {
        ib.rewind();
        ib.put(bufferCount);
        ib.rewind();
        LongBuffer swapchainImages = BufferUtils.createLongBuffer(bufferCount);
        assertResult(KHRSwapchain.vkGetSwapchainImagesKHR(deviceInstance, swapChain, ib, swapchainImages));

        buffers = new SwapChainBuffer[bufferCount];

        for (int i = 0; i < bufferCount; i++) {
            long image = swapchainImages.get(i);

            VkImageViewCreateInfo color_attachment_view = VkImageViewCreateInfo.malloc()
                    .sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .pNext(MemoryUtil.NULL)
                    .flags(0)
                    .image(image)
                    .viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
                    .format(surfaceFormat.getFormat().value)
                    .components(it -> it
                            .r(VK10.VK_COMPONENT_SWIZZLE_R)
                            .g(VK10.VK_COMPONENT_SWIZZLE_G)
                            .b(VK10.VK_COMPONENT_SWIZZLE_B)
                            .a(VK10.VK_COMPONENT_SWIZZLE_A))
                    .subresourceRange(it -> it
                            .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                            .baseMipLevel(0)
                            .levelCount(1)
                            .baseArrayLayer(0)
                            .layerCount(1));

            assertResult(VK10.vkCreateImageView(deviceInstance, color_attachment_view, null, lb));
            long view = lb.get(0);
            buffers[i] = new SwapChainBuffer(image, view);
        }
    }

    @Override
    protected PhysicalDeviceMemoryProperties getMemoryProperties(PhysicalDevice device) {
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();

        VkPhysicalDeviceMemoryProperties p = VkPhysicalDeviceMemoryProperties.malloc();
        VK10.vkGetPhysicalDeviceMemoryProperties(vkDevice, p);
        PhysicalDeviceMemoryProperties memoryProperties = new LWJGLPhysicalDeviceMemoryProperties(p);

        return memoryProperties;

    }

}
