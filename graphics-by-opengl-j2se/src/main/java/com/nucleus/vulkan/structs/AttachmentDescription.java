package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.AttachmentLoadOp;
import com.nucleus.vulkan.Vulkan10.AttachmentStoreOp;
import com.nucleus.vulkan.Vulkan10.Format;
import com.nucleus.vulkan.Vulkan10.ImageLayout;
import com.nucleus.vulkan.Vulkan10.SampleCountFlagBits;

/**
 * Wrapper for VkAttachmentDescription
 *
 */
public class AttachmentDescription {

    public enum AttachmentDescriptionFlagBits {
        VK_ATTACHMENT_DESCRIPTION_MAY_ALIAS_BIT(0x00000001),
        VK_ATTACHMENT_DESCRIPTION_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);
        public final int value;

        private AttachmentDescriptionFlagBits(int value) {
            this.value = value;
        }
    }

    AttachmentDescriptionFlagBits[] flags;
    Format format;
    SampleCountFlagBits samples;
    AttachmentLoadOp loadOp;
    AttachmentStoreOp storeOp;
    AttachmentLoadOp stencilLoadOp;
    AttachmentStoreOp stencilStoreOp;
    ImageLayout initialLayout;
    ImageLayout finalLayout;

}
