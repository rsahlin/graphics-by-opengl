package com.nucleus.shader;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.Constants;
import com.nucleus.common.StringUtils;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES30Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLESWrapper.GLES31;
import com.nucleus.opengl.GLESWrapper.GLES32;
import com.nucleus.opengl.GLESWrapper.ProgramInfo;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.BufferObjectsFactory;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.Window;
import com.nucleus.shader.ShaderSource.ESSLVersion;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.ShadowPass1Program.Shadow1Categorizer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.TextureUtils;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.Matrix;

/**
 * This class handles loading, compiling and linking of OpenGL ES shader programs.
 * Implement for each program to make a mapping between shader variable names and GLES attribute/uniform locations.
 * A ShaderProgram object shall contain information that is specific to the shader program compilation and linking.
 * Uniform and attribute mapping shall be included but not the uniform and attribute data - this is so that the same
 * ShaderProgram instance can be used to render multiple objects.
 * Attribute offsets can be set after compile, if so then the attribute data will be tightly packed. Read the attribute
 * offsets from the ShaderVariable for each attribute buffer.
 * If a {@link VariableIndexer} is set before compile, the offsets are set to the ShaderVariables - this can be used to
 * share attribute buffers between shaders (that have different number of attributes)
 * 
 * @author Richard Sahlin
 *
 */
public abstract class ShaderProgram {

    public static final String PROGRAM_DIRECTORY = "assets/";
    /**
     * Shader suffix as added after checking for which version to use
     */
    public static final String SHADER_SOURCE_SUFFIX = ".essl";
    public static final String FRAGMENT_TYPE = "fragment";
    public static final String VERTEX_TYPE = "vertex";
    public static final String GEOMETRY_TYPE = "geometry";
    protected final static String MUST_SET_FIELDS = "Must set attributesPerVertex,vertexShaderName and fragmentShaderName";
    protected final static String NO_ACTIVE_UNIFORMS = "No active uniforms, forgot to call createProgram()?";

    protected ShaderVariable modelUniform;

