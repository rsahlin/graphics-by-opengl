package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.BlendFactor;
import com.nucleus.vulkan.Vulkan10.BlendOp;

public class PipelineColorBlendAttachmentState {

    boolean blendEnable;
    BlendFactor srcColorBlendFactor;
    BlendFactor dstColorBlendFactor;
    BlendOp colorBlendOp;
    BlendFactor srcAlphaBlendFactor;
    BlendFactor dstAlphaBlendFactor;
    BlendOp alphaBlendOp;
    /**
     * Mask for ColorComponentFlagBits
     */
    int colorWriteMask;

}
