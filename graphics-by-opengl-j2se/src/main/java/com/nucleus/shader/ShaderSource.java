package com.nucleus.shader;

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

    /**
     * 
     * @param sourceName
     * @param sourceNameVersion The source name version info, eg "_300"
     * @param versionedSource
     * @param versionString The version string -AS IS DEFINED IN SOURCE - KEEP WHITESPACE CHARS. Size will be used to
     * substitute version String if needed.
     * @param type
     */
    public ShaderSource(String sourceName, String sourceNameVersion, String versionedSource, String versionString,
            int type) {
        this.sourceName = sourceName;
        this.versionedSource = versionedSource;
        this.versionString = versionString;
        this.type = type;
    }

    public String getSourceNameVersion() {
        return sourceNameVersion;
    }

    public String getFullSourceName() {
        return sourceName + sourceNameVersion + ShaderProgram.SHADER_SOURCE_SUFFIX;
    }

    public void setSource(String versionedSource) {
        this.versionedSource = versionedSource;
    }

    /**
     * Returns the shader version number, of not defined 100 is returned (for ESSL 1.0)
     * 
     * @return
     */
    public int getVersionNumber() {
        if (versionString != null) {
            return Integer.parseInt(versionString.substring(VERSION.length()));
        }
        return ESSLVersion.VERSION100.number;
    }

}
