package com.nucleus.vulkan;

import com.nucleus.Backend;
import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * The Vulkan wrapper -all things related to Vulkan functionality that is shared independently of version
 * 
 * @author rsa1lud
 *
 */
public abstract class VulkanWrapper extends Backend {

    /**
     * The Vulkan API version
     * #define VK_VERSION_MAJOR(version) ((int)(version) >> 22)
     * #define VK_VERSION_MINOR(version) (((int)(version) >> 12) & 0x3ff)
     * #define VK_VERSION_PATCH(version) ((int)(version) & 0xfff)
     *
     */
    public class APIVersion {
        public final int major;
        public final int minor;
        public final int patch;

        public APIVersion(int api) {
            this.major = api >> 22;
            this.minor = (api >> 12) & 0x3ff;
            this.patch = (api & 0x0fff);
        }

        @Override
        public String toString() {
            return major + "." + minor + " patch " + patch;
        }

    }

    /**
     * Abstraction VkPhysicalDevice for Khronos Vulkan physical device
     *
     */
    public interface PhysicalDevice {
        /**
         * Returns the physical device properties
         * 
         * @return
         */
        public PhysicalDeviceProperties getProperties();

        /**
         * Returns the physical device features
         * 
         * @return
         */
        public PhysicalDeviceFeatures getFeatures();

    }

    /**
     * Abstraction of VkPhysicalDeviceProperties for physical device properties
     *
     */
    public interface PhysicalDeviceProperties {

        /**
         * Returns the device type
         * 
         * @return The VkPhysicalDeviceType of the device
         */
        public VkPhysicalDeviceType getDeviceType();

        /**
         * Returns the name of the device
         * 
         * @return
         */
        public String getDeviceName();

        /**
         * Returns the API version as defined in VkPhysicalDevice
         * 
         * @return
         */
        public APIVersion getAPIVersion();

        /**
         * Returns the device limits
         * 
         * @return
         */
        public DeviceLimits getLimits();

    }

    /**
     * 
     * Abstraction of VkPhysicalDeviceFeatures for physical device features
     *
     */
    public static class PhysicalDeviceFeatures {
        boolean robustBufferAccess;
        boolean fullDrawIndexUint32;
        boolean imageCubeArray;
        boolean independentBlend;
        boolean geometryShader;
        boolean tessellationShader;
        boolean sampleRateShading;
        boolean dualSrcBlend;
        boolean logicOp;
        boolean multiDrawIndirect;
        boolean drawIndirectFirstInstance;
        boolean depthClamp;
        boolean depthBiasClamp;
        boolean fillModeNonSolid;
        boolean depthBounds;
        boolean wideLines;
        boolean largePoints;
        boolean alphaToOne;
        boolean multiViewport;
        boolean samplerAnisotropy;
        boolean textureCompressionETC2;
        boolean textureCompressionASTC_LDR;
        boolean textureCompressionBC;
        boolean occlusionQueryPrecise;
        boolean pipelineStatisticsQuery;
        boolean vertexPipelineStoresAndAtomics;
        boolean fragmentStoresAndAtomics;
        boolean shaderTessellationAndGeometryPointSize;
        boolean shaderImageGatherExtended;
        boolean shaderStorageImageExtendedFormats;
        boolean shaderStorageImageMultisample;
        boolean shaderStorageImageReadWithoutFormat;
        boolean shaderStorageImageWriteWithoutFormat;
        boolean shaderUniformBufferArrayDynamicIndexing;
        boolean shaderSampledImageArrayDynamicIndexing;
        boolean shaderStorageBufferArrayDynamicIndexing;
        boolean shaderStorageImageArrayDynamicIndexing;
        boolean shaderClipDistance;
        boolean shaderCullDistance;
        boolean shaderFloat64;
        boolean shaderInt64;
        boolean shaderInt16;
        boolean shaderResourceResidency;
        boolean shaderResourceMinLod;
        boolean sparseBinding;
        boolean sparseResidencyBuffer;
        boolean sparseResidencyImage2D;
        boolean sparseResidencyImage3D;
        boolean sparseResidency2Samples;
        boolean sparseResidency4Samples;
        boolean sparseResidency8Samples;
        boolean sparseResidency16Samples;
        boolean sparseResidencyAliased;
        boolean variableMultisampleRate;
        boolean inheritedQueries;
    }

    public static class SampleCountFlags {

        public SampleCountFlags(int bits) {
            this.bits = bits;
        }

        public enum VkSampleCountFlagBits {
            VK_SAMPLE_COUNT_1_BIT(1),
            VK_SAMPLE_COUNT_2_BIT(2),
            VK_SAMPLE_COUNT_4_BIT(4),
            VK_SAMPLE_COUNT_8_BIT(8),
            VK_SAMPLE_COUNT_16_BIT(16),
            VK_SAMPLE_COUNT_32_BIT(32),
            VK_SAMPLE_COUNT_64_BIT(64);

            public final int samples;

            private VkSampleCountFlagBits(int samples) {
                this.samples = samples;
            }
        };

        int bits;
    }

    public static abstract class DeviceLimits {

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

    public enum VkPhysicalDeviceType {
        VK_PHYSICAL_DEVICE_TYPE_OTHER(0),
        VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1),
        VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2),
        VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3),
        VK_PHYSICAL_DEVICE_TYPE_CPU(4);

        public final int value;

        private VkPhysicalDeviceType(int value) {
            this.value = value;
        }

        public static VkPhysicalDeviceType get(int value) {
            for (VkPhysicalDeviceType type : VkPhysicalDeviceType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

    };

    public enum VkQueueFlagBits {
        VK_QUEUE_GRAPHICS_BIT(1),
        VK_QUEUE_COMPUTE_BIT(2),
        VK_QUEUE_TRANSFER_BIT(4),
        VK_QUEUE_SPARSE_BINDING_BIT(8),
        VK_QUEUE_PROTECTED_BIT(16);

        public final int mask;

        private VkQueueFlagBits(int mask) {
            this.mask = mask;
        }

        public static String toString(int flags) {
            StringBuffer result = new StringBuffer();
            for (VkQueueFlagBits bits : VkQueueFlagBits.values()) {
                if ((flags & bits.mask) != 0) {
                    result.append(result.length() > 0 ? " | " + bits.name() : bits.name());
                }
            }
            return result.toString();
        }

    };

    protected VulkanWrapper(Renderers version) {
        super(version);
    }
}
