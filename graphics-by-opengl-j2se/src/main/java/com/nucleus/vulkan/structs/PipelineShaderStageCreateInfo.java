package com.nucleus.vulkan.structs;

import java.nio.ByteBuffer;

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

    private final int flags;
    private final int stage;
    private final ShaderModule module;
    private final String name;
    private final SpecializationInfo specializationInfo;

    public PipelineShaderStageCreateInfo(int flags, int stage, ShaderModule module, String name,
            SpecializationInfo specializationInfo) {
        this.flags = flags;
        this.stage = stage;
        this.module = module;
        this.name = name;
        this.specializationInfo = specializationInfo;
    }
}
