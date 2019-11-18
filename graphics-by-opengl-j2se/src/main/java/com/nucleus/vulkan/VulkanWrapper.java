package com.nucleus.vulkan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.nucleus.Backend;
import com.nucleus.common.FileUtils;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.shader.ShaderBinary;
import com.nucleus.vulkan.structs.QueueFamilyProperties;

/**
 * The Vulkan wrapper -all things related to Vulkan functionality that is shared
 * independently of version
 *
 */
public abstract class VulkanWrapper extends Backend {

    public enum Result {
        VK_SUCCESS(0),
        VK_NOT_READY(1),
        VK_TIMEOUT(2),
        VK_EVENT_SET(3),
        VK_EVENT_RESET(4),
        VK_INCOMPLETE(5),
        VK_ERROR_OUT_OF_HOST_MEMORY(-1),
        VK_ERROR_OUT_OF_DEVICE_MEMORY(-2),
        VK_VK_ERROR_INITIALIZATION_FAILED(-3),
        VK_ERROR_DEVICE_LOST(-4),
        VK_ERROR_MEMORY_MAP_FAILED(-5),
        VK_ERROR_LAYER_NOT_PRESENT(-6),
        VK_ERROR_EXTENSION_NOT_PRESENT(-7),
        VK_ERROR_FEATURE_NOT_PRESENT(-8),
        VK_ERROR_INCOMPATIBLE_DRIVER(-9),
        VK_ERROR_TOO_MANY_OBJECTS(-10),
        VK_ERROR_FORMAT_NOT_SUPPORTED(-11),
        VK_ERROR_FRAGMENTED_POOL(-12),
        VK_ERROR_OUT_OF_POOL_MEMORY(-1000069000),
        VK_ERROR_INVALID_EXTERNAL_HANDLE(-1000072003),
        VK_ERROR_SURFACE_LOST_KHR(-1000000000),
        VK_ERROR_NATIVE_WINDOW_IN_USE_KHR(-1000000001),
        VK_SUBOPTIMAL_KHR(1000001003),
        VK_ERROR_OUT_OF_DATE_KHR(-1000001004),
        VK_ERROR_INCOMPATIBLE_DISPLAY_KHR(-1000003001),
        VK_ERROR_VALIDATION_FAILED_EXT(-1000011001),
        VK_ERROR_INVALID_SHADER_NV(-1000012000),
        VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT(-1000158000),
        VK_ERROR_FRAGMENTATION_EXT(-1000161000),
        VK_ERROR_NOT_PERMITTED_EXT(-1000174001),
        VK_ERROR_INVALID_DEVICE_ADDRESS_EXT(-1000244000),
        VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT(-1000255000),
        VK_ERROR_OUT_OF_POOL_MEMORY_KHR(VK_ERROR_OUT_OF_POOL_MEMORY.value),
        VK_ERROR_INVALID_EXTERNAL_HANDLE_KHR(VK_ERROR_INVALID_EXTERNAL_HANDLE.value),
        VK_RESULT_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        private Result(int value) {
            this.value = value;
        }

        public static Result getResult(int value) {
            for (Result r : values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return null;
        }

    }

    public interface VulkanDeviceSelector {
        /**
         * Vulkan implementation agnostic method to select the device to use
         * 
         * @param devices
         * @return The device to use or null if no device that can be used
         */
        public PhysicalDevice selectDevice(PhysicalDevice[] devices);

        /**
         * Used to select the queue instance of the device
         * 
         * @param device
         */
        public QueueFamilyProperties selectQueueInstance(PhysicalDevice device);

    }

    protected static VulkanDeviceSelector deviceSelector;

    /**
     * Sets the vulkan device selector - use this to override the default selector
     * behavior
     * 
     * @param deviceSelector The device selector to be called when Vulkan wrapper is
     * created, or null to remove.
     */
    public static void setVulkanDeviceSelector(VulkanDeviceSelector deviceSelector) {
        VulkanWrapper.deviceSelector = deviceSelector;
    }

    /**
     * The Vulkan API version #define VK_VERSION_MAJOR(version) ((int)(version) >>
     * 22) #define VK_VERSION_MINOR(version) (((int)(version) >> 12) & 0x3ff)
     * #define VK_VERSION_PATCH(version) ((int)(version) & 0xfff)
     *
     */
    public class APIVersion {
        public final int major;
        public final int minor;
        public final int patch;

        public APIVersion(int api) {
            this.major = api >> 22;
            this.minor = (api >> 12) & 0x3ff;
            this.patch = (api & 0x0fff);
        }

        @Override
        public String toString() {
            return major + "." + minor + " patch " + patch;
        }

    }

    protected VulkanWrapper(Renderers version) {
        super(version);
        try {
            initVulkanBackend();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected void initVulkanBackend() throws IOException, URISyntaxException {
        String path = ShaderBinary.PROGRAM_DIRECTORY + ShaderBinary.V450 + "/";
        // Returns subfolders in path - remember to include current dir - ie empty folder name.
        ArrayList<String> folders = FileUtils.getInstance().listResourceFolders(path);
        // Insert empty element for current dir.
        folders.add(0, "");
        GLSLCompiler.getInstance().compileShaders(path, folders);
    }

    /**
     * Checks that the resultcode is VK_SUCCESS, if not RuntimeException is thrown.
     * 
     * @param value
     */
    public void assertResult(int value) {
        Result r = Result.getResult(value);
        {
            if (r == null || r != Result.VK_SUCCESS) {
                throw new RuntimeException("Failed with error: " + r);
            }
        }
    }

}
