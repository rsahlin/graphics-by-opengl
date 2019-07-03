package com.nucleus.vulkan;

import org.lwjgl.vulkan.VkQueue;

public class LWJGLQueue extends Queue {

    VkQueue queue;

    public LWJGLQueue(VkQueue queue) {
        if (queue == null) {
            throw new IllegalArgumentException("VkQueue is null");
        }
        this.queue = queue;
    }

}
