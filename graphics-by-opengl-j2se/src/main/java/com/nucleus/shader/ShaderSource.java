package com.nucleus.shader;

import java.util.StringTokenizer;

/**
 * Holds source and data related to the source for a shader
 *
 */
public class ShaderSource {

    public enum ESSLVersion {
        VERSION100(100),
        VERSION300(300),
        VERSION310(310),
        VERSION320(320);

        public final int number;

        private ESSLVersion(int number) {
            this.number = number;
        }

    }

    public static String VERSION = "#version";
    public static String ES = "es";
    public static String SHADING_LANGUAGE_100 = "100";

    /**
     * The sourcename - excluding source name version
     */
    private String sourceName;
    /**
     * If source is versioned, eg using _v300
     */
    private String sourceNameVersion;
    /**
     * The correctly versioned shader source
     */
    protected String versionedSource;
    /**
     * The shader version string, if defined
     */
    protected String versionString;

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
     * Sets the versioned source and the versionString, if source does not contain #version the versionString will be
     * null
     * 
     * @param versionedSource
     */
    public void setSource(String versionedSource) {
        this.versionedSource = versionedSource;
        versionString = hasVersion(versionedSource);
    }

    /**
     * Returns the shader version number, of not defined 100 is returned (for ESSL 1.0)
     * 
     * @return
     */
    public int getVersionNumber() {
        if (versionString != null) {
            return Integer.parseInt(versionString.substring(VERSION.length()).trim());
        }
        return ESSLVersion.VERSION100.number;
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

}
