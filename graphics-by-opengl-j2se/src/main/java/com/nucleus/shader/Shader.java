package com.nucleus.shader;

import java.nio.FloatBuffer;
import java.util.HashMap;

import com.nucleus.common.FileUtils;
import com.nucleus.renderer.Pass;

/**
 * The methods needed for a programmable stage of the pipeline - this is for a
 * generic shader, compute/graphics etc. Pipeline implementations shall take
 * care of loading, compiling and linking of shaders
 *
 */
public interface Shader {

    /**
     * Used to fetch the sources for the shaders
     */
    public class Categorizer {
        protected Pass pass;
        protected Shading shading;
        protected String category;
        protected HashMap<ShaderType, String[]> libNames = new HashMap<>();
        protected VariableIndexer indexer;
        protected String[] sourceNames;

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
         * Returns the name of the category of this shader function, for instance
         * sprite, charmap
         * 
         * @return The category name of null if not relevant
         */
        public String getCategory() {
            return category;
        }

        /**
         * Returns the shader source name, excluding directory prefix and name of shader
         * (vertex/fragment/compute) Default behavior is to return getPath() /
         * getPassString() + getShadingString()
         * 
         * @param type The shader type to return source for
         * @return
         */
        public String getShaderSourceName(ShaderType type) {
            if (sourceNames == null) {
                return (getPath(type) + getPassString() + getShadingString());
            } else {
                return getPath(type) + sourceNames[type.index];
            }
        }

        /**
         * Adds the optional library names to use for shader types. This is used for
         * shading languages that does not support preprocessor include (or similar)
         * 
         * @param type The shader type to set libname(s) for
         * @param libnames One or more lib filenames to include with shader
         */
        public void addLibNames(ShaderType type, String[] libnames) {
            this.libNames.put(type, libnames);
        }

        /**
         * Optional names of additional library files that needs to be appended to
         * shader (type) source. This is for shading languages that does not support
         * precompiler include.
         * 
         * @param type
         * @return Optional strings to additional library sources that shall be
         * included, or null
         */
        public String[] getLibSourceName(ShaderType type) {
            return libNames.get(type);
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
         * The path is ends with directory separator.
         * 
         * @param type The shader type to return source for
         * @return The relative path, if defined it must end with the path separator
         * char
         */
        public String getPath(ShaderType type) {
            String path = getCategoryString();
            return path.length() == 0 ? path : path + FileUtils.DIRECTORY_SEPARATOR;
        }

        /**
         * Returns the pass as a lowercase string, or "" if null.
         * 
         * @return
         */
        public String getPassString() {
            return (pass != null ? pass.name().toLowerCase() : "");
        }

        /**
         * Returns the optional variable (location) indexer
         * 
         * @return Variable locations or null if not specified
         */
        public VariableIndexer getIndexer() {
            return indexer;
        }

        @Override
        public String toString() {
            return (getCategoryString() + FileUtils.DIRECTORY_SEPARATOR + getPassString() + getShadingString());
        }

    }

    /**
     * Categorizer for programs that share fragment shaders - source for fragment
     * shader is normally in assets folder (not using category folder)
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
     * Returns the key value for this shader program, this is the classname and
     * possible name of shader used. The key shall be a unique (in this context)
     * identifier for the shader - it does not need to be globally unique.
     * 
     * @return Key value for this shader program.
     */
    public String getKey();

    /**
     * If set then variable offsets, in the program ShaderVariables will be set from
     * this indexer. If null then offsets will set based on found variable sizes.
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

    /**
     * Returns the shader function type, this can be used to determine the shader
     * source (file) names.
     * 
     * @return
     */
    public Categorizer getFunction();

    /**
     * Returns the program type, this is what shaders are needed
     * 
     * @return
     */
    public ProgramType getType();

    /**
     * The different type of programs that can be linked from different type of
     * shaders.
     *
     */
    public enum ProgramType {
        VERTEX_FRAGMENT(),
        COMPUTE(),
        VERTEX_GEOMETRY_FRAGMENT();
    }

    /**
     * The different type of shaders
     *
     */
    public enum ShaderType {
        VERTEX(0),
        FRAGMENT(1),
        GEOMETRY(2),
        COMPUTE(3),
        TESSELATION(4);

        public final int index;

        private ShaderType(int index) {
            this.index = index;
        }

    }

}
