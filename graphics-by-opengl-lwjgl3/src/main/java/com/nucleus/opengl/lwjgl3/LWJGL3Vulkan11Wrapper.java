package com.nucleus.opengl.lwjgl3;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VKCapabilitiesInstance;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import com.nucleus.SimpleLogger;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.Vulkan11Wrapper;

public class LWJGL3Vulkan11Wrapper extends Vulkan11Wrapper {

    public class Device {
        private Device(long deviceAdress) {
            device = new VkPhysicalDevice(deviceAdress, instance);
            deviceFeatures = VkPhysicalDeviceFeatures.malloc();
            deviceProperties = VkPhysicalDeviceProperties.malloc();
            VK10.vkGetPhysicalDeviceProperties(device, deviceProperties);
            VK10.vkGetPhysicalDeviceFeatures(device, deviceFeatures);
            int[] queueCount = new int[1];
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueCount, null);
            queueProperties = VkQueueFamilyProperties.malloc(queueCount[0]);
            surfaceSupportPresent = new int[queueCount[0]];
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueCount, queueProperties);
            int[] supported = new int[1];
            for (int i = 0; i < surfaceSupportPresent.length; i++) {
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, supported);
            }
        }

        private VkPhysicalDevice device;
        private VkPhysicalDeviceProperties deviceProperties;
        private VkPhysicalDeviceFeatures deviceFeatures;
        private VkQueueFamilyProperties.Buffer queueProperties;
        private int[] surfaceSupportPresent;

        @Override
        public String toString() {
            String result = deviceProperties.deviceNameString() + ", "
                    + VkPhysicalDeviceType.get(deviceProperties.deviceType()) + "\n";
            queueProperties.rewind();
            result += "Queue support: \n";
            int index = 0;
            while (queueProperties.hasRemaining()) {
                result += VkQueueFlagBits.toString(queueProperties.get().queueFlags()) + " - surface present: " +
                        (surfaceSupportPresent[index] == VK10.VK_TRUE) + "\n";
                index++;
            }
            return result;
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
        GLFWVulkan.glfwCreateWindowSurface(instance, window, null, s);
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
            devices[index] = new Device(pb.get());
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
