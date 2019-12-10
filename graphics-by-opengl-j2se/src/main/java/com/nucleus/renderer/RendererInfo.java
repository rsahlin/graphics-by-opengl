package com.nucleus.renderer;

import java.util.List;
import java.util.StringTokenizer;

import com.nucleus.renderer.NucleusRenderer.Renderers;

/**
 * Info about the renderer in the system.
 *
 */
public abstract class RendererInfo {

    /**
     * From gles spec:
     * The version number uses one of these forms:
     * major_number.minor_number major_number.minor_number.release_number
     * Vendor-specific information may follow the version number.
     * Its format depends on the implementation, but a space always separates the version number and the vendor-specific
     * information.
     *
     */
    public static class Version {
        public final int major;
        public final int minor;

        public Version(String versionStr) {
            int offset = 0;
            int whitespace = 0;
            String result = null;
            while ((whitespace = versionStr.indexOf(" ", offset)) != -1) {
                result = versionStr.substring(offset, whitespace);
                if (result.contains(".")) {
                    break;
                }
                offset = whitespace + 1;
            }
            if (whitespace == -1) {
                result = versionStr.substring(offset);
            }
            int dotIndex = result.indexOf(".");
            major = Integer.parseInt(result.substring(0, dotIndex));
            // Check for release number
            StringTokenizer st = new StringTokenizer(result.substring(dotIndex + 1));
            minor = Integer.parseInt(st.nextToken());
        }
    }

    protected String vendor;
    protected String renderer;
    protected String version;
    protected Version GLVersion;
    protected Version shadingLanguageVersion;
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
     * Returns the highest shader language version supported
     * 
     * @return
     */
    public Version getShadingLanguageVersion() {
        return shadingLanguageVersion;
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
