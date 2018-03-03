package com.nucleus.lwjgl3;

import org.lwjgl.opengles.GLESCapabilities;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;

public class LWJGLWrapperFactory {

    /**
     * Creates the GLES wrapper - if caps is specified then the highest possible capabilites are returned.
     * 
     * @param caps If specified, the highest level of GLES support is returned.
     * @param version
     * @return
     */
    public static GLES20Wrapper createWrapper(GLESCapabilities caps, Renderers version) {

        if (caps != null) {
            if (caps.GLES31) {
                return new LWJGL3GLES31Wrapper(Renderers.GLES31);
            }
            if (caps.GLES30) {
                return new LWJGL3GLES30Wrapper(Renderers.GLES30);
            }
            if (caps.GLES20) {
                return new LWJGL3GLES20Wrapper(Renderers.GLES20);
            }
            throw new IllegalArgumentException("No gles support");
        }
        switch (version) {
            case GLES30:
                return new LWJGL3GLES30Wrapper(Renderers.GLES30);
            case GLES31:
                return new LWJGL3GLES31Wrapper(Renderers.GLES31);
            case GLES20:
                return new LWJGL3GLES20Wrapper(Renderers.GLES20);
            default:
                throw new IllegalArgumentException("Not imeplemented for " + version);
        }

    }

}
