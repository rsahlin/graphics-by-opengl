package com.nucleus.opengl.lwjgl3;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VKCapabilitiesInstance;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.LWJGLVulkanDeviceFeatures;
import com.nucleus.vulkan.LWJGLVulkanLimits;
import com.nucleus.vulkan.Vulkan11Wrapper;

public class LWJGL3Vulkan11Wrapper extends Vulkan11Wrapper {

    public class Device implements PhysicalDevice {

        private Device(long deviceAdress, long surface) {

            device = new VkPhysicalDevice(deviceAdress, instance);
            deviceProperties = new DeviceProperties(device);
            deviceFeatures = new LWJGLVulkanDeviceFeatures(device, surface);

        }

        private VkPhysicalDevice device;
        private DeviceProperties deviceProperties;
        private com.nucleus.vulkan.LWJGLVulkanDeviceFeatures deviceFeatures;

        @Override
        public String toString() {
            String result = deviceProperties.getDeviceName() + ", " + deviceProperties.getAPIVersion() + ", "
                    + deviceProperties.getDeviceType() + "\n";
            result += deviceFeatures.toString();
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
        public VkPhysicalDeviceType getDeviceType() {
            return VkPhysicalDeviceType.get(deviceProperties.deviceType());
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

    }

    private VkInstance instance;
    private Device[] devices;
    private long surface;

    protected LWJGL3Vulkan11Wrapper(Renderers version, long window) {
        super(version);
        init(window);
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
        instance = createInstance(requiredExtensions);
        VKCapabilitiesInstance caps = instance.getCapabilities();
        SimpleLogger.d(getClass(), "Created Vulkan");

        long[] s = new long[1];
        if (GLFWVulkan.glfwCreateWindowSurface(instance, window, null, s) != VK10.VK_SUCCESS) {
            throw new IllegalArgumentException("Could not create GLFW window surface");
        }
        surface = s[0];
        selectDevice();
    }

    private void selectDevice() {
        int[] deviceCount = new int[1];
        if (VK10.vkEnumeratePhysicalDevices(instance, deviceCount, null) == VK10.VK_SUCCESS) {
            devices = new Device[deviceCount[0]];
            PointerBuffer pb = MemoryUtil.memAllocPointer(deviceCount[0]);
            if (VK10.vkEnumeratePhysicalDevices(instance, deviceCount, pb) == VK10.VK_SUCCESS) {
                Device device = selectDevice(pb);
            }
        }
    }

    private Device selectDevice(PointerBuffer pb) {
        fetchDevices(pb);

        return devices[0];
    }

    private void fetchDevices(PointerBuffer pb) {
        pb.rewind();
        int index = 0;
        while (pb.remaining() > 0) {
            devices[index] = new Device(pb.get(), surface);
            SimpleLogger.d(getClass(), "Found device: " + devices[index]);
            index++;
        }

    }

    private VkInstance createInstance(PointerBuffer requiredExtensions) {
        VkApplicationInfo appInfo = VkApplicationInfo.calloc()
                .sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(MemoryUtil.memUTF8("LWJGL Vulkan Demo"))
                .pEngineName(MemoryUtil.memUTF8("graphics-by-vulkan"))
                .apiVersion(VK10.VK_MAKE_VERSION(1, 0, 2));

        ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = MemoryUtil
                .memUTF8(EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
        PointerBuffer ppEnabledExtensionNames = MemoryUtil.memAllocPointer(requiredExtensions.remaining() + 1);
        ppEnabledExtensionNames.put(requiredExtensions) // <- platform-dependent required extensions
                .put(VK_EXT_DEBUG_REPORT_EXTENSION) // <- the debug extensions
                .flip();

        // Vulkan uses many struct/record types when creating something. This ensures that every information is
        // available
        // at the callsite of the creation and allows for easier validation and also for immutability of the created
        // object.
        //
        // The following struct defines everything that is needed to create a VkInstance
        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
                .sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO) // <- identifies what kind of struct this is (this
                                                                    // is
                // useful for extending the struct type later)
                .pNext(MemoryUtil.NULL) // <- must always be NULL until any next Vulkan version tells otherwise
                .pApplicationInfo(appInfo) // <- the application info we created above
                .ppEnabledExtensionNames(ppEnabledExtensionNames); // <- and the extension names themselves
        PointerBuffer pInstance = MemoryUtil.memAllocPointer(1); // <- create a PointerBuffer which will hold the handle
                                                                 // to the
        // created VkInstance
        int err = VK10.vkCreateInstance(pCreateInfo, null, pInstance); // <- actually create the VkInstance now!
        long instance = pInstance.get(0); // <- get the VkInstance handle
        MemoryUtil.memFree(pInstance); // <- free the PointerBuffer
        // Check whether we succeeded in creating the VkInstance
        if (err != VK10.VK_SUCCESS) {
            throw new AssertionError("Failed to create VkInstance: " + err);
        }
        // Create an object-oriented wrapper around the simple VkInstance long handle
        // This is needed by LWJGL to later "dispatch" (i.e. direct calls to) the right Vukan functions.
        VkInstance ret = new VkInstance(instance, pCreateInfo);

        // Now we can free/deallocate everything
        pCreateInfo.free();
        MemoryUtil.memFree(VK_EXT_DEBUG_REPORT_EXTENSION);
        MemoryUtil.memFree(ppEnabledExtensionNames);
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

}
