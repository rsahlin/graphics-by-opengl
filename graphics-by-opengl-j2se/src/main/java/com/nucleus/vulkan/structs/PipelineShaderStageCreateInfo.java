package com.nucleus.vulkan.structs;

import java.nio.ByteBuffer;

import com.nucleus.vulkan.Vulkan10.PipelineCreateFlagBits;
import com.nucleus.vulkan.Vulkan10.ShaderStageFlagBits;

/**
 * Wrapper for VkPipelineShaderStageCreateInfo
 */
public class PipelineShaderStageCreateInfo {

    public static class SpecializationMapEntry {
        int constantID;
        int offset;
        int size;
    }

    public static class SpecializationInfo {
        int mapEntryCount;
        SpecializationMapEntry[] mapEntries;
        int dataSize;
        ByteBuffer data;
    }

    private final PipelineCreateFlagBits[] flags;
    private final ShaderStageFlagBits stage;
    private final ShaderModule module;
    private final String name;
    private final SpecializationInfo specializationInfo;

    public PipelineShaderStageCreateInfo(PipelineCreateFlagBits[] flags,
            ShaderStageFlagBits stage, ShaderModule module, String name, SpecializationInfo specializationInfo) {
        this.flags = flags;
        this.stage = stage;
        this.module = module;
        this.name = name;
        this.specializationInfo = specializationInfo;
    }
}
