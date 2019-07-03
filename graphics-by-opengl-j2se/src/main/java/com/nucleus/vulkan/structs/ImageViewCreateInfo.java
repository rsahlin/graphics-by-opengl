package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.ComponentMapping;
import com.nucleus.vulkan.Vulkan10.Format;
import com.nucleus.vulkan.Vulkan10.ImageSubresourceRange;
import com.nucleus.vulkan.Vulkan10.ImageViewType;

/**
 * Wrapper for VkImageViewCreateInfo
 *
 */
public class ImageViewCreateInfo {

    public final ImageViewType type;
    public final Format format;
    public final ComponentMapping components;
    public final ImageSubresourceRange subresourceRange;
    public final Image image;

    public ImageViewCreateInfo(Image image, ImageViewType type, Format format, ComponentMapping components,
            ImageSubresourceRange subresourceRange) {
        this.image = image;
        this.type = type;
        this.format = format;
        this.components = components;
        this.subresourceRange = subresourceRange;
    }

}
