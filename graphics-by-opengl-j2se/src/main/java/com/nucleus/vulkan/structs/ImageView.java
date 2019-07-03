package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkImageView
 *
 */
public class ImageView {

    public final Image image;
    public final long view;

    public ImageView(Image image, long view) {
        this.image = image;
        this.view = view;
    }

}
