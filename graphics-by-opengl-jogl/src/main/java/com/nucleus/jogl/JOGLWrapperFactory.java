package com.nucleus.jogl;

import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.Renderers;

/**
 * Factory for GLES wrapper creation
 *
 */
public class JOGLWrapperFactory {

    public static GLES20Wrapper createWrapper(Renderers version, GLContext glContext) {
        GLProfile profile = glContext.getGL().getGLProfile();
        if (profile.isGL4ES3()) {
            return new JOGLGLES30Wrapper(glContext.getGL().getGL4ES3(), Renderers.GLES30);
        }
        if (profile.isGL2ES2() || profile.isGL2ES2()) {
            return new JOGLGLES20Wrapper(glContext.getGL().getGL2ES2(), Renderers.GLES20);
        }
        throw new IllegalArgumentException("Could not find support for GLES from " + profile.getName());
    }

}
