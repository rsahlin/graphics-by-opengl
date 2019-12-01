package com.nucleus.vulkan.structs;

/**
 * VkViewport
 *
 */
public class Viewport {

    final float x;
    final float y;
    final float width;
    final float height;
    final float minDepth;
    final float maxDepth;

    public Viewport(float x, float y, float width, float height, float minDepth, float maxDepth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

}
