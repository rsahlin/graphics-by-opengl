package com.nucleus.shader;

/**
 * Holds source and data related to the source for a shader
 *
 */
public class ShaderSource {

    /**
     * The full sourcename
     */
    protected String sourceName;
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
     * 
     * @param sourceName
     * @param versionedSource
     * @param versionString The version string -AS IS DEFINED IN SOURCE - KEEP WHITESPACE CHARS. Size will be used to
     * substitute version String if needed.
     * @param type
     */
    public ShaderSource(String sourceName, String versionedSource, String versionString, int type) {
        this.sourceName = sourceName;
        this.versionedSource = versionedSource;
        this.versionString = versionString;
        this.type = type;
    }

}
