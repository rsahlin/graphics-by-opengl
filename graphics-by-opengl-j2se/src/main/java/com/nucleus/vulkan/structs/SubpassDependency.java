package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.AccessFlagBits;
import com.nucleus.vulkan.Vulkan10.DependencyFlagBits;
import com.nucleus.vulkan.Vulkan10.PipelineStageFlagBits;

/**
 * Wrapper for VkSubpassDependency
 *
 */
public class SubpassDependency {
    int srcSubpass;
    int dstSubpass;
    PipelineStageFlagBits[] srcStageMask;
    PipelineStageFlagBits[] dstStageMask;
    AccessFlagBits[] srcAccessMask;
    AccessFlagBits[] dstAccessMask;
    DependencyFlagBits[] dependencyFlags;

}
