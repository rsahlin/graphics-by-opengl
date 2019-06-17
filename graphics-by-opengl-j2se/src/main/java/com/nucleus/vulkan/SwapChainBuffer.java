package com.nucleus.vulkan;

public class SwapChainBuffer {

    public SwapChainBuffer(long image, long view) {
        this.image = image;
        this.view = view;
    }

    protected final long image;
    protected final long view;

}
