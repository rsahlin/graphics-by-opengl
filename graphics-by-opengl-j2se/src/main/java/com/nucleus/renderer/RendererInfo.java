package com.nucleus.renderer;

import java.util.List;
import java.util.StringTokenizer;

import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * Info about the renderer in the system.
 *
 */
public abstract class RendererInfo {

    protected String vendor;
    protected String renderer;
    protected String version;
    protected List<String> extensions;
    protected int maxTextureSize;
    protected Renderers renderVersion;

    /**
     * Internal constructor - do not use
     */
    protected RendererInfo(Renderers renderVersion) {
        this.renderVersion = renderVersion;
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
     * Returns the renderer version, eg GLES2.0, GLES30
     * 
     * @return
     */
    public Renderers getRenderVersion() {
        return renderVersion;
    }

    /**
     * Returns true if the platform has support for the specified extension.
     * 
     * @param extension The extension to check for
     * @return True if the platform has support for the extension
     */
    public boolean hasExtensionSupport(String extension) {
        return extension == null || extensions == null ? false : hasExtensionNoPrefix(extension);
    }

    /**
     * Checks if there is support for extension - excluding prefix like GL_EXT_, GL_ARB_ etc
     * 
     * @param extension
     * @return
     */
    private boolean hasExtensionNoPrefix(String extension) {
        for (String str : extensions) {
            if (str.contains(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the String, navigate to first number char by locating whitespaces, and returns the major.minor version.
     * Eg, 'OPENGL ES 3.2' will return [3][2]
     * 
     * @param glVersion
     * @return Array with to int values for major.minor if version info was found, null otherwise
     */
    public static int[] getVersionStr(String glVersion) {
        StringTokenizer st = new StringTokenizer(glVersion);
        int[] result = null;
        while (result == null && st.hasMoreTokens()) {
            String token = st.nextToken();
            char chr = token.charAt(0);
            if (chr >= '0' && chr <= '9') {
                result = new int[2];
                float val = Float.parseFloat(token);
                result[0] = (int) val;
                result[1] = (int) ((val - result[0]) * 10);
            }
        }
        return result;
    }

}
