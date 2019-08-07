package com.nucleus.opengl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.Assets;
import com.nucleus.common.BufferUtils;
import com.nucleus.common.Constants;
import com.nucleus.common.Environment;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLESWrapper.GLES31;
import com.nucleus.opengl.GLESWrapper.GLES32;
import com.nucleus.opengl.GLESWrapper.ProgramInfo;
import com.nucleus.opengl.shader.GLShaderSource;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.opengl.shader.NamedVariableIndexer;
import com.nucleus.opengl.shader.ShaderProgramException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material.AlphaMode;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.FloatBlockBuffer;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.Shader.Categorizer;
import com.nucleus.shader.Shader.ProgramType;
import com.nucleus.shader.Shader.ShaderType;
import com.nucleus.shader.ShaderBinary;
import com.nucleus.shader.ShaderSource;
import com.nucleus.shader.ShaderSource.SLVersion;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableIndexer;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;

/**
 * A pipeline holds the data that is needed for the processing stages.
 * Some of this data is immutable, such as shader and blend function
 *
 */
public class GLPipeline implements GraphicsPipeline<GLShaderSource> {

    /**
     * Shader suffix as added after checking for which version to use
     */
    public static final String FRAGMENT_TYPE_SUFFIX = ".fs";
    public static final String VERTEX_TYPE_SUFFIX = ".vs";
    public static final String GEOMETRY_TYPE_SUFFIX = ".gs";
    public static final String COMPUTE_TYPE_SUFFIX = ".cs";

    protected GLES20Wrapper gles;

    protected final static String MUST_SET_FIELDS = "Must set attributesPerVertex,vertexShaderName and fragmentShaderName";

    private final static String SHADER_SOURCE_ERROR = "Error setting shader source: ";
    private final static String COMPILE_SHADER_ERROR = "Error compiling shader: ";
    private final static String ATTACH_SOURCE_ERROR = "Error attaching shader source";
    private final static String LINK_PROGRAM_ERROR = "Error linking program: ";
    private final static String VARIABLE_LOCATION_ERROR = "Could not get shader variable location: ";
    private final static String NO_ACTIVE_UNIFORMS = "No active uniforms, forgot to call createProgram()?";
    private final static String CREATE_SHADER_ERROR = "Can not create shader object, context not active?";
    private final static String GET_PROGRAM_INFO_ERROR = "Error fetching program info.";

    private int[] shaderNames;
    /**
     * The GL program object
     */
    private int program = Constants.NO_VALUE;
    /**
     * Available after {@link #fetchProgramInfo(GLES20Wrapper)} has been called
     */
    private ProgramInfo info;
    protected int attributeBufferCount = BufferIndex.values().length;
    /**
     * attributes that are used in the compiled program
     */
    protected NamedShaderVariable[] activeAttributes;
    /**
     * uniforms that are used in the compiled program
     */
    protected NamedShaderVariable[] activeUniforms;
    /**
     * Uniform interface blocks
     */
    protected InterfaceBlock[] uniformInterfaceBlocks;
    protected BlockBuffer[] uniformBlockBuffers;
    /**
     * Samplers (texture units) - the texture unit to use for a shadervariable is stored at the intbuffer
     * position. To fetch texture unit to use for a shadervariable do: samplers.position(shadervariable.position())
     */
    transient protected IntBuffer samplers;
    /**
     * Block variables used in the compiled program - key is the uniform index from GL
     */
    protected HashMap<Integer, NamedShaderVariable> blockVariables = new HashMap<>();
    /**
     * Calculated in create program, created using {@link #attributeBufferCount}
     * If attributes are dynamically mapped (not using indexer) then only one buffer is used.
     */
    transient protected ShaderVariable[][] attributeVariables;
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
    protected BufferIndex defaultDynamicAttribBuffer = BufferIndex.ATTRIBUTES_STATIC;

