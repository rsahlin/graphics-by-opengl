package com.nucleus.opengl.lwjgl3;

import org.lwjgl.opengles.GLESCapabilities;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.vulkan.Vulkan10Wrapper;

public class LWJGLWrapperFactory {

    /**
     * Creates the GLES wrapper
     * 
     * @param version Version and type of wrapper backend to create
     * @return
     */
    public static GLES20Wrapper createGLESWrapper(Renderers version) {
        switch (version) {
            case GLES20:
                return new LWJGL3GLES20Wrapper(Renderers.GLES20);
            case GLES30:
                return new LWJGL3GLES30Wrapper(Renderers.GLES30);
            case GLES31:
                return new LWJGL3GLES31Wrapper(Renderers.GLES31);
            case GLES32:
                return new LWJGL3GLES32Wrapper(Renderers.GLES32);
            default:
                throw new IllegalArgumentException("Not implemented for " + version);
        }
    }

    /**
     * Creates the Vulkan wrapper
     * 
     * @param version
     * @return
     */
    public static Vulkan10Wrapper createVulkanWrapper(Renderers version) {
        switch (version) {
            case VULKAN11:
                return new LWJGL3Vulkan11Wrapper(version);
            default:
                throw new IllegalArgumentException("Not implemented for " + version);
        }

    }

    /**
     * Returns the highest available GLES version from the capabilities
     * 
     * @param caps
     * @return
     */
    public static Renderers getGLESVersion(GLESCapabilities caps) {
        if (caps != null) {
            if (caps.GLES32) {
                return Renderers.GLES32;
            }
            if (caps.GLES31) {
                return Renderers.GLES31;
            }
            if (caps.GLES30) {
                return Renderers.GLES30;
            }
            if (caps.GLES20) {
                return Renderers.GLES20;
            }
        }
        throw new IllegalArgumentException("No gles support");
    }

}
