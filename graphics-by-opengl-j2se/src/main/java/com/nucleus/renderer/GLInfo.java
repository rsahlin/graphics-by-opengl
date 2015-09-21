package com.nucleus.renderer;

import java.util.StringTokenizer;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * Info about the GL ES renderer in the system.
 * 
 * @author Richard Sahlin
 *
 */
public class GLInfo {

    private String vendor;
    private String renderer;
    private String version;
    private String shadingLanguageVersion;
    private String[] extensions;
    private int maxTextureSize;

    /**
     * Fetches info from GLES and stores in this class.
     * 
     * @param gles
     */
    public GLInfo(GLES20Wrapper gles) {
        vendor = gles.glGetString(GLES20.GL_VENDOR);
        version = gles.glGetString(GLES20.GL_VERSION);
        renderer = gles.glGetString(GLES20.GL_RENDERER);
        shadingLanguageVersion = gles.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
        StringTokenizer st = new StringTokenizer(gles.glGetString(GLES20.GL_EXTENSIONS), " ");
        int count = st.countTokens();
        if (count > 0) {
            extensions = new String[count];
            while (st.hasMoreTokens()) {
                extensions[--count] = st.nextToken();
            }
        }
        int[] param = new int[1];
        gles.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, param, 0);
        maxTextureSize = param[0];
        System.out.println("GLInfo:\n" + "GLES Version: " + version + " with shading language "
                + shadingLanguageVersion + "\n" + vendor + " " + renderer);

    }

    /**
     * Returns the company responsible for this GL implementation.
     * This name does not change from release to release.
     * 
     * @return
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Returns the name of the renderer. This name is typically specific to a particular configuration of a hardware
     * platform. It does not change from release to release.
     * 
     * @return
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * Returns a version or release number of the form OpenGL<space>ES<space><version number><space><vendor-specific
     * information>.
     * 
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns a version or release number for the shading language of the form
     * OpenGL<space>ES<space>GLSL<space>ES<space><version number><space><vendor-specific information>.
     * 
     * @return
     */
    public String getShadingLanguageVersion() {
        return shadingLanguageVersion;
    }

}
