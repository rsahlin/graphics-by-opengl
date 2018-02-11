package com.nucleus.lwjgl3;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;

public class LWJGLWrapperFactory {

    public static GLES20Wrapper createWrapper(Renderers version) {
        switch (version) {
            case GLES30:
                return new LWJGL3GLES30Wrapper();
            case GLES31:
                return new LWJGL3GLES31Wrapper();
            default:
                throw new IllegalArgumentException("Not imeplemented for " + version);
        }
    }

}
