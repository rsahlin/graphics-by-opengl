package com.nucleus.vulkan;

import com.nucleus.Backend;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.Vulkan10.Result;

/**
 * The Vulkan wrapper -all things related to Vulkan functionality that is shared independently of version
 * 
 * @author rsa1lud
 *
 */
public abstract class VulkanWrapper extends Backend {

    public interface VulkanDeviceSelector {
        /**
         * Vulkan implementation agnostic method to select the device to use
         * 
         * @param devices
         * @return The device to use or null if no device that can be used
         */
        public PhysicalDevice selectDevice(PhysicalDevice[] devices);

        /**
         * Used to select the queue instance of the device
         * 
         * @param device
         */
        public QueueFamilyProperties selectQueueInstance(PhysicalDevice device);

    }

    protected static VulkanDeviceSelector deviceSelector;

    /**
     * Sets the vulkan device selector - use this to override the default selector behavior
     * 
     * @param deviceSelector The device selector to be called when Vulkan wrapper is created, or null to remove.
     */
    public static void setVulkanDeviceSelector(VulkanDeviceSelector deviceSelector) {
        VulkanWrapper.deviceSelector = deviceSelector;
    }

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

        /**
         * Returns the supported queue families of the device
         * 
         * @return
         */
        public QueueFamilyProperties[] getQueueFamilyProperties();

        /**
         * Returns the supported extensions
         * 
         * @return
         */
        public ExtensionProperties[] getExtensions();

        /**
         * Returns the extension property if this device supportes the named extension
         * 
         * @param extensionName
         * @return The extension property, or null if not supported
         */
        public ExtensionProperties getExtension(String extensionName);

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
        public PhysicalDeviceType getDeviceType();

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
    public abstract static class PhysicalDeviceFeatures {
        public boolean robustBufferAccess;
        public boolean fullDrawIndexUint32;
        public boolean imageCubeArray;
        public boolean independentBlend;
        public boolean geometryShader;
        public boolean tessellationShader;
        public boolean sampleRateShading;
        public boolean dualSrcBlend;
        public boolean logicOp;
        public boolean multiDrawIndirect;
        public boolean drawIndirectFirstInstance;
        public boolean depthClamp;
        public boolean depthBiasClamp;
        public boolean fillModeNonSolid;
        public boolean depthBounds;
        public boolean wideLines;
        public boolean largePoints;
        public boolean alphaToOne;
        public boolean multiViewport;
        public boolean samplerAnisotropy;
        public boolean textureCompressionETC2;
        public boolean textureCompressionASTC_LDR;
        public boolean textureCompressionBC;
        public boolean occlusionQueryPrecise;
        public boolean pipelineStatisticsQuery;
        public boolean vertexPipelineStoresAndAtomics;
        public boolean fragmentStoresAndAtomics;
        public boolean shaderTessellationAndGeometryPointSize;
        public boolean shaderImageGatherExtended;
        public boolean shaderStorageImageExtendedFormats;
        public boolean shaderStorageImageMultisample;
        public boolean shaderStorageImageReadWithoutFormat;
        public boolean shaderStorageImageWriteWithoutFormat;
        public boolean shaderUniformBufferArrayDynamicIndexing;
        public boolean shaderSampledImageArrayDynamicIndexing;
        public boolean shaderStorageBufferArrayDynamicIndexing;
        public boolean shaderStorageImageArrayDynamicIndexing;
        public boolean shaderClipDistance;
        public boolean shaderCullDistance;
        public boolean shaderFloat64;
        public boolean shaderInt64;
        public boolean shaderInt16;
        public boolean shaderResourceResidency;
        public boolean shaderResourceMinLod;
        public boolean sparseBinding;
        public boolean sparseResidencyBuffer;
        public boolean sparseResidencyImage2D;
        public boolean sparseResidencyImage3D;
        public boolean sparseResidency2Samples;
        public boolean sparseResidency4Samples;
        public boolean sparseResidency8Samples;
        public boolean sparseResidency16Samples;
        public boolean sparseResidencyAliased;
        public boolean variableMultisampleRate;
        public boolean inheritedQueries;

