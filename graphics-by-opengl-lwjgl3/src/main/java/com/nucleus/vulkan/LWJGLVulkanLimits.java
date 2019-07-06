package com.nucleus.vulkan;

import org.lwjgl.vulkan.VkPhysicalDeviceLimits;

import com.nucleus.vulkan.structs.DeviceLimits;
import com.nucleus.vulkan.structs.SampleCountFlags;

public class LWJGLVulkanLimits extends DeviceLimits {

    public LWJGLVulkanLimits(VkPhysicalDeviceLimits limits) {
        super();
        maxImageDimension1D = limits.maxImageDimension1D();
        maxImageDimension2D = limits.maxImageDimension2D();
        maxImageDimension3D = limits.maxImageDimension3D();
        maxImageDimensionCube = limits.maxImageDimensionCube();
        framebufferColorSampleCounts = new SampleCountFlags(limits.framebufferColorSampleCounts());
        framebufferDepthSampleCounts = new SampleCountFlags(limits.framebufferDepthSampleCounts());
        framebufferStencilSampleCounts = new SampleCountFlags(limits.framebufferStencilSampleCounts());
        framebufferNoAttachmentsSampleCounts = new SampleCountFlags(limits.framebufferNoAttachmentsSampleCounts());
        sampledImageColorSampleCounts = new SampleCountFlags(limits.sampledImageColorSampleCounts());
        sampledImageDepthSampleCounts = new SampleCountFlags(limits.sampledImageDepthSampleCounts());
        sampledImageIntegerSampleCounts = new SampleCountFlags(limits.sampledImageIntegerSampleCounts());
        sampledImageStencilSampleCounts = new SampleCountFlags(limits.sampledImageStencilSampleCounts());
        maxComputeSharedMemorySize = limits.maxComputeSharedMemorySize();
        limits.maxComputeWorkGroupCount().get(maxComputeWorkGroupCount);
        maxComputeWorkGroupInvocations = limits.maxComputeWorkGroupInvocations();
        limits.maxComputeWorkGroupSize().get(maxComputeWorkGroupSize);

        maxFramebufferWidth = limits.maxFramebufferWidth();
        maxFramebufferHeight = limits.maxFramebufferHeight();
        maxColorAttachments = limits.maxColorAttachments();

    }

    @Override
    public String toString() {
        return "Max image 2D " + maxImageDimension2D + ", max framebuffer " + maxFramebufferWidth + ","
                + maxFramebufferHeight + ", max color attachements " + maxColorAttachments;
    }

}
