package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkVertexInputBindingDescription
 */
public class VertexInputBindingDescription {
    enum VertexInputRate {
        VK_VERTEX_INPUT_RATE_VERTEX(0),
        VK_VERTEX_INPUT_RATE_INSTANCE(1),
        VK_VERTEX_INPUT_RATE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        private VertexInputRate(int value) {
            this.value = value;
        }
    };

    int binding;
    int stride;
    VertexInputRate inputRate;

}
