package com.nucleus.vulkan.lwjgl3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.LWJGLVkQueueFamilyProperties;
import com.nucleus.vulkan.LWJGLVulkanDeviceFeatures;
import com.nucleus.vulkan.LWJGLVulkanLimits;
import com.nucleus.vulkan.Vulkan11Wrapper;

public class LWJGL3Vulkan11Wrapper extends Vulkan11Wrapper {

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
            IntBuffer ib = BufferUtils.createIntBuffer(1);
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
        instance = createInstance(requiredExtensions);
        SimpleLogger.d(getClass(), "Created Vulkan");

        long[] s = new long[1];
        if (GLFWVulkan.glfwCreateWindowSurface(instance, window, null, s) != VK10.VK_SUCCESS) {
            throw new IllegalArgumentException("Could not create GLFW window surface");
        }
        surface = s[0];
    }

    @Override
    protected PhysicalDevice[] fetchDevices() {
        int[] deviceCount = new int[1];
        if (VK10.vkEnumeratePhysicalDevices(instance, deviceCount, null) == VK10.VK_SUCCESS) {
            Device[] devices = new Device[deviceCount[0]];
            PointerBuffer pb = MemoryUtil.memAllocPointer(deviceCount[0]);
            if (VK10.vkEnumeratePhysicalDevices(instance, deviceCount, pb) == VK10.VK_SUCCESS) {
                fetchDevices(pb, devices);
            } else {
                throw new IllegalArgumentException("Failed to enumerate physical devices");
            }
            return devices;
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

    protected void createLogicalDevice(PhysicalDevice device, QueueFamilyProperties selectedQueue) {
        FloatBuffer prios = BufferUtils.createFloatBuffer(selectedQueue.getQueueCount());
        VkDeviceQueueCreateInfo.Buffer queue = VkDeviceQueueCreateInfo.mallocStack(1)
                .sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .queueFamilyIndex(selectedQueue.getQueueIndex())
                .pQueuePriorities(prios);
        /*
         * VkDeviceCreateInfo deviceInfo = VkDeviceCreateInfo.mallocStack(1)
         * .sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
         * .pNext(MemoryUtil.NULL)
         * .flags(0)
         * .pQueueCreateInfos(queue)
         * .ppEnabledLayerNames(null)
         * .ppEnabledExtensionNames()
         * .pEnabledFeatures(features);
         * 
         * check(vkCreateDevice(gpu, device, null, pp));
         * 
         * this.device = new VkDevice(pp.get(0), gpu, device);
         * 
         */
    }

}
