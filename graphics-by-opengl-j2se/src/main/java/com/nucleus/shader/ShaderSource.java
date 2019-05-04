package com.nucleus.shader;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.nucleus.common.Constants;
import com.nucleus.shader.ShaderProgram.ShaderType;

/**
 * Holds source and data related to the source for a shader
 *
 */
public class ShaderSource {

    public final static String FILE_SUFFIX_SEPARATOR = ".";

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
    public static String PRECISION = "precision";
    public static String DEFINE = "#define";

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

    private String suffix;

    /**
     * Shader type
     */
    protected ShaderType type;

    /**
     * Creates a shadersource from full sourcename - including any version and file suffix
     * 
     * @param sourcename Sourcename including file suffix - otherwise it is taken from type
     * @param type
     */
    public ShaderSource(String sourcename, ShaderType type) {
        int s = sourcename.indexOf(FILE_SUFFIX_SEPARATOR);
        if (s > -1) {
            suffix = sourcename.substring(s);
            this.sourceName = sourcename.substring(0, s);
        } else {
            suffix = type.suffix;
            this.sourceName = sourcename;
        }
        this.sourceNameVersion = "";
        this.type = type;
    }

    /**
     * Creates shader source with name of source, including source name version
     * 
     * @param sourceName Name of source - excluding source name version
     * @param sourceNameVersion
     * @param type
     */
    public ShaderSource(String sourceName, String sourceNameVersion, ShaderType type) {
        this.sourceName = sourceName;
        this.sourceNameVersion = sourceNameVersion;
        this.type = type;
        this.suffix = type.suffix;
    }

    public String getSourceNameVersion() {
        return sourceNameVersion;
    }

    public String getFullSourceName() {
        return sourceName + sourceNameVersion + suffix;
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
            // Remove version + newline char
            this.shaderSource = source.substring(version.length() + 1);
        } else {
            this.shaderSource = source;
        }
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
     * Appends source at the specified line, the default is to append after the precision qualifier.
     * 
     * @param line Search for line that starts with this
     * @param source Unversioned shader source
     */
    public void appendSource(String line, String source) {
        if (source != null && source.length() > 0) {
            int lineIndex = getLineIndexOf(line);
            if (lineIndex < 0) {
                throw new IllegalArgumentException("Shader source does not contain line: " + line);
            }
            int nextLineIndex = shaderSource.indexOf("\n", lineIndex) + 1;
            if (nextLineIndex >= 0) {
                this.shaderSource = this.shaderSource.substring(0, nextLineIndex) + source +
                        this.shaderSource.substring(nextLineIndex);
            } else {
                throw new IllegalArgumentException(""
                        + "Malformed? Could not find line delimiter after precision qualifier at " + lineIndex);
            }
        }
    }

    /**
     * Inserts define statements before the specified line.
     * 
     * @param line
     * @param defines
     */
    public void insertDefines(String line, String defines) {
        if (defines != null && defines.length() > 0) {
            int lineIndex = getLineIndexOf(line);
            if (lineIndex < 0) {
                throw new IllegalArgumentException("Shader source does not contain line: " + line);
            }
            this.shaderSource = this.shaderSource.substring(0, lineIndex) + defines
                    + this.shaderSource.substring(lineIndex);
        }
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
        StringTokenizer st = new StringTokenizer(source, "\n");
        try {
            String t = st.nextToken();
            if (t.trim().toLowerCase().startsWith(VERSION)) {
                return t;
            }
            return null;
        } catch (NoSuchElementException e) {
            // Most likely means that file is empty.
            throw new IllegalArgumentException(
                    "Could not find #version in file, is it empty?\n" + "Source:" + source + "\n");
        }
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

    /**
     * Searches for the specified line, beginning with excluding whitespaces, and returns the source string Index (NOT
     * linenumber) of the beginning of that line.
     * 
     * @param line The String to search for, ignoring whitespaces at beginning of line (indentation)
     * @return The source index of the line beginning with the specified String, or -1 if not found
     */
    public int getLineIndexOf(String line) {
        StringTokenizer st = new StringTokenizer(shaderSource, "\n");
        int lineIndex = 0;
        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            if (str.trim().startsWith(PRECISION)) {
                return lineIndex;
            } else {
                lineIndex += str.length() + 1;
            }
        }
        return -1;
    }

}
