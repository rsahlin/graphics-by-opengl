package com.nucleus.vulkan.structs;

import com.nucleus.vulkan.Vulkan10.CompareOp;

public class StencilOpState {

    public enum StencilOp {
        VK_STENCIL_OP_KEEP(0),
        VK_STENCIL_OP_ZERO(1),
        VK_STENCIL_OP_REPLACE(2),
        VK_STENCIL_OP_INCREMENT_AND_CLAMP(3),
        VK_STENCIL_OP_DECREMENT_AND_CLAMP(4),
        VK_STENCIL_OP_INVERT(5),
        VK_STENCIL_OP_INCREMENT_AND_WRAP(6),
        VK_STENCIL_OP_DECREMENT_AND_WRAP(7),
        VK_STENCIL_OP_MAX_ENUM(0x7FFFFFFF);
        public final int value;

        private StencilOp(int value) {
            this.value = value;
        }
    }

    StencilOp failOp;
    StencilOp passOp;
    StencilOp depthFailOp;
    CompareOp compareOp;
    int compareMask;
    int writeMask;
    int reference;

}
