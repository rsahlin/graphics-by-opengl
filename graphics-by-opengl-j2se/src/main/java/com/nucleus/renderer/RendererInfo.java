package com.nucleus.renderer;

import java.util.List;

import com.nucleus.SimpleLogger;
import com.nucleus.common.StringUtils;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSIONS;

/**
 * Info about the renderer in the system.
 * 
 * @author Richard Sahlin
 *
 */
public class RendererInfo {

    private String vendor;
    private String renderer;
    private String version;
    private String shadingLanguageVersion;
    private List<String> extensions;
    private int maxTextureSize;

    /**
     * Fetches info from GLES and stores in this class.
     * 
     * @param gles
     */
    public RendererInfo(GLES20Wrapper gles) {
        vendor = gles.glGetString(GLES20.GL_VENDOR);
        version = gles.glGetString(GLES20.GL_VERSION);
        renderer = gles.glGetString(GLES20.GL_RENDERER);
        shadingLanguageVersion = gles.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
        String glString = gles.glGetString(GLES20.GL_EXTENSIONS);
        if (glString != null) {
            extensions = StringUtils.getList(glString, " ");
        }
        int[] param = new int[1];
        gles.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, param);
        maxTextureSize = param[0];
        SimpleLogger.d(getClass(), "GLInfo:\n" + "GLES Version: " + version + " with shading language "
                + shadingLanguageVersion + "\n" + vendor + " " + renderer + ", max texture size: " + maxTextureSize);
        StringUtils.logList(getClass().getCanonicalName(), extensions);
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
     * OpenGL ES GLSL ES <version number> <vendor-specific information>.
     * 
     * @return
     */
    public String getShadingLanguageVersion() {
        return shadingLanguageVersion;
    }

    /**
     * Returns true if the platform has support for the specified extension.
     * 
     * @param extension The extension to check for
     * @return True if the platform has support for the extension
     */
    public boolean hasExtensionSupport(String extension) {
        if (extensions != null && extensions.contains(extension)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the platform has support for the extension.
     * Will check for extension match regardless of extension prefix like GL_EXT_ etc
     * 
     * @param extension
     * @return
     */
    public boolean hasExtensionSupport(GLES_EXTENSIONS extension) {
        return extension == null ? false : hasExtensionNoPrefix(extension.name());
    }

    /**
     * Checks if there is support for extension - excluding prefix like GL_EXT_, GL_ARB_ etc
     * 
     * @param extension
     * @return
     */
    private boolean hasExtensionNoPrefix(String extension) {
        for (String str : extensions) {
            int index = str.indexOf('_', 3) + 1;
            if (str.substring(index).equals(extension)) {
                return true;
            }
        }
        return false;
    }

}
