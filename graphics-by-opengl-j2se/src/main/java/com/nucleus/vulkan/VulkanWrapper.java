package com.nucleus.vulkan;

import com.nucleus.Backend;
import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * The Vulkan wrapper -all things related to Vulkan functionality that is shared independently of version
 * 
 * @author rsa1lud
 *
 */
public abstract class VulkanWrapper extends Backend {

    public enum VkPhysicalDeviceType {
        VK_PHYSICAL_DEVICE_TYPE_OTHER(0),
        VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1),
        VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2),
        VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3),
        VK_PHYSICAL_DEVICE_TYPE_CPU(4);

        public final int value;

        private VkPhysicalDeviceType(int value) {
            this.value = value;
        }

        public static VkPhysicalDeviceType get(int value) {
            for (VkPhysicalDeviceType type : VkPhysicalDeviceType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

    };

    public enum VkQueueFlagBits {
        VK_QUEUE_GRAPHICS_BIT(1),
        VK_QUEUE_COMPUTE_BIT(2),
        VK_QUEUE_TRANSFER_BIT(4),
        VK_QUEUE_SPARSE_BINDING_BIT(8),
        VK_QUEUE_PROTECTED_BIT(16);

        public final int mask;

        private VkQueueFlagBits(int mask) {
            this.mask = mask;
        }

        public static String toString(int flags) {
            StringBuffer result = new StringBuffer();
            for (VkQueueFlagBits bits : VkQueueFlagBits.values()) {
                if ((flags & bits.mask) != 0) {
                    result.append(result.length() > 0 ? " | " + bits.name() : bits.name());
                }
            }
            return result.toString();
        }

    };

    protected VulkanWrapper(Renderers version) {
        super(version);
    }
}
