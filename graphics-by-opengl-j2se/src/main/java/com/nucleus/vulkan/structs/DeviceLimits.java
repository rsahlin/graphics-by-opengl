package com.nucleus.vulkan.structs;

/**
 * Agnostic abstraction of DeviceLimits
 *
 */
public abstract class DeviceLimits {

    protected DeviceLimits() {

    }

    protected int maxImageDimension1D;
    protected int maxImageDimension2D;
    protected int maxImageDimension3D;
    protected int maxImageDimensionCube;
    protected int maxImageArrayLayers;
    protected int maxTexelBufferElements;
    protected int maxUniformBufferRange;
    protected int maxStorageBufferRange;
    protected int maxPushConstantsSize;
    protected int maxMemoryAllocationCount;
    protected int maxSamplerAllocationCount;
    protected long bufferImageGranularity;
    protected long sparseAddressSpaceSize;
    protected int maxBoundDescriptorSets;
    protected int maxPerStageDescriptorSamplers;
    protected int maxPerStageDescriptorUniformBuffers;
    protected int maxPerStageDescriptorStorageBuffers;
    protected int maxPerStageDescriptorSampledImages;
    protected int maxPerStageDescriptorStorageImages;
    protected int maxPerStageDescriptorInputAttachments;
    protected int maxPerStageResources;
    protected int maxDescriptorSetSamplers;
    protected int maxDescriptorSetUniformBuffers;
    protected int maxDescriptorSetUniformBuffersDynamic;
    protected int maxDescriptorSetStorageBuffers;
    protected int maxDescriptorSetStorageBuffersDynamic;
    protected int maxDescriptorSetSampledImages;
    protected int maxDescriptorSetStorageImages;
    protected int maxDescriptorSetInputAttachments;
    protected int maxVertexInputAttributes;
    protected int maxVertexInputBindings;
    protected int maxVertexInputAttributeOffset;
    protected int maxVertexInputBindingStride;
    protected int maxVertexOutputComponents;
    protected int maxTessellationGenerationLevel;
    protected int maxTessellationPatchSize;
    protected int maxTessellationControlPerVertexInputComponents;
    protected int maxTessellationControlPerVertexOutputComponents;
    protected int maxTessellationControlPerPatchOutputComponents;
    protected int maxTessellationControlTotalOutputComponents;
    protected int maxTessellationEvaluationInputComponents;
    protected int maxTessellationEvaluationOutputComponents;
    protected int maxGeometryShaderInvocations;
    protected int maxGeometryInputComponents;
    protected int maxGeometryOutputComponents;
    protected int maxGeometryOutputVertices;
    protected int maxGeometryTotalOutputComponents;
    protected int maxFragmentInputComponents;
    protected int maxFragmentOutputAttachments;
    protected int maxFragmentDualSrcAttachments;
    protected int maxFragmentCombinedOutputResources;
    protected int maxComputeSharedMemorySize;
    protected int[] maxComputeWorkGroupCount = new int[3];
    protected int maxComputeWorkGroupInvocations;
    protected int[] maxComputeWorkGroupSize = new int[3];
    protected int subPixelPrecisionBits;
    protected int subTexelPrecisionBits;
    protected int mipmapPrecisionBits;
    protected int maxDrawIndexedIndexValue;
    protected int maxDrawIndirectCount;
    protected float maxSamplerLodBias;
    protected float maxSamplerAnisotropy;
    protected int maxViewports;
    protected int[] maxViewportDimensions = new int[2];
    protected float[] viewportBoundsRange = new float[2];
    protected int viewportSubPixelBits;
    protected int minMemoryMapAlignment;
    protected long minTexelBufferOffsetAlignment;
    protected long minUniformBufferOffsetAlignment;
    protected long minStorageBufferOffsetAlignment;
    protected int minTexelOffset;
    protected int maxTexelOffset;
    protected int minTexelGatherOffset;
    protected int maxTexelGatherOffset;
    protected float minInterpolationOffset;
    protected float maxInterpolationOffset;
    protected int subPixelInterpolationOffsetBits;
    protected int maxFramebufferWidth;
    protected int maxFramebufferHeight;
    protected int maxFramebufferLayers;
    protected SampleCountFlags framebufferColorSampleCounts;
    protected SampleCountFlags framebufferDepthSampleCounts;
    protected SampleCountFlags framebufferStencilSampleCounts;
    protected SampleCountFlags framebufferNoAttachmentsSampleCounts;
    protected int maxColorAttachments;
    protected SampleCountFlags sampledImageColorSampleCounts;
    protected SampleCountFlags sampledImageIntegerSampleCounts;
    protected SampleCountFlags sampledImageDepthSampleCounts;
    protected SampleCountFlags sampledImageStencilSampleCounts;
    protected SampleCountFlags storageImageSampleCounts;
    protected int maxSampleMaskWords;
    protected boolean timestampComputeAndGraphics;
    protected float timestampPeriod;
    protected int maxClipDistances;
    protected int maxCullDistances;
    protected int maxCombinedClipAndCullDistances;
    protected int discreteQueuePriorities;
    protected float[] pointSizeRange = new float[2];
    protected float[] lineWidthRange = new float[2];
    protected float pointSizeGranularity;
    protected float lineWidthGranularity;
    protected boolean strictLines;
    protected boolean standardSampleLocations;
    protected long optimalBufferCopyOffsetAlignment;
    protected long optimalBufferCopyRowPitchAlignment;
    protected long nonCoherentAtomSize;
}