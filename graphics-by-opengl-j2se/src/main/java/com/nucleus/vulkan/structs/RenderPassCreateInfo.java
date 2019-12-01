package com.nucleus.vulkan.structs;

/**
 * Wrapper for VkRenderPassCreateInfo
 */
public class RenderPassCreateInfo {
    // Reserved for future use - VkRenderPassCreateFlags flags;
    AttachmentDescription[] attachments;
    SubpassDescription[] subpasses;
    SubpassDependency[] dependencies;

}
