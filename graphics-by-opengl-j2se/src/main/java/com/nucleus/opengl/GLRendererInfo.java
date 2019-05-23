package com.nucleus.opengl;

import com.nucleus.SimpleLogger;
import com.nucleus.common.StringUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.RendererInfo;

/**
 * The renderer info for GL/GLES based renderers
 *
 */
public class GLRendererInfo extends RendererInfo {

    /**
     * Fetches info from GLES and stores in this class.
     * 
     * @param gles
     * @param renderVersion
     */
    public GLRendererInfo(GLES20Wrapper gles, Renderers renderVersion) {
        super(renderVersion);
        vendor = gles.glGetString(GLES20.GL_VENDOR);
        version = gles.glGetString(GLES20.GL_VERSION);
        renderer = gles.glGetString(GLES20.GL_RENDERER);
        String shadingLanguageVersion = gles.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
        String glString = gles.glGetString(GLES20.GL_EXTENSIONS);
        if (glString != null) {
            extensions = StringUtils.getList(glString, " ");
        }
        int[] param = new int[1];
        gles.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, param);
        maxTextureSize = param[0];
        SimpleLogger.d(getClass(), "GLInfo:\n" + "GLES Version: " + version + " with shading language "
                + shadingLanguageVersion + "\n" + vendor + " " + renderer + ", max texture size: " + maxTextureSize);
        if (extensions != null) {
            StringUtils.logList(getClass().getCanonicalName(), extensions);
        }
        // Some implementations may raise error in glGetString for some unknown reason (LWJGL) - clear any raised errors
        // here
        while (gles.glGetError() != GLES20.GL_NO_ERROR) {
        }
    }

}
