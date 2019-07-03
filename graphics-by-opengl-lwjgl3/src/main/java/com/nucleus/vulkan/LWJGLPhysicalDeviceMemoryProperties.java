package com.nucleus.vulkan;

import org.lwjgl.vulkan.VkMemoryHeap;
import org.lwjgl.vulkan.VkMemoryType;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class LWJGLPhysicalDeviceMemoryProperties extends PhysicalDeviceMemoryProperties {

    public LWJGLPhysicalDeviceMemoryProperties(VkPhysicalDeviceMemoryProperties vkProperties) {
        int memCount = vkProperties.memoryTypeCount();
        int heapCount = vkProperties.memoryHeapCount();

        memoryTypes = new MemoryType[memCount];
        memoryHeap = new MemoryHeap[heapCount];
        for (int i = 0; i < memCount; i++) {
            VkMemoryType vkType = vkProperties.memoryTypes(i);
            memoryTypes[i] = new MemoryType(vkType.propertyFlags(), vkType.heapIndex());
        }
        for (int i = 0; i < heapCount; i++) {
            VkMemoryHeap vkHeap = vkProperties.memoryHeaps(i);
            memoryHeap[i] = new MemoryHeap(vkHeap.size(), vkHeap.flags());
        }
    }

}
