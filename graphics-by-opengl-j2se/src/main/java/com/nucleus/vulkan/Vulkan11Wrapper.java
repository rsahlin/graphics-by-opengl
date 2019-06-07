package com.nucleus.vulkan;

import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * Wrapper for Vulkan 1.1 functionality
 *
 */
public abstract class Vulkan11Wrapper extends Vulkan10Wrapper {

    protected Vulkan11Wrapper(Renderers version) {
        super(version);
    }

}
