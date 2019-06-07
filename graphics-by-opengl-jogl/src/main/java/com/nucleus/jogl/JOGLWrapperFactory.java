package com.nucleus.jogl;

import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.nucleus.Backend;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * Factory for GLES wrapper creation
 *
 */
public class JOGLWrapperFactory implements BackendFactory {

    @Override
    public Backend createBackend(Renderers version, Object window, Object context) {
        if (context == null || !(context instanceof GLContext)) {
            throw new IllegalArgumentException("Invalid context, null or not GLContext: " + context);
        }
        GLContext glContext = (GLContext) context;
        GLProfile profile = glContext.getGL().getGLProfile();
        if (profile.isGL4ES3()) {
            return new JOGLGLES32Wrapper(glContext.getGL().getGL4ES3(), Renderers.GLES32);
        }
        if (profile.isGL2ES2() || profile.isGL2ES2()) {
            return new JOGLGLES20Wrapper(glContext.getGL().getGL2ES2(), Renderers.GLES20);
        }
        throw new IllegalArgumentException("Could not find support for GLES from " + profile.getName());
    }

}
