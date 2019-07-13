package com.nucleus.shader;

import java.io.File;
import java.nio.FloatBuffer;

import com.nucleus.BackendException;
import com.nucleus.opengl.shader.GLShaderProgram.ShaderType;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;

/**
 * The resources needed for a programmable stage of the pipeline
 *
 */
public interface Shader {

    /**
     * Used to fetch the sources for the shaders
     */
    public static class Categorizer {
        protected Pass pass;
        protected Shading shading;
        protected String category;

        public Categorizer(Pass pass, Shading shading, String category) {
            this.pass = pass;
            this.shading = shading;
            this.category = category;
        }

        /**
         * Returns the shading used, or null if not relevant
         * 
         * @return
         */
        public Shading getShading() {
            return shading;
        }

        /**
         * Returns the name of the category of this shader function, for instance sprite, charmap
         * 
         * @return The category name of null if not relevant
         */
        public String getCategory() {
            return category;
        }

        /**
         * Returns the shader source name, excluding directory prefix and name of shader (vertex/fragment/compute)
         * Default behavior is to return getPath() / getPassString() + getShadingString()
         * 
         * @param shaderType The shader type to return source for
         * @return
         */
        public String getShaderSourceName(ShaderType type) {
            return (getPath(type) + getPassString() + getShadingString());
        }

        /**
         * Returns the shading as a lowercase string, or "" if not set.
         * 
         * @return
         */
        public String getShadingString() {
            return (shading != null ? shading.name().toLowerCase() : "");
        }

        /**
         * Returns the category as a lowercase string, or "" if not set
         * 
         * @return
         */
        public String getCategoryString() {
            return (category != null ? category.toLowerCase() : "");
        }

        /**
         * Returns the relative path - by default this is the category
         * 
         * @param shaderType The shader type to return source for
         * @return The relative path, if defined it must end with the path separator char
         */
        public String getPath(ShaderType type) {
            String path = getCategoryString();
            return path.length() == 0 ? path : path + File.separator;
        }

        /**
         * Returns the pass as a lowercase string, or "" if null.
         * 
         * @return
         */
        public String getPassString() {
            return (pass != null ? pass.name().toLowerCase() : "");
        }

        @Override
        public String toString() {
            return (getCategoryString() + File.separatorChar + getPassString() + getShadingString());
        }

    }

    /**
     * Categorizer for programs that share fragment shaders - source for fragment shader is normally in
     * assets folder (not using category folder)
     *
     */
    public static class SharedfragmentCategorizer extends Categorizer {

        public SharedfragmentCategorizer(Pass pass, Shading shading, String category) {
            super(pass, shading, category);
        }

        @Override
        public String getShaderSourceName(ShaderType type) {
            switch (type) {
                case FRAGMENT:
                    // Fragment shaders are shared - skip category path
                    return getPassString() + getShadingString();
                default:
                    return super.getShaderSourceName(type);
            }
        }
    }

    /**
     * Different type of shadings that needs to be supported in shaders
     *
     */
    public enum Shading {
        flat(),
        textured(),
        pbr(),
        shadow1(),
        shadow2();
    }

    /**
     * Create the programs for the shader program implementation.
     * This method must be called before the program is used, or the other methods are called.
     * How the program is cread depends on API backend (GL/Vulkan)
     * 
     * @param renderer The render backend to use when compiling and linking program.
     * @throws BackendException If program could not be compiled and linked, possibly due to IOException
     */
    public void createProgram(NucleusRenderer renderer) throws BackendException;

    /**
     * Returns the key value for this shader program, this is the classname and possible name of shader used.
     * The key shall be a unique (in this context) identifier for the shader - it does not need to be globally unique.
     * 
     * @return Key value for this shader program.
     */
    public String getKey();

    /**
     * If set then variable offsets, in the program ShaderVariables will be set from this indexer.
     * If null then offsets will set based on found variable sizes.
     * 
     * @param variableIndexer
     */
    public void setIndexer(VariableIndexer variableIndexer);

    /**
     * Returns the uniform data, this shall be mapped to Backend before rendering
     * 
     * @return The buffer holding uniform data
     */
    public FloatBuffer getUniformData();

}
