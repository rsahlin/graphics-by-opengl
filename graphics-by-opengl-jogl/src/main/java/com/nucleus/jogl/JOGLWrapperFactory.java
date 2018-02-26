package com.nucleus.jogl;

import com.jogamp.opengl.GLContext;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;

public class JOGLWrapperFactory {

    public static GLES20Wrapper createWrapper(Renderers version, GLContext glContext) {
        switch (version) {
            case GLES20:
                return new JOGLGLES20Wrapper(glContext.getGL().getGL2ES2(), version);
            case GLES30:
            case GLES31:
                return new JOGLGLES30Wrapper(glContext.getGL().getGL4ES3(), version);
            default:
                throw new IllegalArgumentException("Not imeplemented for " + version);
        }
    }

}