        /**
         * Copy the the features into destination
         * 
         * @param destination Implementation specific destination
         */
        public abstract void copy(Object destination);

        @Override
        public String toString() {
            String result = (geometryShader ? "Has " : "No ") + "geometry shader, " +
                    (tessellationShader ? "Has " : "No ") + "tesseleation shader" +
                    "\n";
            return result;
        }

    }

    public static class SampleCountFlags {

        public SampleCountFlags(int bits) {
            this.bits = bits;
        }

        public enum SampleCountFlagBits {
            VK_SAMPLE_COUNT_1_BIT(1),
            VK_SAMPLE_COUNT_2_BIT(2),
            VK_SAMPLE_COUNT_4_BIT(4),
            VK_SAMPLE_COUNT_8_BIT(8),
            VK_SAMPLE_COUNT_16_BIT(16),
            VK_SAMPLE_COUNT_32_BIT(32),
            VK_SAMPLE_COUNT_64_BIT(64);

            public final int samples;

            private SampleCountFlagBits(int samples) {
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

    public static class ExtensionProperties {
        public ExtensionProperties(String name, int specVersion) {
            this.name = name;
            this.specVersion = specVersion;
        }

        String name;
        int specVersion;

        public String getName() {
            return name;
        }

    }

    public static class Extent3D {
        final int[] values = new int[3];

        public Extent3D(int width, int height, int depth) {
            values[0] = width;
            values[1] = height;
            values[2] = depth;
        }
    }

    public static abstract class QueueFamilyProperties {

        protected int queueIndex;
        protected int queueFlags;
        protected int queueCount;
        protected int timestampValidBits;
        protected Extent3D minImageTransferGranularity;
        protected boolean surfaceSupportsPresent;

        /**
         * Returns the queue support flags
         * 
         * @return
         */
        public int getQueueFlags() {
            return queueFlags;
        }

        /**
         * Returns true if queue has support for the queueFlag
         * 
         * @param queueFlag
         * @return
         */
        public boolean hasSupport(QueueFlagBits queueFlag) {
            return (queueFlag.mask & getQueueFlags()) != 0;
        }

        public boolean isSurfaceSupportsPresent() {
            return surfaceSupportsPresent;
        }

        public int getQueueIndex() {
            return queueIndex;
        }

        public int getQueueCount() {
            return queueCount;
        }

        @Override
        public String toString() {
            return QueueFlagBits.toString(getQueueFlags()) + " - surface present: " +
                    isSurfaceSupportsPresent() + "\n";
        }

    };

    public enum PhysicalDeviceType {
        VK_PHYSICAL_DEVICE_TYPE_OTHER(0),
        VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1),
        VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2),
        VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3),
        VK_PHYSICAL_DEVICE_TYPE_CPU(4);

        public final int value;

        private PhysicalDeviceType(int value) {
            this.value = value;
        }

        public static PhysicalDeviceType get(int value) {
            for (PhysicalDeviceType type : PhysicalDeviceType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

    };

    public enum QueueFlagBits {
        VK_QUEUE_GRAPHICS_BIT(1),
        VK_QUEUE_COMPUTE_BIT(2),
        VK_QUEUE_TRANSFER_BIT(4),
        VK_QUEUE_SPARSE_BINDING_BIT(8),
        VK_QUEUE_PROTECTED_BIT(16);

        public final int mask;

        private QueueFlagBits(int mask) {
            this.mask = mask;
        }

        public static String toString(int flags) {
            StringBuffer result = new StringBuffer();
            for (QueueFlagBits bits : QueueFlagBits.values()) {
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

    /**
     * Checks that the resultcode is VK_SUCCESS, if not RuntimeException is thrown.
     * 
     * @param value
     */
    public void assertResult(int value) {
        Result r = Result.getResult(value);
        {
            if (r == null || r != Result.VK_SUCCESS) {
                throw new RuntimeException("Failed with error: " + r);
            }
        }
    }

}
