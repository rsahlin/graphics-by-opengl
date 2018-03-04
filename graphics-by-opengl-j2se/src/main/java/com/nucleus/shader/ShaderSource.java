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

    public ShaderSource(String sourceName, String versionedSource, String versionString, int type) {
        this.sourceName = sourceName;
        this.versionedSource = versionedSource;
        this.versionString = versionString;
        this.type = type;
    }

}
