package com.nucleus.vulkan;

import com.nucleus.renderer.Backend;
import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * The Vulkan wrapper -all things related to Vulkan functionality that is shared independently of version
 * 
 * @author rsa1lud
 *
 */
public abstract class VulkanWrapper extends Backend {

    protected VulkanWrapper(Renderers version) {
        super(version);
    }

}
