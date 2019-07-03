package com.nucleus.vulkan;

/**
 * 
 * Abstraction of VkPhysicalDeviceFeatures for physical device features
 *
 */
public abstract class PhysicalDeviceFeatures {
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