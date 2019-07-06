package com.nucleus.vulkan.structs;

/**
 * Agnostic abstraction of DeviceLimits
 *
 */
public abstract class DeviceLimits {

    DeviceLimits() {

    }

    int maxImageDimension1D;
    int maxImageDimension2D;
    int maxImageDimension3D;
    int maxImageDimensionCube;
    int maxImageArrayLayers;
    int maxTexelBufferElements;
    int maxUniformBufferRange;
    int maxStorageBufferRange;
    int maxPushConstantsSize;
    int maxMemoryAllocationCount;
    int maxSamplerAllocationCount;
    long bufferImageGranularity;
    long sparseAddressSpaceSize;
    int maxBoundDescriptorSets;
    int maxPerStageDescriptorSamplers;
    int maxPerStageDescriptorUniformBuffers;
    int maxPerStageDescriptorStorageBuffers;
    int maxPerStageDescriptorSampledImages;
    int maxPerStageDescriptorStorageImages;
    int maxPerStageDescriptorInputAttachments;
    int maxPerStageResources;
    int maxDescriptorSetSamplers;
    int maxDescriptorSetUniformBuffers;
    int maxDescriptorSetUniformBuffersDynamic;
    int maxDescriptorSetStorageBuffers;
    int maxDescriptorSetStorageBuffersDynamic;
    int maxDescriptorSetSampledImages;
    int maxDescriptorSetStorageImages;
    int maxDescriptorSetInputAttachments;
    int maxVertexInputAttributes;
    int maxVertexInputBindings;
    int maxVertexInputAttributeOffset;
    int maxVertexInputBindingStride;
    int maxVertexOutputComponents;
    int maxTessellationGenerationLevel;
    int maxTessellationPatchSize;
    int maxTessellationControlPerVertexInputComponents;
    int maxTessellationControlPerVertexOutputComponents;
    int maxTessellationControlPerPatchOutputComponents;
    int maxTessellationControlTotalOutputComponents;
    int maxTessellationEvaluationInputComponents;
    int maxTessellationEvaluationOutputComponents;
    int maxGeometryShaderInvocations;
    int maxGeometryInputComponents;
    int maxGeometryOutputComponents;
    int maxGeometryOutputVertices;
    int maxGeometryTotalOutputComponents;
    int maxFragmentInputComponents;
    int maxFragmentOutputAttachments;
    int maxFragmentDualSrcAttachments;
    int maxFragmentCombinedOutputResources;
    int maxComputeSharedMemorySize;
    int[] maxComputeWorkGroupCount = new int[3];
    int maxComputeWorkGroupInvocations;
    int[] maxComputeWorkGroupSize = new int[3];
    int subPixelPrecisionBits;
    int subTexelPrecisionBits;
    int mipmapPrecisionBits;
    int maxDrawIndexedIndexValue;
    int maxDrawIndirectCount;
    float maxSamplerLodBias;
    float maxSamplerAnisotropy;
    int maxViewports;
    int[] maxViewportDimensions = new int[2];
    float[] viewportBoundsRange = new float[2];
    int viewportSubPixelBits;
    int minMemoryMapAlignment;
    long minTexelBufferOffsetAlignment;
    long minUniformBufferOffsetAlignment;
    long minStorageBufferOffsetAlignment;
    int minTexelOffset;
    int maxTexelOffset;
    int minTexelGatherOffset;
    int maxTexelGatherOffset;
    float minInterpolationOffset;
    float maxInterpolationOffset;
    int subPixelInterpolationOffsetBits;
    int maxFramebufferWidth;
    int maxFramebufferHeight;
    int maxFramebufferLayers;
    SampleCountFlags framebufferColorSampleCounts;
    SampleCountFlags framebufferDepthSampleCounts;
    SampleCountFlags framebufferStencilSampleCounts;
    SampleCountFlags framebufferNoAttachmentsSampleCounts;
    int maxColorAttachments;
    SampleCountFlags sampledImageColorSampleCounts;
    SampleCountFlags sampledImageIntegerSampleCounts;
    SampleCountFlags sampledImageDepthSampleCounts;
    SampleCountFlags sampledImageStencilSampleCounts;
    SampleCountFlags storageImageSampleCounts;
    int maxSampleMaskWords;
    boolean timestampComputeAndGraphics;
    float timestampPeriod;
    int maxClipDistances;
    int maxCullDistances;
    int maxCombinedClipAndCullDistances;
    int discreteQueuePriorities;
    float[] pointSizeRange = new float[2];
    float[] lineWidthRange = new float[2];
    float pointSizeGranularity;
    float lineWidthGranularity;
    boolean strictLines;
    boolean standardSampleLocations;
    long optimalBufferCopyOffsetAlignment;
    long optimalBufferCopyRowPitchAlignment;
    long nonCoherentAtomSize;
}