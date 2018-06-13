package com.nucleus.shader;

import java.util.StringTokenizer;

import com.nucleus.common.Constants;

/**
 * Holds source and data related to the source for a shader
 *
 */
public class ShaderSource {

    public enum ESSLVersion {
        VERSION100(100),
        VERSION300(300),
        VERSION310(310),
        VERSION320(320),
        VERSION430(430);

        public final int number;

        private ESSLVersion(int number) {
            this.number = number;
        }

        /**
         * Returns full version string, including #version
         * 
         * @return
         */
        public String getVersionString() {
            switch (this) {
                case VERSION100:
                    return VERSION + " 100";
                case VERSION300:
                case VERSION310:
                case VERSION320:
                    return VERSION + " " + Integer.toString(number) + " " + ES;
                case VERSION430:
                    return VERSION + " " + Integer.toString(number);
                default:
                    throw new IllegalArgumentException("Not implemented for " + this);
            }

        }

        /**
         * Finds the shader version from the version string
         * 
         * @param version Trimmed version string, including #version
         * @return
         */
        public static ESSLVersion getVersion(String version) {

            int v = ShaderSource.getVersionNumber(version);
            for (ESSLVersion essl : values()) {
                if (essl.number == v) {
                    return essl;
                }
            }
            return null;
        }

    }

    public static String VERSION = "#version";
    public static String ES = "es";
    public static String SHADING_LANGUAGE_100 = "100";

    /**
     * Use for shader source names that are versioned 300
     */
    public static final String V300 = "_v300";
    /**
     * Use for shader source names that are versioned 310
     */
    public static final String V310 = "_v310";
    /**
     * Use for shader source names that are versioned 320
     */
    public static final String V320 = "_v320";

    /**
     * The sourcename - excluding source name version
     */
    private String sourceName;
    /**
     * If source is versioned, eg using _v300
     */
    private String sourceNameVersion;

    /**
     * Shader source without #version
     */
    private String shaderSource;

    /**
     * The full shader version string, including #VERSION and ES as needed - if defined by calling
     * {@link #setShaderVersion(ESSLVersion)}
     */
    private String versionString;

    /**
     * Shader type
     */
    protected int type;

    /**
     * Creates shader source with name of source, including source name version
     * 
     * @param sourceName Name of source - excluding source name version
     * @param sourceNameVersion
     * @param type
     */
    public ShaderSource(String sourceName, String sourceNameVersion, int type) {
        this.sourceName = sourceName;
        this.sourceNameVersion = sourceNameVersion;
        this.type = type;
    }

    public String getSourceNameVersion() {
        return sourceNameVersion;
    }

    public String getFullSourceName() {
        return sourceName + sourceNameVersion + ShaderProgram.SHADER_SOURCE_SUFFIX;
    }

    /**
     * Returns the unversioned shader source
     * 
     * @return
     */
    public String getSource() {
        return shaderSource;
    }

    /**
     * Sets the shader source, if source is versioned the {@link #setShaderVersion(ESSLVersion)} method is called
     * and the raw source is set.
     * 
     * @param source
     */
    public void setSource(String source) {
        String version = hasVersion(source);
        if (version != null) {
            setShaderVersion(version);
        }
        this.shaderSource = source.substring(version.length());
    }

    /**
     * Sets the version string from the shading language version, next time {@link #getVersionedShaderSource()} is
     * called it will be versioned using this.
     * If null is specified nothing is done.
     * 
     * @param version
     */
    public void setShaderVersion(ESSLVersion version) {
        if (version != null) {
            versionString = version.getVersionString();
        }
    }

    /**
     * Internal method to set the version string
     * 
     * @param version Complete version string, with #version and ES if needed, eg '#version 300 es'
     */
    protected void setShaderVersion(String version) {
        this.versionString = version;
    }

    /**
     * Returns the shader source versioned for the shading version specified by calling
     * {@link #setShaderVersion(ESSLVersion)}
     * 
     * @return
     */
    public String getVersionedShaderSource() {
        return versionString + "\n" + shaderSource;
    }

    /**
     * Returns the shader version, or null if not defined
     * 
     * @return
     */
    public ESSLVersion getVersion() {
        if (versionString == null) {
            return null;
        }
        return ESSLVersion.getVersion(versionString);
    }

    /**
     * Returns the version number from the version string
     * 
     * @param version Version, including #version and ES if needed
     * @return Version number or -1 if version is null
     */
    public static int getVersionNumber(String version) {
        if (version != null) {
            // Check for ES index in version number.
            int index = version.indexOf(ES);
            if (index > 0) {
                return Integer.parseInt(version.substring(VERSION.length() + 1, index).trim());
            } else {
                return Integer.parseInt(version.substring(VERSION.length() + 1).trim());
            }
        }
        return Constants.NO_VALUE;

    }

    /**
     * Appends source at the end of this source.
     * 
     * @param source Unversioned shader source
     */
    public void appendSource(String source) {
        this.shaderSource += source;
    }

    /**
     * Checks if the first (non empty) line contains version, if so it is returned
     * 
     * @param source
     * @return The version string that is the full first line (excluding line separator char), eg "#version 310 es",
     * "#version 430" or null if no version.
     * The returned string can be used to calculate offset/length when substituting version.
     */
    public static String hasVersion(String source) {
        StringTokenizer st = new StringTokenizer(source, System.lineSeparator());
        String t = st.nextToken();
        if (t.trim().toLowerCase().startsWith(VERSION)) {
            return t;
        }
        return null;
    }

    /**
     * Returns the minimum shader version that must be supported for the sources.
     * Will return {@link ESSLVersion#VERSION100} if no version info is set in sources.
     * 
     * @param sources
     * @return
     */
    public static ESSLVersion getMinVersion(ShaderSource[] sources) {
        ESSLVersion minEssl = ESSLVersion.VERSION100;
        ESSLVersion essl = null;
        for (ShaderSource ss : sources) {
            if ((essl = ss.getVersion()).number > minEssl.number) {
                minEssl = essl;
            }
        }
        return minEssl;
    }

}
