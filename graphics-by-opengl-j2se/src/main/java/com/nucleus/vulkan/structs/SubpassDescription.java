package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.PipelineBindPoint;
import com.nucleus.vulkan.Vulkan10.SubpassDescriptionFlagBits;

/**
 * Wrapper for VkSubpassDescription
 *
 */
public class SubpassDescription {
    SubpassDescriptionFlagBits[] flags;
    PipelineBindPoint pipelineBindPoint;
    AttachmentReference[] inputAttachments;
    AttachmentReference[] colorAttachments;
    AttachmentReference[] resolveAttachments;
    AttachmentReference[] depthStencilAttachment;
    int[] preserveAttachments;

}
