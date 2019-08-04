package com.nucleus.opengl.shader;

import java.io.IOException;
import java.util.ArrayList;

import com.nucleus.Backend;
import com.nucleus.SimpleLogger;
import com.nucleus.common.StringUtils;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.shader.Shader.Categorizer;
import com.nucleus.shader.Shader.ShaderType;
import com.nucleus.shader.ShaderSource;

public class GLShaderSource extends ShaderSource {

    // uniform index from GL
    protected ArrayList<String>[] commonSources = new ArrayList[ShaderType.values().length];

    public GLShaderSource(String path, String sourcename, Categorizer function, String suffix, ShaderType type) {
        super(path, sourcename, suffix, type);
    }

    @Override
    public void loadShader(Backend backend, Categorizer function) throws IOException {
        Renderers v = GLESWrapper.getInfo().getRenderVersion();
        if (backend instanceof GLES20Wrapper) {
            ((GLES20Wrapper) backend).loadVersionedShaderSource(this);
            createCommonShaders(((GLES20Wrapper) backend), function, this.type);
        }

    }

    /**
     * Creates the common shaders that can be used to share functions between shaders.
     * 
     * @param gles
     * @param function
     * @param type
     * @throws IOException
     */
    private void createCommonShaders(GLES20Wrapper gles, Categorizer function, ShaderType type)
            throws IOException {
        String[] common = getLibSourceName(function, type);
        if (common != null) {
            ShaderSource[] commonSources = new ShaderSource[common.length];
            for (int i = 0; i < commonSources.length; i++) {
                commonSources[i] = createCommonSource(common[i], function, type);
            }
            loadShaderSources(gles, commonSources);
            SimpleLogger.d(getClass(), "Adding common sources : " + StringUtils.getString(common));
            createCommonSources(gles, commonSources, type);
        }
    }

    /**
     * Returns the common shader source for the specified type - call this when appending the common shader source
     * to a shader program.
     * 
     * @param type Shader type
     * @return
     */
    public String getCommonSources(ShaderType type) {
        if (commonSources[type.index] == null) {
            return null;
        }
        String result = new String();
        for (String source : commonSources[type.index]) {
            result += source;
        }
        return result;
    }

    /**
     * Internal method to create common ShaderSource for a given source name and type
     * 
     * @param sourceName
     * @param function
     * @param type
     * @return
     */
    protected ShaderSource createCommonSource(String sourceName, Categorizer function, ShaderType type) {
        switch (type) {
            case VERTEX:
                return new GLShaderSource(getPath(), sourceName, function, getSuffix(), type);
            case FRAGMENT:
                return new GLShaderSource(getPath(), sourceName, function, getSuffix(), type);
            default:
                throw new IllegalArgumentException("Not implemented for " + type);
        }
    }

    /**
     * Loads the version correct shader sources for the sourceNames and types.
     * The shader sourcenames will be versioned, when this method returns the shaders sourcecode can be fetched
     * from the sources objects.
     * 
     * @param gles
     * @param sources Name and type of shader sources to load, versioned source will be stored here
     * @throws IOException
     */
    protected void loadShaderSources(GLESWrapper gles, ShaderSource[] sources)
            throws IOException {
        // NOTE! - The shader source has not been loaded yet!
        int count = sources.length;
        for (int i = 0; i < count; i++) {
            gles.loadVersionedShaderSource(sources[i]);
        }
    }

    /**
     * Creates the common shaders that can be used to share functions between shaders, as a collection of source
     * strings.
     * Use this if platform does not have support for separate shader objects - append the source in commonSources
     * to shaders that needs it
     * 
     * @param gles
     * @param commonSource Array with vertex shader common sources
     * @param type
     * @throws IOException
     */
    public void createCommonSources(GLES20Wrapper gles, ShaderSource[] commonSource, ShaderType type)
            throws IOException {
        commonSources[type.index] = new ArrayList<>();
        for (ShaderSource source : commonSource) {
            commonSources[type.index].add(source.getSource());
        }
    }

    @Override
    public String[] getLibSourceName(Categorizer function, ShaderType type) {
        return function.getLibSourceName(type);
    }

}
