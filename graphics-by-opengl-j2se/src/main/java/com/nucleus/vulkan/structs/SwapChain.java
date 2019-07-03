package com.nucleus.vulkan.structs;

/**
 * Handle for pointer to SwapChain and number of images in swapchain
 *
 */
public class SwapChain {

    public final long swapChain;
    public final int imageCount;

    public SwapChain(long swapChain, int imageCount) {
        this.swapChain = swapChain;
        this.imageCount = imageCount;
    }

}