    /**
     * The different type of programs that can be linked from different type of shaders.
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
        VERTEX(GLES20.GL_VERTEX_SHADER, 0),
        FRAGMENT(GLES20.GL_FRAGMENT_SHADER, 1),
        GEOMETRY(GLES32.GL_GEOMETRY_SHADER, 2),
        COMPUTE(GLES31.GL_COMPUTE_SHADER, 3);

        public final int value;
        public final int index;

        private ShaderType(int value, int index) {
            this.value = value;
            this.index = index;
        }

        /**
         * Returns the enum from the GL shader type, eg GL_VERTEX_SHADER
         * 
         * @param shader
         * @return
         */
        public static ShaderType getFromType(int shaderType) {
            for (ShaderType t : values()) {
                if (t.value == shaderType) {
                    return t;
                }
            }
            return null;
        }

    }

    /**
     * Read when shader source is created in {@link #createShaderSource(Renderers)}
     * Subclasses may modify before {@link #createProgram(GLES20Wrapper)} is called - or before they call
     * super.createProgram()
     */
    protected ProgramType shaders;
    protected ShaderProgram shadowPass1;
    protected ShaderProgram shadowPass2;

    /**
     * Number of vertices per sprite - this is for a quad that is created using element buffer.
     */
    public final static int VERTICES_PER_SPRITE = 4;
    /**
     * Draw using an index list each quad is made up of 6 indices (2 triangles)
     */
    public final static int INDICES_PER_SPRITE = 6;
    /**
     * Default number of components 1
     */
    public final static int DEFAULT_COMPONENTS = 3;

    public final static String SHADER_SOURCE_ERROR = "Error setting shader source: ";
    public final static String COMPILE_SHADER_ERROR = "Error compiling shader: ";
    public final static String COMPILE_STATUS_ERROR = "Failed compile status: ";
    public final static String CREATE_SHADER_ERROR = "Can not create shader object, context not active?";
    public final static String ATTACH_SOURCE_ERROR = "Error attaching shader source";
    public final static String LINK_PROGRAM_ERROR = "Error linking program: ";
    public final static String BIND_ATTRIBUTE_ERROR = "Error binding attribute: ";
    public final static String VARIABLE_LOCATION_ERROR = "Could not get shader variable location: ";
    public final static String NULL_VARIABLES_ERROR = " are null, program not created? Must call fetchProgramInfo()";
    public final static String GET_PROGRAM_INFO_ERROR = "Error fetching program info.";

    /**
     * Used to fetch the sources for the shaders
     */
    public static class Categorizer {
        protected Pass pass;
        protected Texture2D.Shading shading;
        protected String category;

        public Categorizer(Pass pass, Texture2D.Shading shading, String category) {
            this.pass = pass;
            this.shading = shading;
            this.category = category;
        }

        /**
         * Returns the shading used, or null if not relevant
         * 
         * @return
         */
        public Texture2D.Shading getShading() {
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
         * @param shaderType The shader type to return source for, GL_VERTEX_SHADER, GL_FRAGMENT_SHADER,
         * GL_COMPUTE_SHADER
         * @return
         */
        public String getShaderSourceName(int shaderType) {
            return (getPath(shaderType) + getPassString() + getShadingString());
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
         * @param shaderType The shader type to return source for, GL_VERTEX_SHADER, GL_FRAGMENT_SHADER,
         * GL_COMPUTE_SHADER
         * @return The relative path, if defined it must end with the path separator char
         */
        public String getPath(int shaderType) {
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
     * The basic function
     */
    protected Categorizer function;

    /**
     * The GL program object
     */
    private int program = Constants.NO_VALUE;
    /**
     * Available after {@link #fetchProgramInfo(GLES20Wrapper)} has been called
     */
    private ProgramInfo info;

    private ShaderSource[] shaderSources;
    private int[] shaderNames;

    /**
     * active attributes
     */
    protected ShaderVariable[] activeAttributes;
    /**
     * active uniforms
     */
    protected ShaderVariable[] activeUniforms;
    /**
     * Calculated in create program, created using {@link #attributeBufferCount}
     * If attributes are dynamically mapped (not using indexer) then only one buffer is used.
     */
    protected ShaderVariable[][] attributeVariables;

    protected BufferIndex defaultDynamicAttribBuffer = BufferIndex.ATTRIBUTES_STATIC;

    protected int attributeBufferCount = BufferIndex.values().length;

    protected GlobalLight globalLight = GlobalLight.getInstance();
    /**
     * The size of each buffer for the attribute variables - as set either from indexer if this is used or taken
     * from defined attributes.
     */
    protected int[] attributesPerVertex;
    /**
     * Optional additional storage per vertex, used when attribute buffer is created.
     */
    protected int[] paddingPerVertex;
    /**
     * If specified then variable offsets will be taken from this.
     */
    protected VariableIndexer variableIndexer;

    protected HashMap<Integer, ShaderVariable> blockVariables = new HashMap<>(); // Active block uniforms, index is the
                                                                                 // uniform index from GL
    protected ArrayList<String>[] commonSources = new ArrayList[ShaderType.values().length];

    /**
     * Uniform interface blocks
     */
    protected InterfaceBlock[] uniformInterfaceBlocks;
    protected BlockBuffer[] uniformBlockBuffers;
    /**
     * Samplers (texture units)
     */
    transient protected IntBuffer samplers;

    /**
     * Uniforms, used when rendering - uniforms array shall belong to program since uniforms are a property of the
     * program. This data is quite small and the size depends on what program is used - and not the mesh.
     * The same mesh may be rendered with different programs, for instance different shadow passes and will have
     * different number of uniforms depending on the program.
     * 
     */
    transient protected FloatBuffer uniforms;

    /**
     * Unmapped variable types
     */
    protected List<Integer> unMappedTypes = new ArrayList<>();

    /**
     * Returns the program for the specified pass and shading, this is used to resolve the correct
     * program for different passes
     * 
     * @param gles
     * @param pass
     * @param shading
     */
    public ShaderProgram getProgram(GLES20Wrapper gles, Pass pass, Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
            case SHADOW1:
                if (shadowPass1 == null) {
                    shadowPass1 = AssetManager.getInstance().getProgram(gles,
                            new ShadowPass1Program(this, new Shadow1Categorizer(Pass.SHADOW1, shading,
                                    function.getCategory()), shaders));
                }
                return shadowPass1;
            case SHADOW2:
                if (shadowPass2 == null) {
                    shadowPass2 = AssetManager.getInstance().getProgram(gles,
                            new ShadowPass2Program(this, pass, function.getCategory(), shading, shaders));
                }
                return shadowPass2;
            default:
                throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }

    /**
     * Returns the offset within an attribute buffer where the data is or -1 if shader variable is not defined.
     * 
     * @param name Name of the shader variable to query
     * @return Offset into attribute (buffer) where the storage for the specified variable is, or -1 if the variable is
     * not found.
     */
    public int getAttributeOffset(String name) {
        ShaderVariable v = getAttributeByName(name);
        if (v != null) {
            return v.getOffset();
        }
        return -1;
    }

    /**
     * Returns the name of the shader source - including relative source path BUT excluding vertex shader + extension.
     * for instance to load /asssets/line/flatvertex.essl this method shall return 'line/flat' for the shaderType
     * GL_VERTEX_SHADER
     * 
     * Default behavior is to return the function path + shader source name
     * 
     * @param shaderType The shader type to return source for, GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_COMPUTE_SHADER
     * @return
     */
    protected String getShaderSourceName(int shaderType) {
        return function.getShaderSourceName(shaderType);
    }

    protected ShaderProgram(Pass pass, Texture2D.Shading shading, String category, ProgramType shaders) {
        function = new Categorizer(pass, shading, category);
        this.shaders = shaders;
    }

    protected ShaderProgram(Categorizer function, ProgramType shaders) {
        this.function = function;
        this.shaders = shaders;
    }

    /**
     * If set then variable offsets, in the program ShaderVariables will be set from this indexer.
     * If null then offsets will set based on found variable sizes.
     * 
     * @param variableIndexer
     */
    public void setIndexer(VariableIndexer variableIndexer) {
        this.variableIndexer = variableIndexer;
    }

    /**
     * Returns the indexer used when creating this program, or null if not set.
     * 
     * @return
     */
    public VariableIndexer getIndexer() {
        return variableIndexer;
    }

    /**
     * Creates an indexer from the {@link #activeAttributes} - only use this method of the indexer is not set.
     * 
     * @return
     * @throws IllegalArgumentException If an indexer has been set by calling {@link #setIndexer(VariableIndexer)}
     */
    public VariableIndexer createIndexer() {
        if (variableIndexer != null) {
            throw new IllegalArgumentException("Indexer has been set.");
        }
        int size = activeAttributes.length;
        String[] names = new String[size];
        int[] offsets = new int[size];
        VariableType[] types = new VariableType[size];
        BufferIndex[] bufferIndexes = new BufferIndex[size];
        BufferIndex bufferIndex = BufferIndex.ATTRIBUTES;
        for (int i = 0; i < size; i++) {
            names[i] = activeAttributes[i].getName();
            offsets[i] = activeAttributes[i].getOffset();
            types[i] = activeAttributes[i].getType();
            // TODO - currently only supports using one buffer
            bufferIndexes[i] = bufferIndex;
        }
        return new VariableIndexer(names, offsets, types, bufferIndexes,
                new int[] { getAttributesPerVertex(bufferIndex) });
    }

    /**
     * Loads the version correct shader sources for the sourceNames and types.
     * The shader sourcenames will be versioned, when this method returns the shaders sourcecode can be fetched
     * from the sources objects.
     * 
     * @param sources Name and type of shader sources to load, versioned source will be stored here
     * @throws IOException
     */
    protected void loadShaderSources(GLESWrapper gles, ShaderSource[] sources)
            throws IOException {

        // NOTE! - The shader source has not been loaded yet!
        int count = sources.length;
        Renderers v = GLESWrapper.getInfo().getRenderVersion();
        for (int i = 0; i < count; i++) {
            gles.loadVersionedShaderSource(sources[i]);
        }
    }

    /**
     * Sets the name of the shaders in this program and returns an array of {@link ShaderSource}, normally 2 - one for
     * vertex and one for fragment-shader.
     * This method must be called before the program is created.
     * 
     * @param version Highest level of GL that is supported
     */
    protected ShaderSource[] createShaderSource(Renderers version) {
        ShaderSource[] sources = null;
        ShaderType[] shaderTypes;
        switch (shaders) {
            case VERTEX_FRAGMENT:
                sources = new ShaderSource[2];
                shaderTypes = new ShaderType[] { ShaderType.VERTEX, ShaderType.FRAGMENT };
                break;
            case COMPUTE:
                sources = new ShaderSource[1];
                shaderTypes = new ShaderType[] { ShaderType.COMPUTE };
                break;
            case VERTEX_GEOMETRY_FRAGMENT:
                sources = new ShaderSource[3];
                shaderTypes = new ShaderType[] { ShaderType.VERTEX, ShaderType.GEOMETRY,
                        ShaderType.FRAGMENT };
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + shaders);
        }
        for (int i = 0; i < shaderTypes.length; i++) {
            sources[i] = getShaderSource(version, shaderTypes[i]);
        }
        return sources;
    }

    /**
     * Returns the name of the shader source for the specified type, this is taken from the function using pass, shading
     * and category.
     * Override in subclasses to point to other shader source
     * 
     * @param version Highest GL version that is supported, used to fetch versioned source name.
     * @param type The shader type to return source for
     * @return
     */
    protected ShaderSource getShaderSource(Renderers version, ShaderType type) {

        switch (type.value) {
            case GLES20.GL_VERTEX_SHADER:
                return new ShaderSource(
                        PROGRAM_DIRECTORY + getShaderSourceName(type.value)
                                + VERTEX_TYPE,
                        getSourceNameVersion(version, type.value), type);
            case GLES20.GL_FRAGMENT_SHADER:
                return new ShaderSource(
                        PROGRAM_DIRECTORY + getShaderSourceName(type.value)
                                + FRAGMENT_TYPE,
                        getSourceNameVersion(version, type.value), type);
            case GLES31.GL_COMPUTE_SHADER:
                return new ShaderSource(
                        PROGRAM_DIRECTORY + getShaderSourceName(type.value),
                        getSourceNameVersion(version, type.value), type);
            case GLES32.GL_GEOMETRY_SHADER:
                return new ShaderSource(
                        PROGRAM_DIRECTORY + getShaderSourceName(type.value)
                                + GEOMETRY_TYPE,
                        getSourceNameVersion(version, type.value), type);
            default:
                throw new IllegalArgumentException("Not implemented for type: " + type);

        }
    }

    /**
     * Called by {@link #getShaderSource(Renderers, int)} to append shader (ESSL) version to sourcename.
     * This is used to be able to load different shader sources depending on if GLES major version is less than 3.
     * Override this if different source shall be used depending on available renderer/shader version.
     * Default is to append _v300 if GLES version is 3 or above.
     * 
     * @param version Highest GL version that is supported
     * @param type Shader type, GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_COMPUTE_SHADER
     * @return Empty string "", or shader version to append to source name if different shader source shall be used for
     * a specific shader version.
     */
    protected String getSourceNameVersion(Renderers version, int type) {
        if (version.major >= 3) {
            return ShaderSource.V300;
        }
        return "";
    }

    /**
     * Create the programs for the shader program implementation.
     * This method must be called before the program is used, or the other methods are called.
     * It will create the {@link #shaderSources} field, compile and link the shader sources specified.
     * 
     * @param gles The GLES20 wrapper to use when compiling and linking program.
     * @throws GLException If program could not be compiled and linked, possibly due to IOException
     */
    public void createProgram(GLES20Wrapper gles) throws GLException {
        shaderSources = createShaderSource(GLES20Wrapper.getInfo().getRenderVersion());
        if (shaderSources == null) {
            throw new ShaderProgramException(MUST_SET_FIELDS);
        }
        createProgram(gles, shaderSources);
    }

    /**
     * Sorts the attributes used based on BufferIndex - attribute variables are sorted based on buffer in the specified
     * result array.
     * Finds the shader attribute variables per buffer using VariableMapping, iterate through defined (by subclasses)
     * attribute variable mapping.
     * Put the result in the result array and set the {@linkplain ShaderVariable} offset based on used attributes.
     * 
     * TODO Add check for mismatch of size, ie if ShaderVariables has one variable as float3 and it is defined is
     * program as float4 then raise error.
     * 
     * @param resultArray Array to store shader variables for each attribute buffer in, attributes for buffer 1 will go
     * at index 0.
     */
    private void sortAttributeVariablePerBuffer(ShaderVariable[][] resultArray) {
        if (variableIndexer == null) {
            // If indexer not specified then use one buffer.
            resultArray[defaultDynamicAttribBuffer.index] = new ShaderVariable[info
                    .getActiveVariables(VariableType.ATTRIBUTE)];
            if (resultArray[defaultDynamicAttribBuffer.index].length != activeAttributes.length) {
                throw new IllegalArgumentException("Active variable array size mismatch - active count from info "
                        + info.getActiveVariables(VariableType.ATTRIBUTE) + ", array size "
                        + activeAttributes.length);
            }
            resultArray[BufferIndex.ATTRIBUTES.index] = activeAttributes;
        } else {
            for (int index = 0; index < resultArray.length; index++) {
                resultArray[index] = variableIndexer.sortByBuffer(activeAttributes, index);
            }
        }
    }

    private void dynamicMapShaderOffset(ShaderVariable[] variables, VariableType type) {
        int offset = 0;
        int samplerOffset = 0;
        for (ShaderVariable v : variables) {
            if (v != null && v.getType() == type) {
                switch (v.getDataType()) {
                    case GLES20.GL_SAMPLER_2D:
                    case GLES30.GL_SAMPLER_2D_SHADOW:
                        v.setOffset(samplerOffset);
                        samplerOffset += v.getSizeInFloats();
                        break;
                    default:
                        v.setOffset(offset);
                        offset += v.getSizeInFloats();
                        break;
                }
            }
        }
    }

    /**
     * Returns the number of attributes per vertex that are used by the program.
     * 
     * @param buffer The buffer to get attributes per vertex for
     * @return Number of attributes as used by this program, per vertex 0 or -1 if not defined.
     */
    protected int getAttributesPerVertex(BufferIndex buffer) {
        if (attributesPerVertex.length > buffer.index) {
            return attributesPerVertex[buffer.index];
        } else {
            // No attribute buffer for this program.
            return -1;
        }
    }

    /**
     * Returns an array with number of attributes used per vertex, for each attribute buffer that is used by this
     * program.
     * This is the minimal storage that this program needs per vertex.
     * 
     * @return
     */
    public int[] getAttributeSizes() {
        int[] attributeSize = new int[attributeBufferCount];
        for (BufferIndex index : BufferIndex.values()) {
            if (index.index >= attributeSize.length) {
                break;
            }
            attributeSize[index.index] = getAttributesPerVertex(index);
        }
        return attributeSize;
    }

    /**
     * Internal method, creates the array storage for for uniform samplers, sampler usage is specific to program
     * and does not need to be stored in mesh.
     * 
     */
    protected void createSamplerStorage() {
        if (activeUniforms == null) {
            throw new IllegalArgumentException(NO_ACTIVE_UNIFORMS);
        }
        int samplerSize = 0;
        samplerSize = getSamplerSize(activeUniforms);
        if (samplerSize > 0) {
            createSamplers(samplerSize);
        } else {
            SimpleLogger.d(getClass(), "No samplers used");
        }
    }

    /**
     * Creates array store for uniform data that fits this shader program.
     * - try to use uniform blocks as much as possible instead of separate uniforms
     * 
     * @return
     */
    public FloatBuffer createUniformArray() {
        if (activeUniforms == null) {
            throw new IllegalArgumentException(NO_ACTIVE_UNIFORMS);
        }
        int uniformSize = getVariableSize(activeUniforms, VariableType.UNIFORM);
        return BufferUtils.createFloatBuffer(uniformSize);
    }

    /**
     * Creates the block (uniform) buffers needed for this program, if any are used.
     * Always creates a {@link FloatBlockBuffer} for uniforms.
     * If BlockBuffer is needed for other type than uniforms it needs to be implemented.
     * Binds uniform block to a binding point using the block index, this means that there is one binding point
     * per uniform block.
     * 
     * @param gles
     * @return Uniform variable block buffers, using buffer objects, for this program, or null if not used.
     */
    public BlockBuffer[] createUniformBlockBuffers(GLES30Wrapper gles) throws GLException {
        if (uniformInterfaceBlocks == null) {
            return null;
        }
        for (InterfaceBlock block : uniformInterfaceBlocks) {
            // Here the binding point and block index is the same.
            gles.glUniformBlockBinding(program, block.blockIndex, block.blockIndex);
        }
        BlockBuffer[] buffers = BlockBuffer.createBlockBuffers(uniformInterfaceBlocks);
        BufferObjectsFactory.getInstance().createUBOs(gles, buffers);
        return buffers;
    }

    /**
     * Set the attribute pointer(s) using the data in the vertexbuffer, this shall make the necessary calls to
     * set the pointers for used attributes, enable pointers as needed.
     * This will make the actual connection between the attribute data in the vertex buffer and the shader.
     * It is up to the caller to make sure that the attribute array(s) in the mesh contains valid data.
     * 
     * @param gles
     * @param mesh
     */
    public void updateAttributes(GLES20Wrapper gles, AttributeUpdater mesh) throws GLException {
        for (int i = 0; i < attributeVariables.length; i++) {
            AttributeBuffer buffer = mesh.getAttributeBuffer(i);
            if (buffer != null) {
                gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attributeVariables[i]);
                GLUtils.handleError(gles, "glVertexAttribPointers ");
            }
        }
    }

    /**
     * Sets the float values from data at the offset from variable, use this to set more than one value.
     * 
     * @param variable The shader variable to set uniform data to
     * @param data The uniform data to set
     * @param sourceOffset Offset into data where values are read
     */
    public void setUniformData(ShaderVariable variable, float[] data, int sourceOffset) {
        uniforms.position(variable.getOffset());
        uniforms.put(data, sourceOffset, variable.getSizeInFloats());
    }

    /**
     * Updates the data uploaded to GL as uniforms, if uniforms are static then only the matrices needs to be updated.
     * Calls {@link #setUniformMatrices(float[][])} to update uniform matrices.
     * Then call {@link #updateUniformData(float[])} to set program specific uniform data
     * Then sets uniforms to GL by calling {@link #uploadUniforms(GLES20Wrapper, float[], ShaderVariable[])
     * When this method returns the uniform data has been uploaded to GL and is ready.
     * 
     * @param gles
     * @param matrices modelview, projection and renderpass matrices
     */
    public void updateUniforms(GLES20Wrapper gles, float[][] matrices)
            throws GLException {
        setUniformMatrices(matrices);
        updateUniformData(uniforms);
        uploadUniforms(gles, uniforms, activeUniforms);
    }

    /**
     * Uploads uniforms to GL, float array data is uploaded and if blockbuffer is used it is bound.
     * If uniform block buffer is dirty and uses UBO the data is first uploaded to buffer object then uniform block is
     * bound.
     * 
     * 
     * @param gles
     * @param uniformData
     * @param activeUniforms
     * @throws GLException
     */
    protected void uploadUniforms(GLES20Wrapper gles, FloatBuffer uniformData, ShaderVariable[] activeUniforms)
            throws GLException {

        for (ShaderVariable v : activeUniforms) {
            // If null then declared in program but not used, silently ignore
            if (v != null) {
                if (v.getBlockIndex() != Constants.NO_VALUE) {
                    setUniformBlock((GLES30Wrapper) gles, uniformBlockBuffers[v.getBlockIndex()], v);
                } else {
                    uploadUniform(gles, uniformData, v);
                }
            }
        }
    }

    /**
     * Prepares a texture used before rendering starts.
     * This shall set texture parameters to used textures, ie activate texture, bind texture then set parameters.
     * 
     * @param gles
     * @param texture
     * @throws GLException
     */
    public void prepareTexture(GLES20Wrapper gles, Texture2D texture) throws GLException {
        if (texture == null || texture.getTextureType() == TextureType.Untextured) {
            return;
        }
        /**
         * TODO - make texture names into enums
         */
        int unit = samplers.get(getUniformByName("uTexture").getOffset());
        TextureUtils.prepareTexture(gles, texture, unit);
    }

    /**
     * Fetches the program info and stores in this class.
     * This will read active attribute and uniform names, call this after the program has been compiled and linked.
     * 
     * @param gles
     * @throws GLException If attribute or uniform locations could not be found.
     */
    protected void fetchProgramInfo(GLES20Wrapper gles) throws GLException {
        info = gles.getProgramInfo(program);
        GLUtils.handleError(gles, GET_PROGRAM_INFO_ERROR);
        activeAttributes = new ShaderVariable[info.getActiveVariables(VariableType.ATTRIBUTE)];
        activeUniforms = new ShaderVariable[info.getActiveVariables(VariableType.UNIFORM)];
        int uniformBlockCount = info.getActiveVariables(VariableType.UNIFORM_BLOCK);
        if (uniformBlockCount > 0) {
            uniformInterfaceBlocks = gles.getUniformBlocks(info);
            for (InterfaceBlock block : uniformInterfaceBlocks) {
                fetchActiveVariables(gles, VariableType.UNIFORM_BLOCK, info, block);
            }
        }
        fetchActiveVariables(gles, VariableType.ATTRIBUTE, info, null);
        fetchActiveVariables(gles, VariableType.UNIFORM, info, null);
        attributeVariables = new ShaderVariable[attributeBufferCount][];
        attributesPerVertex = new int[attributeBufferCount];
        paddingPerVertex = new int[attributeBufferCount];
        sortAttributeVariablePerBuffer(attributeVariables);
    }

    /**
     * Maps shader variables to defined offsets or updates the shader variable offsets to runtime values
     * 
     * @param gles
     * @indexer Offsets to use for shader variables, if specified active variables will be mapped to offsets
     * as found when comparing variable name.
     * If null then offsets will be updated according to used variables in an increasing manner and will be tightly
     * packed.
     * @throws GLException
     */
    protected void mapAttributeOffsets(GLES20Wrapper gles, VariableIndexer indexer) throws GLException {
        if (indexer == null) {
            dynamicMapOffsets();
        } else {
            setVariableOffsets(gles, activeAttributes, indexer);
            // Map the used uniforms from all used shader variables.
            dynamicMapShaderOffset(activeUniforms, VariableType.UNIFORM);
        }
    }

    /**
     * Use the offset as specified in the indexer and update or set the offset in program variables.
     * Use this when the offset mapping of variables shall be controlled, for instance by a shared program.
     * 
     * @param gles
     * @param indexer
     */
    protected void setVariableOffsets(GLES20Wrapper gles, ShaderVariable[] variables, VariableIndexer indexer) {
        for (ShaderVariable v : variables) {
            int index = indexer.getIndexByName(v.getName());
            // For now we cannot recover if variable not defined in indexer
            if (index == -1) {
                throw new IllegalArgumentException("Indexer must define offset for shader variable " + v.getName());
            }
            v.setOffset(indexer.getOffset(index));
        }
    }

    /**
     * Dynamically sets used shader variable offsets, for ATTRIBUTES and UNIFORMS
     * The offset will be tightly packed based on used variable size, the order of used variables will be the same.
     */
    private void dynamicMapOffsets() {
        for (ShaderVariable[] sv : attributeVariables) {
            // In case only attribute buffer is used the first index will be null.
            if (sv != null) {
                dynamicMapShaderOffset(sv, VariableType.ATTRIBUTE);
            }
        }
        // Map the used uniforms from all used shader variables.
        dynamicMapShaderOffset(activeUniforms, VariableType.UNIFORM);
    }

    /**
     * Fetches active variables info of the specified type (attribute or uniform) and calls implementing shader program
     * class to store the shader variable.
     * This will create the mapping between the specific shader program and the location of shader variables
     * so that each shader variable can be indexed using the implementing shader program.
     * 
     * @param gles
     * @param type Type of variable to fetch info for.
     * @param info
     * @param block Variable block info or null - store block variables here
     * @throws GLException If attribute or uniform location(s) are -1, ie they could not be found using the name.
     */
    private void fetchActiveVariables(GLES20Wrapper gles, VariableType type, ProgramInfo info, InterfaceBlock block)
            throws GLException {
        // If interface block the count is number of indices in block
        int count = block == null ? info.getActiveVariables(type) : block.indices.length;
        if (count == 0) {
            return;
        }
        // If type is uniform block then query max length of uniform name
        VariableType infoType = type != VariableType.UNIFORM_BLOCK ? type : VariableType.UNIFORM;
        byte[] nameBuffer = new byte[info.getMaxNameLength(infoType)];
        ShaderVariable variable = null;
        for (int i = 0; i < count; i++) {
            variable = null;
            if (block != null) {
                variable = gles.getActiveVariable(program, type, block.indices[i], nameBuffer);
                // Add to current block uniforms
                this.blockVariables.put(variable.getActiveIndex(), variable);
                addShaderVariable(variable);
            } else {
                // Check if uniform variable already has been fetched from block - then check that it is used in type of
                // shader.
                variable = getBlockVariable(type, i);
                if (variable != null) {
                    SimpleLogger.d(getClass(),
                            type.name() + " using block variable for index " + i + ", " + variable.getName());
                } else {
                    variable = gles.getActiveVariable(program, type, i, nameBuffer);
                    setVariableLocation(gles, program, variable);
                    addShaderVariable(variable);
                }
            }
        }
    }

    /**
     * Returns ShaderVariable from variable blocks, if the block variable is used in the type.
     * 
     * @param type Shader type
     * @param index
     * @return
     */
    protected ShaderVariable getBlockVariable(VariableType type, int index) {
        ShaderVariable var = blockVariables.get(index);
        if (var != null) {
            InterfaceBlock block = uniformInterfaceBlocks[var.getBlockIndex()];
            switch (block.usage) {
                case VERTEX_SHADER:
                    return type == VariableType.UNIFORM ? var : null;
                case FRAGMENT_SHADER:
                    return type == VariableType.ATTRIBUTE ? var : null;
                case VERTEX_FRAGMENT_SHADER:
                    return var;
            }
        }
        return null;
    }

    /**
     * Fetch the GLES shader variable location (name) and set in the ShaderVariable
     * This is the name (int) value to use to access the variable in GL
     * 
     * @param gles
     * @param program
     * @param variable
     * @throws GLException
     */
    protected void setVariableLocation(GLES20Wrapper gles, int program, ShaderVariable variable)
            throws GLException {
        switch (variable.getType()) {
            case ATTRIBUTE:
                variable.setLocation(gles.glGetAttribLocation(program, variable.getName()));
                break;
            case UNIFORM:
                variable.setLocation(gles.glGetUniformLocation(program, variable.getName()));
                break;
            case UNIFORM_BLOCK:
                // Location is already set - do nothing.
                break;
        }
        if (variable.getLocation() < 0) {
            throw new GLException(VARIABLE_LOCATION_ERROR + variable.getName(), 0);
        }
    }

    /**
     * Returns the active shader uniform by name, or null if not found
     * 
     * @param uniform Name of uniform to return
     * @return
     */
    public ShaderVariable getUniformByName(String uniform) {
        return getVariableByName(uniform, activeUniforms);
    }

    /**
     * Returns the active shader attribute by name, or null if not found
     * 
     * @param attrib Name of attribute to return
     * @return
     */
    public ShaderVariable getAttributeByName(String attrib) {
        return getVariableByName(attrib, activeAttributes);
    }

    protected ShaderVariable getVariableByName(String name, ShaderVariable[] variables) {
        for (ShaderVariable v : variables) {
            if (v != null && v.getName().contentEquals(name)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Links the specified vertex and fragment shaders to the specified program.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param program
     * @param shaderNames
     * @throws GLException If the program could not be linked with the shaders.
     */
    public void linkProgram(GLES20Wrapper gles, int program, int[] shaderNames)
            throws GLException {
        for (int name : shaderNames) {
            gles.glAttachShader(program, name);
        }
        GLUtils.handleError(gles, ATTACH_SOURCE_ERROR);
        gles.glLinkProgram(program);
        SimpleLogger.d(getClass(), gles.glGetProgramInfoLog(program));
        GLUtils.handleError(gles, LINK_PROGRAM_ERROR);
    }

    private void logShaderSources(GLES20Wrapper gles, int[] shaderNames) {
        SimpleLogger.d(getClass(), "Common vertex shaders:");
        // It could be an exception before shader names are allocated
        if (shaderNames != null) {
            int index = 1;
            for (int name : shaderNames) {
                SimpleLogger.d(getClass(), "Shader source for shader " + index++ + " : " + toString());
                SimpleLogger.d(getClass(), gles.glGetShaderSource(name));

            }
        }
    }

    /**
     * Creates the shader name, attaches the source and compiles the shader.
     * 
     * @param gles
     * @param source
     * @param library true if this is not the main shader
     * @return The created shader
     * @throws GLException If there is an error setting or compiling shader source.
     */
    public int compileShader(GLES20Wrapper gles, ShaderSource source) throws GLException {
        int shader = gles.glCreateShader(source.type.value);
        if (shader == 0) {
            throw new GLException(CREATE_SHADER_ERROR, GLES20.GL_NO_ERROR);
        }
        source.appendSource(getCommonSources(source.type));
        compileShader(gles, source, shader);
        return shader;
    }

    /**
     * Compiles the shader from the specified inputstream, the inputstream is not closed after reading.
     * It is up to the caller to close the stream.
     * The GL version will be appended to the source, calling {@link GLESWrapper#getShaderVersion()}
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param source The shader source
     * @param shader OpenGL object to compile the shader to.
     * @throws GLException If there is an error setting or compiling shader source.
     */
    public void compileShader(GLES20Wrapper gles, ShaderSource source, int shader) throws GLException {
        try {
            if (source.getSource() == null) {
                throw new IllegalArgumentException("Shader source is null for " + source.getFullSourceName());
            }
            gles.glShaderSource(shader, source.getVersionedShaderSource());
            GLUtils.handleError(gles, SHADER_SOURCE_ERROR + source.getFullSourceName());
            gles.glCompileShader(shader);
            GLUtils.handleError(gles, COMPILE_SHADER_ERROR + source.getFullSourceName());
            checkCompileStatus(gles, source, shader);
        } catch (GLException e) {
            SimpleLogger.d(getClass(), e.getMessage() + " from source:" + System.lineSeparator());
            StringTokenizer st = new StringTokenizer(source.getVersionedShaderSource(), System.lineSeparator());
            StringBuffer sb = new StringBuffer();
            int index = 1;
            while (st.hasMoreTokens()) {
                sb.append(index++ + " " + st.nextToken() + "\n\r");
            }
            SimpleLogger.d(getClass(), sb.toString());
            throw e;
        }
    }

    /**
     * Checks the compile status of the specified shader program - if shader is not successfully compiled an exception
     * is thrown.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param source
     * @param shader
     * @throws GLException
     */
    public void checkCompileStatus(GLES20Wrapper gles, ShaderSource source, int shader) throws GLException {
        IntBuffer compileStatus = BufferUtils.createIntBuffer(1);
        gles.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus);
        if (compileStatus.get(0) != GLES20.GL_TRUE) {
            throw new GLException(
                    COMPILE_STATUS_ERROR + source.getFullSourceName() + " : " + compileStatus.get(0) + "\n"
                            + gles.glGetShaderInfoLog(shader),
                    GLES20.GL_FALSE);
        }
    }

    /**
     * Checks the link status of the specified program - if link status returns false then an exception is thrown.
     * 
     * @param gles
     * @param program The program to check link status on
     * @throws GLException
     */
    public void checkLinkStatus(GLES20Wrapper gles, int program) throws GLException {
        int[] linkStatus = new int[1];
        gles.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            throw new GLException(LINK_PROGRAM_ERROR + Integer.toString(program));
        }
    }

    /**
     * Validates the program - only call this in debug mode.
     * Set uniform and texture data before calling this method.
     * 
     * @param gles
     */
    public void validateProgram(GLES20Wrapper gles) {
        gles.glValidateProgram(program);
        String result = gles.glGetProgramInfoLog(program);
        int[] status = new int[1];
        gles.glGetProgramiv(program, GLES20.GL_VALIDATE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            SimpleLogger.d(getClass(), "Could not validate program\n");
            SimpleLogger.d(getClass(), result);
            throw new IllegalArgumentException("Could not validate program:\n" + result);
        } else {
            SimpleLogger.d(getClass(), "Program " + program + " validated OK.");
        }
    }

    /**
     * Returns the program object for this shader program.
     * 
     * @return The program object.
     */
    public int getProgram() {
        return program;
    }

    /**
     * Returns the uniform data, this shall be mapped to GL by the program.
     * 
     * @return
     */
    public FloatBuffer getUniformData() {
        return uniforms;
    }

    /**
     * Stores the shader variable in this program, if variable is of unmapped type, for instance Sampler, then it is
     * skipped. Also skip variables that are defined in code but not used in shader.
     * Variables are stored in {@link #activeUniforms} or {@link #activeAttributes}
     * 
     * @param variable
     * @throws IllegalArgumentException If shader variables are null, the program has probably not been created,
     * or if a variable has no mapping in the code.
     */
    protected void addShaderVariable(ShaderVariable variable) {
        // If variable type is is unMappedTypes then skip, for instance texture
        if (unMappedTypes.contains(variable.getDataType())) {
            return;
        }
        setShaderVariable(variable);
    }

    /**
     * Sets the active shader variable into {@link ShaderVariable} array - call this when variable has been validated.
     * 
     * @param variable
     */
    protected void setShaderVariable(ShaderVariable variable) {
        switch (variable.getType()) {
            case ATTRIBUTE:
                activeAttributes[variable.getActiveIndex()] = variable;
                break;
            case UNIFORM:
                if (activeUniforms[variable.getActiveIndex()] != null) {
                    throw new IllegalArgumentException("Not null");
                }
                activeUniforms[variable.getActiveIndex()] = variable;
                break;
            case UNIFORM_BLOCK:
                if (activeUniforms[variable.getActiveIndex()] != null) {
                    throw new IllegalArgumentException("Not null");
                }
                activeUniforms[variable.getActiveIndex()] = variable;
                break;
            default:
                // DO NOTHING
        }
    }

    /**
     * Utility method to create the vertex and shader program using the specified shader names.
     * The shaders will be loaded, compiled and linked.
     * Vertex shader, fragment shader and program objects will be created.
     * If program compiles successfully then the program info is fetched.
     * 
     * @param gles
     * @param sources Name of shaders to load, compile and link
     * @throws GLException If program could not be compiled and linked
     */
    protected void createProgram(GLES20Wrapper gles, ShaderSource[] sources) throws GLException {
        SimpleLogger.d(getClass(),
                "Creating program for: " + sources.length + " shaders in program " + getClass().getSimpleName()
                        + ", sources are:");
        for (ShaderSource ss : sources) {
            SimpleLogger.d(getClass(), ss.getFullSourceName());
        }
        try {
            loadShaderSources(gles, sources);
            createCommonShaders(gles, sources);
            ESSLVersion minVersion = ShaderSource.getMinVersion(sources);
            shaderNames = new int[sources.length];
            program = gles.glCreateProgram();
            for (int shaderIndex = 0; shaderIndex < sources.length; shaderIndex++) {
                // Insert the correct version depending on platform implementation.
                sources[shaderIndex].setShaderVersion(gles.replaceShaderVersion(minVersion));

                SimpleLogger.d(getClass(),
                        "Compiling " + sources[shaderIndex].getFullSourceName());
                shaderNames[shaderIndex] = compileShader(gles, sources[shaderIndex]);

            }
            linkProgram(gles, program, shaderNames);
            checkLinkStatus(gles, program);
            fetchProgramInfo(gles);
            mapAttributeOffsets(gles, variableIndexer);
            setAttributesPerVertex();
            uniforms = createUniformArray();
            if (GLES20Wrapper.getInfo().getRenderVersion().major >= 3) {
                uniformBlockBuffers = createUniformBlockBuffers((GLES30Wrapper) gles);
            }
            createSamplerStorage();
            setSamplers();
            initUniformData(uniforms);
        } catch (GLException e) {
            logShaderSources(gles, shaderNames);
            throw e;
        } catch (IOException e) {
            throw new GLException(e.toString(), -1);
        }
    }

    /**
     * Fetch the storage requirements for each vertex, per buffer
     */
    protected void setAttributesPerVertex() {
        if (variableIndexer == null) {
            // Fetch packed size for uniform/attribute data
            for (int i = 0; i < attributeBufferCount; i++) {
                // If only attribute buffer is used the first array index will be null
                if (attributeVariables[i] != null) {
                    attributesPerVertex[i] = getVariableSize(attributeVariables[i], VariableType.ATTRIBUTE);
                }
            }
        } else {
            for (int i = 0; i < variableIndexer.sizePerVertex.length; i++) {
                attributesPerVertex[i] = variableIndexer.getSizePerVertex(i);
            }
        }

    }

    /**
     * Sets the uniform data into the block - if BlockBuffer is dirty the UBO is updated.
     * 
     * @param gles
     * @param block
     * @param variable
     * @param offset
     * @throws GLException
     */
    protected void setUniformBlock(GLES30Wrapper gles, BlockBuffer blockBuffer, ShaderVariable variable)
            throws GLException {
        if (blockBuffer.isDirty()) {
            gles.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, variable.getBlockIndex(), blockBuffer.getBufferName());
            /**
             * TODO - Another solution is to use glBufferSubData - but the benefit may not be obvious since reusing the
             * same buffer with new content may trigger wait for buffer to be finished in rendering.
             * Theoretically GL should handle allocation of buffers in an optimized manner, effectively reusing a
             * discarded buffer.
             */
            gles.glBufferData(GLES30.GL_UNIFORM_BUFFER, blockBuffer.getSizeInBytes(),
                    blockBuffer.getBuffer().position(0),
                    GLES30.GL_STATIC_DRAW);
            blockBuffer.setDirty(false);
            GLUtils.handleError(gles, "setUniformBlock " + blockBuffer.getBlockName());

        } else {
            InterfaceBlock vars = blockBuffer.interfaceBlock;
            gles.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, vars.blockIndex,
                    blockBuffer.getBufferName());
            GLUtils.handleError(gles, "setUniformBlock " + blockBuffer.getBlockName());
        }
    }

    /**
     * Uploads one of more float uniforms for the specified variable to GL, supports VEC2, VEC3, VEC4 and MAT2, MAT3,
     * MAT4 types
     * 
     * @param gles
     * @param uniforms The uniform data
     * @param variable Shader variable to set uniform data for, datatype and size is read.
     * @param offset Offset into uniform array where data starts.
     * @throws GLException If there is an error setting a uniform to GL
     */
    public final void uploadUniform(GLES20Wrapper gles, FloatBuffer uniforms, ShaderVariable variable)
            throws GLException {
        int offset = variable.getOffset();
        uniforms.position(offset);
        GLUtils.handleError(gles, "Clear error");
        switch (variable.getDataType()) {
            case GLES20.GL_FLOAT:
                gles.glUniform1fv(variable.getLocation(), variable.getSize(), uniforms);
                break;
            case GLES20.GL_FLOAT_VEC2:
                gles.glUniform2fv(variable.getLocation(), variable.getSize(), uniforms);
                break;
            case GLES20.GL_FLOAT_VEC3:
                gles.glUniform3fv(variable.getLocation(), variable.getSize(), uniforms);
                break;
            case GLES20.GL_FLOAT_VEC4:
                gles.glUniform4fv(variable.getLocation(), variable.getSize(), uniforms);
                break;
            case GLES20.GL_FLOAT_MAT2:
                gles.glUniformMatrix2fv(variable.getLocation(), variable.getSize(), false, uniforms);
                break;
            case GLES20.GL_FLOAT_MAT3:
                gles.glUniformMatrix3fv(variable.getLocation(), variable.getSize(), false, uniforms);
                break;
            case GLES20.GL_FLOAT_MAT4:
                gles.glUniformMatrix4fv(variable.getLocation(), variable.getSize(), false, uniforms);
                break;
            case GLES20.GL_SAMPLER_2D:
                samplers.position(offset);
                gles.glUniform1iv(variable.getLocation(), variable.getSize(), samplers);
                break;
            case GLES30.GL_SAMPLER_2D_SHADOW:
                samplers.position(offset);
                gles.glUniform1iv(variable.getLocation(), variable.getSize(), samplers);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for dataType: " + variable.getDataType());
        }
        GLUtils.handleError(gles, "setUniform(), dataType: " + variable.getDataType());

    }

    /**
     * Returns the size of all the shader variables of the specified type, either ATTRIBUTE or UNIFORM
     * EXCLUDING the size of Sampler2D variables.
     * For uniforms this corresponds to the total size buffer size needed - the size of Sampler2D variables.
     * For attributes this corresponds to the total buffer size needed, normally attribute data is put in
     * dynamic and static buffers.
     * 
     * @param variables
     * @param type
     * @param index BufferIndex to the buffer that the variables belong to, or null
     * @return Total size, in floats, of all defined shader variables of the specified type
     */
    protected int getVariableSize(ShaderVariable[] variables, VariableType type) {
        int size = 0;
        for (ShaderVariable v : variables) {
            if (v != null && v.getType() == type && v.getDataType() != GLES20.GL_SAMPLER_2D) {
                size += v.getSizeInFloats();
            }
        }
        return alignVariableSize(size, type);
    }

    /**
     * returns the size, in basic machine units, of all active variables in the block.
     * 
     * @param block
     * @return
     */
    protected int getVariableSize(InterfaceBlock block) {
        int size = 0;
        for (int index : block.indices) {
            ShaderVariable variable = this.blockVariables.get(index);
            size += variable.getSizeInBytes();
        }
        return size;
    }

    /**
     * Align the size of the variables (per vertex) of the type in the buffer with index, override in
     * subclasses if for instance
     * Attributes shall be aligned to a specific size, eg vec4
     * 
     * @param size The packed size
     * @param type
     * @return The aligned size of variables per vertex.
     */
    protected int alignVariableSize(int size, VariableType type) {
        return size;
    }

    /**
     * Returns the size of Sampler2D variables
     * 
     * @param variables
     * @return
     */
    protected int getSamplerSize(ShaderVariable[] variables) {
        int size = 0;
        for (ShaderVariable v : variables) {
            if (v != null && v.getType() == VariableType.UNIFORM)
                switch (v.getDataType()) {
                    case GLES20.GL_SAMPLER_2D:
                    case GLES20.GL_SAMPLER_CUBE:
                    case GLES30.GL_SAMPLER_2D_SHADOW:
                    case GLES30.GL_SAMPLER_2D_ARRAY:
                    case GLES30.GL_SAMPLER_2D_ARRAY_SHADOW:
                    case GLES30.GL_SAMPLER_CUBE_SHADOW:
                    case GLES30.GL_SAMPLER_3D:
                        size += v.getSizeInFloats();
                }
        }
        return size;

    }

    /**
     * Returns a list with samplers from array of ShaderVariables
     * 
     * @param variables
     * @return
     */
    protected ArrayList<ShaderVariable> getSamplers(ShaderVariable[] variables) {
        ArrayList<ShaderVariable> samplers = new ArrayList<>();
        for (ShaderVariable v : variables) {
            if (v != null && v.getType() == VariableType.UNIFORM)
                switch (v.getDataType()) {
                    case GLES20.GL_SAMPLER_2D:
                    case GLES20.GL_SAMPLER_CUBE:
                    case GLES30.GL_SAMPLER_2D_SHADOW:
                    case GLES30.GL_SAMPLER_2D_ARRAY:
                    case GLES30.GL_SAMPLER_2D_ARRAY_SHADOW:
                    case GLES30.GL_SAMPLER_CUBE_SHADOW:
                    case GLES30.GL_SAMPLER_3D:
                        samplers.add(v);
                }
        }
        return samplers;
    }

    /**
     * 
     * Sets the data for the uniform matrices needed by the program - the default implementation will set the modelview
     * and projection matrices. Will NOT set uniforms to GL, only update the uniform array store
     * 
     * @param matrices Source matrices to set to uniform data array.
     */
    public void setUniformMatrices(float[][] matrices) {
        // Refresh the uniform matrixes - default is model - view and projection
        if (modelUniform == null) {
            modelUniform = getUniformByName(Matrices.MODEL.name);
        }
        uniforms.position(modelUniform.getOffset());
        uniforms.put(matrices[Matrices.MODEL.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.VIEW.index], 0, Matrix.MATRIX_ELEMENTS);
        uniforms.put(matrices[Matrices.PROJECTION.index], 0, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Initializes the uniform data for this program.
     * Is called after program is linked and uniform buffers are created, variables and blocks are resolved.
     * 
     * @param destinationUniforms
     */
    public abstract void initUniformData(FloatBuffer destinationUniforms);

    /**
     * Updates the shader program specific uniform data, storing in in the uniformData array or
     * {@link #uniformBlockBuffers}
     * Subclasses shall set any uniform data needed - but not matrices which is set in
     * {@link #setUniformMatrices(float[][])}
     * 
     * @param destinationUniforms
     */
    public abstract void updateUniformData(FloatBuffer destinationUniform);

    /**
     * Sets UV fraction for the tiled texture + number of frames in x.
     * Use this for programs that use tiled texture behavior.
     * 
     * @param texture
     * @param uniforms Will store 1 / tilewidth, 1 / tilewidth, tilewidth, beginning at offset
     * @param variable The shader variable
     * @param offset Offset into destination where fraction is set
     */
    protected void setTextureUniforms(TiledTexture2D texture, FloatBuffer uniforms, ShaderVariable variable) {
        if (texture.getWidth() == 0 || texture.getHeight() == 0) {
            SimpleLogger.d(getClass(), "ERROR! Texture size is 0: " + texture.getWidth() + ", " + texture.getHeight());
        }
        uniforms.position(variable.getOffset());
        uniforms.put((((float) texture.getWidth()) / texture.getTileWidth()) / (texture.getWidth()));
        uniforms.put((((float) texture.getHeight()) / texture.getTileHeight()) / (texture.getHeight()));
        uniforms.put(texture.getTileWidth());
    }

    /**
     * Sets the screensize to uniform storage
     * 
     * @param uniforms
     * @param uniformScreenSize
     */
    protected void setScreenSize(FloatBuffer uniforms, ShaderVariable uniformScreenSize) {
        if (uniformScreenSize != null) {
            uniforms.position(uniformScreenSize.getOffset());
            uniforms.put(Window.getInstance().getWidth());
            uniforms.put(Window.getInstance().getHeight());
        }
    }

    /**
     * Sets the emissive light color in uniform data
     * 
     * @param uniforms
     * @param uniformEmissive
     * @param material
     */
    protected void setEmissive(FloatBuffer uniforms, ShaderVariable uniformEmissive, float[] emissive) {
        uniforms.position(uniformEmissive.getOffset());
        uniforms.put(emissive, 0, 4);
    }

    /**
     * Returns the key value for this shader program, this is the classname and possible name of shader used.
     * This method is used by {@link AssetManager} when programs are compiled and stored.
     * 
     * @return Key value for this shader program.
     */
    public String getKey() {
        return getClass().getSimpleName() + function.getShadingString() + function.getCategoryString();
    }

    /**
     * Returns the shading that this program supports
     * 
     * @return
     */
    public Shading getShading() {
        return function.shading;
    }

    /**
     * Creates the buffer holding samplers to use
     * 
     * @param size number of samplers used
     * 
     */
    private void createSamplers(int size) {
        this.samplers = BufferUtils.createIntBuffer(size);
    }

    /**
     * Creates the buffer to hold the block variable data
     * 
     * @param block The block to create the buffer for
     * @param size The size, in bytes to allocate.
     */
    protected BlockBuffer createBlockBuffer(InterfaceBlock block, int size) {
        // Size is in bytes, align to floats
        FloatBlockBuffer fbb = new FloatBlockBuffer(block, size >>> 2);
        return fbb;
    }

    /**
     * Sets the texture units to use for each sampler, default behavior is to start at unit 0 and increase for each
     * sampler.
     */
    protected void setSamplers() {
        ArrayList<ShaderVariable> samplersList = getSamplers(activeUniforms);
        if (samplersList.size() > 0) {
            samplers.rewind();
            for (int i = 0; i < samplersList.size(); i++) {
                samplers.put(samplersList.get(i).getOffset());
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sourceNames = new StringBuffer();
        for (ShaderSource s : shaderSources) {
            sourceNames.append("\n" + s.getFullSourceName());
        }
        return shaders + " : " + function.toString() + "\n" + sourceNames.toString();
    }

    private void createCommonShaders(GLES20Wrapper gles, ShaderSource[] sources) throws GLException, IOException {
        switch (shaders) {
            case COMPUTE:
                createCommonShaders(gles, ShaderType.COMPUTE, sources[0].getSourceNameVersion());
                break;
            case VERTEX_FRAGMENT:
                createCommonShaders(gles, ShaderType.VERTEX, sources[0].getSourceNameVersion());
                createCommonShaders(gles, ShaderType.FRAGMENT, sources[0].getSourceNameVersion());
                break;
            case VERTEX_GEOMETRY_FRAGMENT:
                createCommonShaders(gles, ShaderType.VERTEX, sources[0].getSourceNameVersion());
                createCommonShaders(gles, ShaderType.GEOMETRY, sources[0].getSourceNameVersion());
                createCommonShaders(gles, ShaderType.FRAGMENT, sources[0].getSourceNameVersion());
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + shaders);
        }

    }

    /**
     * Creates the common shaders that can be used to share functions between shaders.
     * 
     * @param gles
     * @param type
     * @param sourceNameVersion
     * @throws GLException
     */
    private void createCommonShaders(GLES20Wrapper gles, ShaderType type, String sourceNameVersion)
            throws GLException, IOException {
        String[] common = getCommonShaderName(type);
        if (common != null) {
            ShaderSource[] commonSources = new ShaderSource[common.length];
            for (int i = 0; i < commonSources.length; i++) {
                commonSources[i] = new ShaderSource(common[i], sourceNameVersion, type);
            }
            loadShaderSources(gles, commonSources);
            SimpleLogger.d(getClass(), "Adding common sources : " + StringUtils.getString(common));
            createCommonSources(gles, commonSources, type);
        }
    }

    /**
     * Returns the common (library) shader name for the specified type.
     * Override to include needed files.
     * 
     * @param type
     * @return
     */
    protected String[] getCommonShaderName(ShaderType type) {
        // Default is to not use common library
        return null;
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

    /**
     * Returns the common shader source for the specified type - call this when appending the common shader source
     * to a shader program.
     * 
     * @param type Shader type
     * @return
     */
    private String getCommonSources(ShaderType type) {
        if (commonSources[type.index] == null) {
            return null;
        }
        String result = new String();
        for (String source : commonSources[type.index]) {
            result += source;
        }
        return result;
    }
}