    /**
     * Internal constructor - do not call directly, use
     * {@link Assets#getGraphicsPipeline(NucleusRenderer, GraphicsShader)
     * 
     * @param gles
     */
    public GLPipeline(GLES20Wrapper gles) {
        if (gles == null) {
            throw new IllegalArgumentException("GLES wrapper is null");
        }
        this.gles = gles;
    }

    @Override
    public void enable(NucleusRenderer renderer) throws BackendException {
        gles.glUseProgram(program);
        GLUtils.handleError(gles, "glUseProgram " + program);
        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
            validateProgram(gles);
        }

    }

    @Override
    public void update(NucleusRenderer renderer, Mesh mesh, float[][] matrices) throws BackendException {

        uploadAttributes(gles, mesh);
        prepareTexture(renderer, mesh.getTexture(Texture2D.TEXTURE_0));
        mesh.getMaterial().setBlendModeSeparate(gles);

    }

    @Override
    public void update(NucleusRenderer renderer, GLTF gltf, Primitive primitive, float[][] matrices)
            throws BackendException {

        // Can be optimized to update uniforms under the following conditions:
        // The program has changed OR the matrices have changed, ie another parent node.
        com.nucleus.scene.gltf.Material material = primitive.getMaterial();
        if (material != null) {
            // PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            // Check for doublesided.
            if (material.isDoubleSided()) {
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
            // ((GLTFShaderProgram) shader).prepareTextures(renderer, gltf, primitive, material);
            if (material.getAlphaMode() == AlphaMode.OPAQUE) {
                gles.glDisable(GLES20.GL_BLEND);
            } else {
                gles.glEnable(GLES20.GL_BLEND);
            }
        }
    }

    @Override
    public void glVertexAttribPointer(ArrayList<Attributes> attribs, ArrayList<Accessor> accessors)
            throws BackendException {
        for (int i = 0; i < attribs.size(); i++) {
            Accessor accessor = accessors.get(i);
            NamedShaderVariable v = getAttributeByName(attribs.get(i).name());
            if (v != null) {
                gles.glVertexAttribPointer(accessor, v);
            } else {
                // TODO - when fully implemented this should not happen.
            }
        }
        GLUtils.handleError(gles, "glVertexAttribPointer");
    }

    @Override
    public int[] getAttributeSizes() {
        return attributesPerVertex;
    }

    @Override
    public NamedShaderVariable getUniformByName(String uniform) {
        return getVariableByName(uniform, activeUniforms);
    }

    @Override
    public NamedShaderVariable getAttributeByName(String attribute) {
        return getVariableByName(attribute, activeAttributes);
    }

    @Override
    public void destroy(NucleusRenderer renderer) {
        // if (shader != null) {
        // gles.glDeleteProgram(program);
        // shader = null;
        // }
    }

    @Override
    public void compile(NucleusRenderer renderer, GraphicsShader shader) throws BackendException {
        variableIndexer = shader.getFunction().getIndexer();
        GLShaderSource[] shaderSources = createShaderSource(GLES20Wrapper.getInfo().getRenderVersion(),
                shader.getFunction(), shader.getType());
        if (shaderSources == null) {
            throw new ShaderProgramException(MUST_SET_FIELDS);
        }
        for (ShaderSource ss : shaderSources) {
            try {
                ss.loadShader(renderer.getBackend(), shader.getFunction());
            } catch (IOException e) {
                throw new BackendException(e);
            }
        }
        createProgram(renderer, shaderSources, shader);
    }

    /**
     * Sets the name of the shaders in this program and returns an array of {@link ShaderSource}, normally 2 - one for
     * vertex and one for fragment-shader.
     * This method must be called before the program is created.
     * 
     * @param version Backend API version
     * @param function
     * @param type
     */
    protected GLShaderSource[] createShaderSource(Renderers version, Categorizer function, ProgramType type) {
        GLShaderSource[] sources = null;
        ShaderType[] shaderTypes;
        switch (type) {
            case VERTEX_FRAGMENT:
                sources = new GLShaderSource[2];
                shaderTypes = new ShaderType[] { ShaderType.VERTEX, ShaderType.FRAGMENT };
                break;
            case COMPUTE:
                sources = new GLShaderSource[1];
                shaderTypes = new ShaderType[] { ShaderType.COMPUTE };
                break;
            case VERTEX_GEOMETRY_FRAGMENT:
                sources = new GLShaderSource[3];
                shaderTypes = new ShaderType[] { ShaderType.VERTEX, ShaderType.GEOMETRY,
                        ShaderType.FRAGMENT };
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + type);
        }
        for (int i = 0; i < shaderTypes.length; i++) {
            sources[i] = getShaderSource(version, function, shaderTypes[i]);
        }
        return sources;
    }

    @Override
    public GLShaderSource getShaderSource(Renderers version, Categorizer function, ShaderType type) {
        String sourceNameVersion = getSourceNameVersion(version);
        switch (type) {
            case VERTEX:
                return new GLShaderSource(
                        ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion, function.getShaderSourceName(type),
                        function, VERTEX_TYPE_SUFFIX, type);
            case FRAGMENT:
                return new GLShaderSource(
                        ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion, function.getShaderSourceName(type),
                        function, FRAGMENT_TYPE_SUFFIX, type);
            case COMPUTE:
                return new GLShaderSource(
                        ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion, function.getShaderSourceName(type),
                        function, COMPUTE_TYPE_SUFFIX, type);
            case GEOMETRY:
                return new GLShaderSource(
                        ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion, function.getShaderSourceName(type),
                        function, GEOMETRY_TYPE_SUFFIX, type);
            default:
                throw new IllegalArgumentException("Not implemented for type: " + type);
        }
    }

    /**
     * This is used to be able to load different shader sources depending on shading language version.
     * Override this if different source shall be used depending on available renderer/shader version.
     * 
     * @param version Highest shading language version that is supported
     * @return Empty string "", or shader version to append to source name if different shader source shall be used for
     * a specific shader version.
     */
    protected String getSourceNameVersion(Renderers version) {
        switch (version) {
            case GLES20:
                return ShaderSource.V200 + "/";
            case GLES30:
            case GLES31:
            case GLES32:
                return ShaderSource.V300 + "/";
            default:
                throw new IllegalArgumentException("Not implemented for " + version);
        }
    }

    @Override
    public String getFilenameSuffix(ShaderType type) {
        switch (type) {
            case VERTEX:
                return VERTEX_TYPE_SUFFIX;
            case FRAGMENT:
                return FRAGMENT_TYPE_SUFFIX;
            case COMPUTE:
                return COMPUTE_TYPE_SUFFIX;
            case GEOMETRY:
                return GEOMETRY_TYPE_SUFFIX;
            default:
                throw new IllegalArgumentException("Not implemented for type: " + type);

        }
    }

    @Override
    public void createProgram(NucleusRenderer renderer, GLShaderSource[] sources, GraphicsShader shader)
            throws BackendException {
        SimpleLogger.d(getClass(),
                "Creating program for: " + sources.length + " shaders in program " + getClass().getSimpleName()
                        + ", sources are:");
        for (ShaderSource ss : sources) {
            SimpleLogger.d(getClass(), ss.getFullSourceName());
        }
        GLES20Wrapper gles = (GLES20Wrapper) renderer.getBackend();
        try {
            // loadShaderSources(gles, sources);
            // createCommonShaders(gles);
            SLVersion minVersion = ShaderSource.getMinVersion(sources);
            shaderNames = new int[sources.length];
            program = gles.glCreateProgram();
            for (int shaderIndex = 0; shaderIndex < sources.length; shaderIndex++) {
                // Insert the correct version depending on platform implementation.
                sources[shaderIndex].setShaderVersion(gles.replaceShaderVersion(minVersion));

                SimpleLogger.d(getClass(),
                        "Compiling " + sources[shaderIndex].getFullSourceName());
                shaderNames[shaderIndex] = compileShader(gles, sources[shaderIndex], shader);

            }
            linkProgram(gles, program, shaderNames);
            checkLinkStatus(gles, program);
            fetchProgramInfo(gles);
            mapAttributeOffsets(gles, (NamedVariableIndexer) variableIndexer);
            setAttributesPerVertex();
            if (GLES20Wrapper.getInfo().getRenderVersion().major >= 3) {
                uniformBlockBuffers = createUniformBlockBuffers(renderer);
            }
            createSamplerStorage();
            setSamplers();
        } catch (GLCompilerException e) {
            logNumberedShaderSource(gles, e.shader);
            SimpleLogger.d(getClass(), e.getMessage() + " from source:" + System.lineSeparator());
            throw e;
        } catch (BackendException e) {
            logShaderSources(gles, shaderNames);
            throw e;
        }
    }

    /**
     * Creates the shader name, attaches the source and compiles the shader.
     * 
     * @param gles
     * @param source
     * @param library true if this is not the main shader
     * @return The created shader
     * @throws GLException If there is an error setting or calling to compiling shader source.
     * @throws GLCompilerException If compilation failed
     */
    public int compileShader(GLES20Wrapper gles, GLShaderSource source, GraphicsShader program)
            throws GLException, GLCompilerException {
        int shader = gles.glCreateShader(getShaderValue(source.type));
        if (shader == 0) {
            throw new GLException(CREATE_SHADER_ERROR, GLES20.GL_NO_ERROR);
        }
        source.appendSource(ShaderSource.PRECISION, source.getCommonSources(source.type));
        insertDefines(source, program);
        compileShader(gles, source, shader);
        return shader;
    }

    protected void insertDefines(ShaderSource source, GraphicsShader shader) {
        if (shader instanceof GLTFShaderProgram) {
            source.insertDefines(ShaderSource.PRECISION, ((GLTFShaderProgram) shader).getDefines(source.type));
        }
    }

    /**
     * Compiles the shader from the shader source.
     * The GL version will be appended to the source, calling {@link GLESWrapper#getShaderVersion()}
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param source The shader source
     * @param shader OpenGL object to compile the shader to.
     * @throws GLException If there is an error setting or calling to compile shader source
     * @throws GLCompilerException If compilation failed
     */
    public void compileShader(GLES20Wrapper gles, ShaderSource source, int shader)
            throws GLException, GLCompilerException {
        String sourceStr = null;
        if (source.getSource() == null) {
            throw new IllegalArgumentException("Shader source is null for " + source.getFullSourceName());
        }
        sourceStr = source.getVersionedShaderSource();
        // These calls only return an error if there is an error in the parameters, invalid shader etc.
        // ie it does not mean compilation is successful - check compile status to know.
        gles.glShaderSource(shader, sourceStr);
        GLUtils.handleError(gles, SHADER_SOURCE_ERROR + source.getFullSourceName());
        gles.glCompileShader(shader);
        GLUtils.handleError(gles, COMPILE_SHADER_ERROR + source.getFullSourceName());

        // Check compilation status to know if compilation was success or failure
        checkCompileStatus(gles, source, shader);
    }

    /**
     * Checks the compile status of the specified shader program - if shader is not successfully compiled an exception
     * is thrown.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param source
     * @param shader
     * @throws GLCompilerException If there is an error compiling the shader
     */
    protected void checkCompileStatus(GLES20Wrapper gles, ShaderSource source, int shader) throws GLCompilerException {
        IntBuffer compileStatus = BufferUtils.createIntBuffer(1);
        gles.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus);
        if (compileStatus.get(0) != GLES20.GL_TRUE) {
            throw new GLCompilerException(compileStatus.get(0), shader, source, gles.glGetShaderInfoLog(shader));
        }
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
        activeAttributes = new NamedShaderVariable[info.getActiveVariables(VariableType.ATTRIBUTE)];
        activeUniforms = new NamedShaderVariable[info.getActiveVariables(VariableType.UNIFORM)];
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

    private int getShaderValue(ShaderType type) {
        switch (type) {
            case VERTEX:
                return GLES20.GL_VERTEX_SHADER;
            case FRAGMENT:
                return GLES20.GL_FRAGMENT_SHADER;
            case GEOMETRY:
                return GLES32.GL_GEOMETRY_SHADER;
            case COMPUTE:
                return GLES31.GL_COMPUTE_SHADER;
        }
        return -1;
    }

    /**
     * Links the specified vertex and fragment shaders to the specified program.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param program
     * @param shaderNames
     * @throws GLException If the program could not be linked with the shaders.
     */
    private void linkProgram(GLES20Wrapper gles, int program, int[] shaderNames)
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
                logNumberedShaderSource(gles, name);
            }
        }
    }

    /**
     * Logs the shader source for the specified shader, using numbered lines.
     * 
     * @param gles
     * @param shader
     */
    private void logNumberedShaderSource(GLES20Wrapper gles, int shader) {
        String shaderSource = gles.glGetShaderSource(shader);
        // Android does not have full support for Java 8
        Iterator<String> i = new BufferedReader(new StringReader(shaderSource)).lines().iterator();
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (i.hasNext()) {
            sb.append(index++ + " " + i.next() + System.lineSeparator());

        }
        SimpleLogger.d(getClass(), System.lineSeparator() + sb.toString());
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
        VariableType infoType = type != ShaderVariable.VariableType.UNIFORM_BLOCK ? type
                : VariableType.UNIFORM;
        byte[] nameBuffer = new byte[info.getMaxNameLength(infoType)];
        NamedShaderVariable variable = null;
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
    protected NamedShaderVariable getBlockVariable(VariableType type, int index) {
        NamedShaderVariable var = blockVariables.get(index);
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
    protected void setVariableLocation(GLES20Wrapper gles, int program, NamedShaderVariable variable)
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
     * Stores the shader variable in this program, if variable is of unmapped type, for instance Sampler, then it is
     * skipped. Also skip variables that are defined in code but not used in shader.
     * Variables are stored in {@link #activeUniforms} or {@link #activeAttributes}
     * 
     * @param variable
     * @throws IllegalArgumentException If shader variables are null, the program has probably not been created,
     * or if a variable has no mapping in the code.
     */
    protected void addShaderVariable(NamedShaderVariable variable) {
        setShaderVariable(variable);
    }

    /**
     * Sets the active shader variable into {@link NamedShaderVariable} array - call this when variable has been
     * validated.
     * 
     * @param variable
     */
    protected void setShaderVariable(NamedShaderVariable variable) {
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
     * Maps shader variables to defined offsets or updates the shader variable offsets to runtime values
     * 
     * @param gles
     * @indexer Offsets to use for shader variables, if specified active variables will be mapped to offsets
     * as found when comparing variable name.
     * If null then offsets will be updated according to used variables in an increasing manner and will be tightly
     * packed.
     * @throws GLException
     */
    protected void mapAttributeOffsets(GLES20Wrapper gles, NamedVariableIndexer indexer) throws GLException {
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
    protected void setVariableOffsets(GLES20Wrapper gles, NamedShaderVariable[] variables,
            NamedVariableIndexer indexer) {
        for (NamedShaderVariable v : variables) {
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
            for (int i = 0; i < variableIndexer.getSizesPerVertex().length; i++) {
                attributesPerVertex[i] = variableIndexer.getSizePerVertex(i);
            }
        }

    }

    /**
     * Creates the block (uniform) buffers needed for this program, if any are used.
     * Always creates a {@link FloatBlockBuffer} for uniforms.
     * If BlockBuffer is needed for other type than uniforms it needs to be implemented.
     * Binds uniform block to a binding point using the block index, this means that there is one binding point
     * per uniform block.
     * 
     * @param renderer
     * @return Uniform variable block buffers, using buffer objects, for this program, or null if not used.
     */
    protected BlockBuffer[] createUniformBlockBuffers(NucleusRenderer renderer) throws BackendException {
        if (uniformInterfaceBlocks == null) {
            return null;
        }
        for (InterfaceBlock block : uniformInterfaceBlocks) {
            GLES30Wrapper gles = (GLES30Wrapper) renderer.getBackend();
            // Here the binding point and block index is the same.
            gles.glUniformBlockBinding(program, block.blockIndex, block.blockIndex);
        }
        BlockBuffer[] buffers = BlockBuffer.createBlockBuffers(uniformInterfaceBlocks);
        renderer.getBufferFactory().createUBOs(buffers);
        return buffers;
    }

    @Override
    public int getVariableSize(VariableType type) {
        switch (type) {
            case ATTRIBUTE:
                return getVariableSize(activeAttributes, type);
            case UNIFORM:
                return getVariableSize(activeUniforms, type);
            case UNIFORM_BLOCK:
            default:
                throw new IllegalArgumentException("Not implemented");

        }
    }

    private int getVariableSize(ShaderVariable[] variables, VariableType type) {
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
     * subclasses if for instance Attributes shall be aligned to a specific size, eg vec4
     * 
     * @param size The packed size
     * @param type
     * @return The aligned size of variables per vertex.
     */
    protected int alignVariableSize(int size, VariableType type) {
        return size;
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
     * Creates the buffer holding samplers to use
     * 
     * @param size number of samplers used
     * 
     */
    private void createSamplers(int size) {
        this.samplers = BufferUtils.createIntBuffer(size);
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
     * Sets the texture units to use for each sampler, default behavior is to start at unit 0 and increase for each
     * sampler.
     * The {@link #samplers} array will contain the texture unit to use for each offset.
     * To use, position the samplers array using samplers.position(shadervariable.getOffset()) - the index
     * will contain the texture unit to use
     */
    protected void setSamplers() {
        ArrayList<ShaderVariable> samplersList = getSamplers(activeUniforms);
        if (samplersList.size() > 0) {
            for (int i = 0; i < samplersList.size(); i++) {
                ShaderVariable sampler = samplersList.get(i);
                samplers.position(sampler.getOffset());
                samplers.put(i);
            }
        }
    }

    /**
     * Sorts the attributes used based on BufferIndex - attribute variables are sorted based on buffer in the specified
     * result array.
     * Finds the shader attribute variables per buffer using VariableMapping, iterate through defined (by subclasses)
     * attribute variable mapping.
     * Put the result in the result array and set the {@linkplain NamedShaderVariable} offset based on used attributes.
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
            resultArray[defaultDynamicAttribBuffer.index] = new NamedShaderVariable[info
                    .getActiveVariables(VariableType.ATTRIBUTE)];
            if (resultArray[defaultDynamicAttribBuffer.index].length != activeAttributes.length) {
                throw new IllegalArgumentException("Active variable array size mismatch - active count from info "
                        + info.getActiveVariables(VariableType.ATTRIBUTE) + ", array size "
                        + activeAttributes.length);
            }
            resultArray[BufferIndex.ATTRIBUTES.index] = activeAttributes;
        } else {
            for (int index = 0; index < resultArray.length; index++) {
                resultArray[index] = sortByBuffer(variableIndexer, activeAttributes, index);
            }
        }
    }

    /**
     * Sort the variables belonging to the specified buffer index. Returning an array with the variables.
     * 
     * @param mapper
     * @param activeVariables
     * @param index Index of the buffer
     * @return
     */
    protected ShaderVariable[] sortByBuffer(VariableIndexer mapper, NamedShaderVariable[] activeVariables,
            int index) {
        ArrayList<ShaderVariable> result = new ArrayList<>();
        for (NamedShaderVariable v : activeVariables) {
            BufferIndex bi = variableIndexer
                    .getBufferIndex(((NamedVariableIndexer) variableIndexer).getIndexByName(v.getName()));
            if (bi != null && bi.index == index) {
                result.add(v);
            }
        }
        ShaderVariable[] array = new ShaderVariable[result.size()];
        return result.toArray(array);
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
    public void uploadAttributes(GLES20Wrapper gles, AttributeUpdater mesh) throws GLException {
        for (int i = 0; i < attributeVariables.length; i++) {
            AttributeBuffer buffer = mesh.getAttributeBuffer(i);
            if (buffer != null) {
                gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attributeVariables[i]);
                GLUtils.handleError(gles, "glVertexAttribPointers ");
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
     * Uploads the uniforms to render backend
     * When this method returns the uniform data has been uploaded to GL and is ready.
     * 
     * @param gles
     */
    public void uploadUniforms(GLES20Wrapper gles, GraphicsShader shader)
            throws GLException {
        uploadUniforms(gles, shader.getUniformData(), activeUniforms);
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
     * Uploads one of more float uniforms for the specified variable to GL, supports VEC2, VEC3, VEC4 and MAT2, MAT3,
     * MAT4 types
     * 
     * @param gles
     * @param uniforms The uniform data
     * @param variable Shader variable to set uniform data for, datatype and size is read. If null then nothing is done
     * @param offset Offset into uniform array where data starts.
     * @throws GLException If there is an error setting a uniform to GL
     */
    public final void uploadUniform(GLES20Wrapper gles, FloatBuffer uniforms, ShaderVariable variable)
            throws GLException {
        if (variable == null) {
            return;
        }
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
        if (GLUtils.handleError(gles,
                "setUniform: " + variable.getLocation() + ", dataType: " + variable.getDataType() +
                        ", size " + variable.getSize())) {
            /**
             * TODO - log the names of the shaders used in this program.
             */
        }

    }

    /**
     * Prepares a texture used before rendering starts.
     * This shall set texture parameters to used textures, ie activate texture, bind texture then set parameters.
     * TODO - This should be moved to a class that handles nucleus texture/mesh
     * 
     * @param renderer
     * @param texture
     * @throws BackendException
     */
    @Deprecated
    public void prepareTexture(NucleusRenderer renderer, Texture2D texture) throws BackendException {
        if (texture == null || texture.getTextureType() == TextureType.Untextured) {
            return;
        }
        /**
         * TODO - make texture names into enums
         */
        int unit = samplers.get(getUniformByName("uTexture").getOffset());
        renderer.prepareTexture(texture, unit);
    }

    protected NamedShaderVariable getVariableByName(String name, NamedShaderVariable[] variables) {
        for (NamedShaderVariable v : variables) {
            if (v != null && v.getName().contentEquals(name)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void uploadVariable(FloatBuffer data, ShaderVariable variable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void uploadUniforms(FloatBuffer uniformData, ShaderVariable[] activeUniforms) throws BackendException {
        uploadUniforms(gles, uniformData, activeUniforms != null ? activeUniforms : this.activeUniforms);
    }

    @Override
    public void uploadAttributes(FloatBuffer attributeData, ShaderVariable[] activeAttributes) {
    }

    @Override
    public ShaderVariable[] getActiveVariables(VariableType type) {
        switch (type) {
            case ATTRIBUTE:
                return activeAttributes;
            case UNIFORM:
                return activeUniforms;
            default:
                throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public BlockBuffer[] getUniformBlocks() {
        return uniformBlockBuffers;
    }

    @Override
    public int getTextureUnit(ShaderVariable sampler) {
        return samplers.get(sampler.getOffset());
    }

    @Override
    public VariableIndexer getLocationMapping() {
        return variableIndexer;
    }

}
