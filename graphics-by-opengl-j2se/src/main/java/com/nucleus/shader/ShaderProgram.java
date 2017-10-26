package com.nucleus.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nucleus.SimpleLogger;
import com.nucleus.assets.AssetManager;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.Consumer;
import com.nucleus.geometry.AttributeUpdater.Property;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.io.StreamUtils;
import com.nucleus.light.GlobalLight;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.Window;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.Matrix;

/**
 * This class handles loading, compiling and linking of OpenGL ES shader programs.
 * Implement for each program to make a mapping between shader variable names and GLES attribute/uniform locations.
 * A ShaderProgram object shall contain information that is specific to the shader program compilation and linking.
 * Uniform and attribute mapping shall be included but not the uniform and attribute data - this is so that the same
 * ShaderProgram instance can be used to render multiple objects.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class ShaderProgram {

    public static final String PROGRAM_DIRECTORY = "assets/";
    public static final String SHADER_SOURCE_SUFFIX = ".essl";
    public static final String FRAGMENT = "fragment";
    public static final String VERTEX = "vertex";
    protected final static String MUST_SET_FIELDS = "Must set attributesPerVertex,vertexShaderName and fragmentShaderName";
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

    /**
     * Enum used when the attribute locations shall be automatically bound.
     * The mode can be either APPEND or PREFIX.
     * APPEND means that the location to bind the attribute to is appended after the name - must contain 2 numbers,
     * eg 00 for first location.
     * PREFIX means that the name is prefixed with the attribute location, must contain 2 numbers, eg 00 for first
     * location.
     * 
     * @author Richard Sahlin
     *
     */
    public enum AttribNameMapping {
        APPEND(), 
        PREFIX();
    }

    public final static String SHADER_SOURCE_ERROR = "Error setting shader source: ";
    public final static String COMPILE_SHADER_ERROR = "Error compiling shader: ";
    public final static String CREATE_SHADER_ERROR = "Can not create shader object, context not active?";
    public final static String LINK_PROGRAM_ERROR = "Error linking program: ";
    public final static String BIND_ATTRIBUTE_ERROR = "Error binding attribute: ";
    public final static String VARIABLE_LOCATION_ERROR = "Could not get shader variable location: ";
    public final static String NULL_VARIABLES_ERROR = "ShaderVariables are null, program not created? Must call fetchProgramInfo()";
    public final static String GET_PROGRAM_INFO_ERROR = "Error fetching program info.";

    /**
     * Index into array where active (attribute or uniform) variable is stored, used when
     * calling GL
     */
    protected final static int ACTIVE_COUNT_OFFSET = 0;
    /**
     * Index into array where max name length (attribute or uniform) for variable is stored, used when calling GL
     */
    protected final static int MAX_NAME_LENGTH_OFFSET = 1;

    /**
     * Used when calling glGetActiveAttrib as offset into data to be written
     */
    protected final static int SIZE_OFFSET = 1;
    /**
     * Used when calling glGetActiveAttrib as offset into data to be written
     */
    protected final static int TYPE_OFFSET = 2;
    /**
     * Used when calling glGetActiveAttrib as offset into data to be written
     */
    protected final static int NAME_LENGTH_OFFSET = 0;

    /**
     * The GL program object
     */
    private int program = -1;
    /**
     * The GL vertex shader object
     */
    private int vertexShader = -1;
    /**
     * The GL fragment shader object
     */
    private int fragmentShader = -1;

    /**
     * This is the main array holding all active shader variables
     * It is set when {@link #addShaderVariable(ShaderVariable)} is called.
     * Use {@link #getShaderVariable(VariableMapping)} to fetch variable.
     */
    protected ShaderVariable[] shaderVariables;

    private AttribNameMapping attribNameMapping = null;

    /**
     * Calculated in create program
     */
    protected ShaderVariable[][] attributeVariables;

    protected GlobalLight globalLight = GlobalLight.getInstance();
    /**
     * The size of each buffer for the attribute variables
     */
    protected int[] attributesPerVertex;
    protected VariableMapping[] attributes; // List of attributes defined by a program
    protected int attributeBufferCount; // Number of buffers used by attributes
    protected VariableMapping[] sourceUniforms; // List of uniforms defined by a program

    /**
     * Uniforms, used when rendering this Mesh depending on what ShaderProgram is used.
     */
    transient protected float[] uniforms;
    /**
     * Samplers (texture units)
     */
    transient protected int[] samplers;

    /**
     * The following fields MUST be set by subclasses
     */
    protected String vertexShaderName;
    protected String fragmentShaderName;
    //Optional
    protected Texture2D.Shading shading;

    /**
     * Unmapped variable types
     */
    protected List<Integer> unMappedTypes = new ArrayList<>();

    /**
     * Returns the program for the specified pass and shading, this is used to resolve the correct
     * program for different passes
     * @param renderer
     * @param pass
     * @param shading
     */
    public abstract ShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Texture2D.Shading shading);
    
    /**
     * Returns the offset within an attribute buffer where the property is, this is used to set specific properties
     * of a vertex.
     * This will be the same for all vertices, you only need to fetch this once. It will not change as long
     * as the same program is used.
     * 
     * @param property
     * @return Offset into attribute (buffer) where the storage for the specified property is, or -1 if the property
     * is not supported.
     */
    public int getPropertyOffset(Property property) {
        ShaderVariable v = null;
        switch (property) {
        case TRANSLATE:
            v = shaderVariables[ShaderVariables.aTranslate.index];
            break;
        case ROTATE:
            v = shaderVariables[ShaderVariables.aRotate.index];
            break;
        case SCALE:
            v = shaderVariables[ShaderVariables.aScale.index];
            break;
        case FRAME:
            v = shaderVariables[ShaderVariables.aFrameData.index];
            break;
        case COLOR_AMBIENT:
        case COLOR:
            v = shaderVariables[ShaderVariables.aColor.index];
            break;
        default:
        }
        if (v != null) {
            return v.getOffset();
        } else {
            SimpleLogger.d(getClass(), "No ShaderVariable for " + property);
        }
        return -1;
    }

    /**
     * Returns the variable mapping for the shader variable, the mapping is used to find buffer index and offsets.
     * 
     * @param variable The shader variable to get the variable mapping for.
     * @throws IllegalArgumentException If the shader variable has no variable mapping in the subclass.
     * @throws NullPointerException If variable is null
     */
    public VariableMapping getVariableMapping(ShaderVariable variable) {
        return ShaderVariables.valueOf(getVariableName(variable));
    }

    /**
     * Returns the number of defined attribute + uniform variables in the program.
     * This is to make it easier when developing so that temporarily unused variabled do not need to be removed.
     * 
     * @return Number of defined variables in the shader program, all variables do not need to be used.
     */
    public int getVariableCount() {
        return ShaderVariables.values().length;
    }
    
    /**
     * Creates a new shader program for the specified shading - used by subclasses
     * @param shading
     * @param mapping
     */
    protected ShaderProgram(Texture2D.Shading shading, VariableMapping[] mapping) {
        super();
        this.shading = shading;
        setMapping(mapping);
        setShaderSource(shading);
    }
    
    /**
     * Creates a new ShaderProgram with the variable mapping, used by subclasses to create instance of shader.
     * 
     * @param mapping The variable mapping as defined by the subclass, this holds information of where uniform and
     * attribute data is
     */
    protected ShaderProgram(VariableMapping[] mapping) {
        super();
        setMapping(mapping);
        setShaderSource(null);
    }
    
    protected void setMapping(VariableMapping[] mapping) {
        setUniformMapping(mapping);
        setAttributeMapping(mapping);
    }

    /**
     * Sets the shading and the name of the vertex/fragment shaders
     * @param shading
     */
    protected void setShaderSource(Texture2D.Shading shading) {
        //TODO - need a name together with shading to connect to shader, eg 'Translate', 'Transform' or 'Shadow'
        vertexShaderName = PROGRAM_DIRECTORY + shading.name() + VERTEX + SHADER_SOURCE_SUFFIX;
        fragmentShaderName = PROGRAM_DIRECTORY + shading.name() + FRAGMENT + SHADER_SOURCE_SUFFIX;
    }
    
    /**
     * Returns the number of attribute buffers - as found when calling {@link #createProgram(GLES20Wrapper)}
     * 
     * @return
     */
    public int getAttributeBufferCount() {
        return attributeBufferCount;
    }

    /**
     * Sets the uniform mapping as defined by subclass
     * 
     * @param mapping
     */
    protected void setUniformMapping(VariableMapping[] mapping) {
        ArrayList<VariableMapping> uniformList = new ArrayList<VariableMapping>();
        for (VariableMapping v : mapping) {
            if (v.getType() == VariableType.UNIFORM) {
                uniformList.add(v);
            }
        }
        sourceUniforms = uniformList.toArray(new VariableMapping[uniformList.size()]);
    }

    /**
     * Internal method
     * Fetches the attribute variable mappings, these are used to create per buffer attribute mappings.
     * The attribute buffer count is calculated.
     * 
     * @param mapping
     */
    protected void setAttributeMapping(VariableMapping[] mapping) {
        ArrayList<VariableMapping> attributeList = new ArrayList<VariableMapping>();
        for (VariableMapping v : mapping) {
            if (v.getType() == VariableType.ATTRIBUTE) {
                attributeList.add(v);
                if (v.getBufferIndex().index + 1 > attributeBufferCount) {
                    attributeBufferCount = v.getBufferIndex().index + 1;
                }
            }
        }
        attributes = attributeList.toArray(new VariableMapping[attributeList.size()]);
    }

    /**
     * Create the programs for the shader program implementation.
     * This method must be called before the program is used, or the other methods are called.
     * 
     * @param gles The GLES20 wrapper to use when compiling and linking program.
     * @throws RuntimeException If there is an error reading shader sources or compiling/linking program.
     */
    public void createProgram(GLES20Wrapper gles) {
        if (vertexShaderName == null || fragmentShaderName == null) {
            throw new ShaderProgramException(MUST_SET_FIELDS);
        }
        createProgram(gles, vertexShaderName, fragmentShaderName);
    }

    /**
     * Maps the attributes used based on BufferIndex - attribute variables are sorted based on buffer in the specified
     * result array.
     * Finds the shader attribute variables per buffer using VariableMapping, iterate through defined (by subclasses)
     * attribute variable mapping.
     * Put the result in the result array and set the {@linkplain ShaderVariable} offset based on used attributes.
     * 
     * @param resultArray Array to store shader variables for each attribute buffer in, attributes for buffer 1 will go
     * at index 0.
     */
    private void mapAttributeVariablePerBuffer(ShaderVariable[][] resultArray) {

        HashMap<BufferIndex, ArrayList<ShaderVariable>> svPerBuffer = new HashMap<BufferIndex, ArrayList<ShaderVariable>>();
        for (VariableMapping vm : attributes) {
            ArrayList<ShaderVariable> array = svPerBuffer.get(vm.getBufferIndex());
            if (array == null) {
                array = new ArrayList<ShaderVariable>();
                svPerBuffer.put(vm.getBufferIndex(), array);
            }
            ShaderVariable sv = getShaderVariable(vm);
            if (sv != null) {
                array.add(getShaderVariable(vm));
            }
        }
        for (BufferIndex key : svPerBuffer.keySet()) {
            ArrayList<ShaderVariable> defined = svPerBuffer.get(key);
            if (defined != null) {
                resultArray[key.index] = defined.toArray(new ShaderVariable[defined.size()]);
            }
        }
    }

    private void dynamicMapShaderOffset(ShaderVariable[] variables, VariableType type) {
        int offset = 0;
        int samplerOffset = 0;
        for (ShaderVariable v : variables) {
            if (v != null && v.getType() == type) {
                if (v.getDataType() == GLES20.GL_SAMPLER_2D) {
                    v.setOffset(samplerOffset);
                    samplerOffset += v.getSizeInFloats();
                } else {
                    v.setOffset(offset);
                    offset += v.getSizeInFloats();
                }
            }
        }
    }

    /**
     * Returns the vertex stride for the program, use this when creating a mesh
     * 
     * @return
     */
    public int getVertexStride() {
        return attributesPerVertex[BufferIndex.VERTICES.index];
    }

    /**
     * Returns the number of attributes per vertex
     * TODO - rename this to dynamic, since it fetches the number of attributes per vertex for dynamic buffer
     * 
     * @return
     */
    public int getAttributesPerVertex() {
        if (attributesPerVertex.length > BufferIndex.ATTRIBUTES.index) {
            return attributesPerVertex[BufferIndex.ATTRIBUTES.index];
        } else {
            // No attribute buffer for this program.
            return 0;
        }
    }

    /**
     * Creates the attribute buffers for the specified mesh.
     * 
     * @param mesh
     * @param verticeCount Number of vertices to allocate storage for
     * @return
     */
    public AttributeBuffer[] createAttributeBuffers(Mesh mesh, int verticeCount) {
        AttributeBuffer[] buffers = new AttributeBuffer[attributeBufferCount];
        for (BufferIndex index : BufferIndex.values()) {
            if (index.index >= buffers.length) {
                break;
            }
            buffers[index.index] = createAttributeBuffer(index, verticeCount, mesh);
        }
        return buffers;
    }

    /**
     * Creates the storage for attributes that are not vertices, only creates the storage will not fill buffer.
     * For some subclasses this must also create a backing attribute array in the mesh that is used as
     * intermediate storage before the vertex buffer is updated.
     * 
     * @param index The attribute buffer to create
     * @param verticeCount Number of vertices
     * @param mesh
     * @return The buffer for attribute storage or null if not needed.
     */
    protected AttributeBuffer createAttributeBuffer(BufferIndex index, int verticeCount, Mesh mesh) {
        switch (index) {
        case ATTRIBUTES:
            int attrs = getAttributesPerVertex();
            if (attrs > 0) {
                AttributeBuffer buffer = new AttributeBuffer(verticeCount, attrs, GLES20.GL_FLOAT);
                if (mesh instanceof Consumer) {
                    ((Consumer) mesh).bindAttributeBuffer(buffer);
                }
                return buffer;
            }
            return null;
        case VERTICES:
            return new AttributeBuffer(verticeCount, getVertexStride(), GLES20.GL_FLOAT);
        case ATTRIBUTES_STATIC:
        default:
            throw new IllegalArgumentException("Not implemented");
        }
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
    public void bindAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException {
        for (int i = 0; i < attributeVariables.length; i++) {
            AttributeBuffer buffer = mesh.getVerticeBuffer(i);
            if (buffer != null) {
                gles.glVertexAttribPointer(buffer, GLES20.GL_ARRAY_BUFFER, attributeVariables[i]);
                GLUtils.handleError(gles, "glVertexAttribPointers ");
            }
        }
    }

    /**
     * Checks the status of the attribute name mapping and if enabled the attributes are bound to locations
     * that correspond to location that is appended or prefixed to the name.
     * 
     * @param gles
     * @throws GLException
     */
    protected void bindAttributeNames(GLES20Wrapper gles) throws GLException {
        if (attribNameMapping == null) {
            return;
        }
        for (ShaderVariable attribute : shaderVariables) {
            if (attribute.getType() == VariableType.ATTRIBUTE) {
                int location = 0;
                switch (attribNameMapping) {
                case PREFIX:
                    location = Integer.parseInt(attribute.getName().substring(0, 2));
                    break;
                case APPEND:
                    int length = attribute.getName().length();
                    location = Integer.parseInt(attribute.getName().substring(length - 2, length));
                    break;
                }
                gles.glBindAttribLocation(program, location, attribute.getName());
                GLUtils.handleError(gles, BIND_ATTRIBUTE_ERROR);
                attribute.setLocation(location);
            }
        }

    }

    /**
     * Sets the mode for automatic name mapping of attribute locations.
     * 
     * @see AttribNameMapping
     * @param attribMapping Or null to not automatically bind attributes.
     */
    public void setAttributeNameMapping(AttribNameMapping attribMapping) {
        this.attribNameMapping = attribMapping;
    }

    /**
     * Fetches the program info and stores in this class.
     * This will read active attribute and uniform names, call this after the program has been compiled and linked.
     * 
     * @param gles
     * @throws GLException If attribute or uniform locations could not be found.
     */
    protected void fetchProgramInfo(GLES20Wrapper gles) throws GLException {
        int[] attribInfo = new int[2];
        int[] uniformInfo = new int[2];
        gles.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, attribInfo, ACTIVE_COUNT_OFFSET);
        gles.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, uniformInfo, ACTIVE_COUNT_OFFSET);
        gles.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, attribInfo, MAX_NAME_LENGTH_OFFSET);
        gles.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORM_MAX_LENGTH, uniformInfo, MAX_NAME_LENGTH_OFFSET);
        GLUtils.handleError(gles, GET_PROGRAM_INFO_ERROR);
        shaderVariables = new ShaderVariable[getVariableCount()];
        fetchActiveVariables(gles, VariableType.ATTRIBUTE, attribInfo);
        fetchActiveVariables(gles, VariableType.UNIFORM, uniformInfo);
        attributeVariables = new ShaderVariable[attributeBufferCount][];
        attributesPerVertex = new int[attributeBufferCount];
        mapAttributeVariablePerBuffer(attributeVariables);
        dynamicMapVariables();
        for (int i = 0; i < attributesPerVertex.length; i++) {
            attributesPerVertex[i] = getVariableSize(attributeVariables[i], VariableType.ATTRIBUTE);
        }
    }

    /**
     * Dynamically sets used shader variable offsets, for ATTRIBUTES and UNIFORMS
     * The offset will be tightly packed based on used variable size
     */
    private void dynamicMapVariables() {
        for (ShaderVariable[] sv : attributeVariables) {
            dynamicMapShaderOffset(sv, VariableType.ATTRIBUTE);
        }
        // Map the used uniforms from all used shader variables.
        dynamicMapShaderOffset(shaderVariables, VariableType.UNIFORM);
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
     * @throws GLException If attribute or uniform location(s) are -1, ie they could not be found using the name.
     */
    private void fetchActiveVariables(GLES20Wrapper gles, VariableType type, int[] info) throws GLException {

        int count = info[ACTIVE_COUNT_OFFSET];
        byte[] nameBuffer = new byte[info[MAX_NAME_LENGTH_OFFSET]];
        int[] written = new int[3];

        for (int i = 0; i < count; i++) {
            switch (type) {
            case ATTRIBUTE:
                gles.glGetActiveAttrib(program, i, written, NAME_LENGTH_OFFSET, written,
                        SIZE_OFFSET, written, TYPE_OFFSET, nameBuffer);
                break;
            case UNIFORM:
                gles.glGetActiveUniform(program, i, written, NAME_LENGTH_OFFSET, written,
                        SIZE_OFFSET, written, TYPE_OFFSET, nameBuffer);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + type);
            }
            ShaderVariable variable = new ShaderVariable(type, nameBuffer, written, NAME_LENGTH_OFFSET, SIZE_OFFSET,
                    TYPE_OFFSET);
            setVariableLocation(gles, program, type, variable);
            addShaderVariable(variable);
        }
    }

    /**
     * Fetch the GLES shader variable location (name) and set in the ShaderVariable
     * This is the name (int) value to use to access the variable in GL
     * 
     * @param gles
     * @param program
     * @param type
     * @param variable
     * @throws GLException
     */
    private void setVariableLocation(GLES20Wrapper gles, int program, VariableType type, ShaderVariable variable)
            throws GLException {
        switch (type) {
        case ATTRIBUTE:
            variable.setLocation(gles.glGetAttribLocation(program, variable.getName()));
            break;
        case UNIFORM:
            variable.setLocation(gles.glGetUniformLocation(program, variable.getName()));
            break;
        }
        if (variable.getLocation() < 0) {
            throw new GLException(VARIABLE_LOCATION_ERROR + variable.getName(), 0);
        }
    }

    /**
     * Links the specified vertex and fragment shader to the specified program.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param program
     * @param vertexShader
     * @param fragmentShader
     * @throws GLException If the program could not be linked with the shaders.
     */
    public void linkProgram(GLES20Wrapper gles, int program, int vertexShader, int fragmentShader) throws GLException {
        gles.glAttachShader(program, vertexShader);
        gles.glAttachShader(program, fragmentShader);
        gles.glLinkProgram(program);
        System.out.println(gles.glGetProgramInfoLog(program));
        GLUtils.handleError(gles, LINK_PROGRAM_ERROR);
    }

    /**
     * Compiles the shader from the specified inputstream, the inputstream is not closed after reading.
     * It is up to the caller to close the stream.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param shaderStream Inputstream to the shader source.
     * @param shader OpenGL object to compile the shader to.
     * @param sourceName Name of the sourcefile - this is used for error reporting.
     * @throws IOException If there is an error reading from stream
     * @throws GLException If there is an error setting or compiling shader source.
     */
    public void compileShader(GLES20Wrapper gles, InputStream shaderStream, int shader, String sourceName)
            throws IOException, GLException {
        String shaderStr = new String(StreamUtils.readFromStream(shaderStream));
        gles.glShaderSource(shader, shaderStr);
        GLUtils.handleError(gles, SHADER_SOURCE_ERROR + sourceName);
        gles.glCompileShader(shader);
        GLUtils.handleError(gles, COMPILE_SHADER_ERROR + sourceName);
        checkCompileStatus(gles, shader, sourceName);
    }

    /**
     * Checks the compile status of the specified shader program - if shader is not sucessfully compiled an exception
     * is thrown.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param shader
     * @param sourceName Name of shader source
     * @throws GLException
     */
    public void checkCompileStatus(GLES20Wrapper gles, int shader, String sourceName) throws GLException {
        IntBuffer compileStatus = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        gles.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus);
        if (compileStatus.get(0) != GLES20.GL_TRUE) {
            throw new GLException(COMPILE_SHADER_ERROR + sourceName + " : " + compileStatus.get(0) + "\n"
                    + gles.glGetShaderInfoLog(shader),
                    GLES20.GL_FALSE);
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
     * Returns the shader variable for the specified index, use this to map attributes to variables.
     * 
     * @param attribute
     * @return
     * @throws IllegalArgumentException If shader variables are null, the program has probably not been created.
     */
    public ShaderVariable getShaderVariable(VariableMapping attribute) {
        if (shaderVariables == null) {
            throw new IllegalArgumentException(NULL_VARIABLES_ERROR);
        }
        return shaderVariables[attribute.getIndex()];
    }

    /**
     * Utility method to return the name of a shader variable, this will remove unwanted characters such as array
     * declaration or '.' field access eg 'struct.field' will become 'struct'
     * 
     * @param variable
     * @return Name of variable, without array declaration.
     */
    public String getVariableName(ShaderVariable variable) {
        String name = variable.getName();
        if (name.endsWith("]")) {
            int end = name.indexOf("[");
            name = name.substring(0, end);
        }
        int dot = name.indexOf(".");
        if (dot == -1) {
            return name;
        }
        if (dot == 0) {
            return name.substring(1);
        }
        return name.substring(0, dot);
    }

    /**
     * Stores the shader variable in this program, if variable is of unmapped type, for instance Sampler, then it is
     * skipped. Also skip variables that are defined in code but not used in shader.
     * 
     * @param variable
     * @throws IllegalArgumentException If shader variables are null, the program has probably not been created,
     * or if a variable has no mapping in the code.
     */
    protected void addShaderVariable(ShaderVariable variable) {
        if (shaderVariables == null) {
            throw new IllegalArgumentException(NULL_VARIABLES_ERROR);
        }
        // If variable type is is unMappedTypes then skip, for instance texture
        if (unMappedTypes.contains(variable.getDataType())) {
            return;
        }
        try {
            VariableMapping vm = getVariableMapping(variable);
            // TODO Offset is set dynamically when dynamicMapShaderOffset() is called - create a setting so that
            // it is possible to toggle between the two modes.
            // variable.setOffset(vm.getOffset());
            shaderVariables[vm.getIndex()] = variable;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Variable has no mapping to shader variable (ie used in shader but not defined in program " + getClass().getSimpleName() + ") : "
                            + variable.getName());
        }
    }

    /**
     * Utility method to create the vertex and shader program using the specified shader names.
     * The shaders will be loaded, compiled and linked.
     * Utility method to automatically load, compile and link the specified vertex and fragment shaders.
     * Vertex shader, fragment shader and program objects will be created.
     * If program compiles succesfully then the program info is fetched.
     * 
     * @param gles
     * @param vertexName Name of vertex shader to load, compile and link
     * @param fragmentName Name of fragment shader to load, compile and link
     */
    protected void createProgram(GLES20Wrapper gles, String vertexName, String fragmentName) {
        SimpleLogger.d(getClass(), "Creating program for: " + vertexName + " and " + fragmentName);
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream vertexStream = null;
        InputStream fragmentStream = null;
        try {
            vertexStream = classLoader.getResourceAsStream(vertexName);
            fragmentStream = classLoader.getResourceAsStream(fragmentName);
            vertexShader = gles.glCreateShader(GLES20.GL_VERTEX_SHADER);
            if (vertexShader == 0) {
                // Only need to check first source for 0. At least GL has current context.
                throw new GLException(CREATE_SHADER_ERROR, GLES20.GL_NO_ERROR);
            }
            fragmentShader = gles.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            program = gles.glCreateProgram();
            SimpleLogger.d(getClass(),
                    "Program name: " + program + ", vertex: " + vertexShader + " fragment: " + fragmentShader);
            compileShader(gles, vertexStream, vertexShader, vertexName);
            compileShader(gles, fragmentStream, fragmentShader, fragmentName);
            linkProgram(gles, program, vertexShader, fragmentShader);
            fetchProgramInfo(gles);
            bindAttributeNames(gles);
            createUniformStorage(shaderVariables);
        } catch (IOException e) {
            if (vertexStream == null) {
                throw new RuntimeException("Could not load " + vertexName);
            } else {
                throw new RuntimeException("Could not load " + fragmentName);
            }
        } catch (GLException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Sets one of more float uniforms for the specified variable, supports VEC2, VEC3, VEC4 and MAT2, MAT3, MAT4 types
     * 
     * @param gles
     * @param variable Shader variable to set uniform data for, datatype and size is read.
     * @param offset Offset into uniform array where data starts.
     * @throws GLException If there is an error setting a uniform to GL
     */
    protected final void setUniform(GLES20Wrapper gles, ShaderVariable variable, int offset)
            throws GLException {

        switch (variable.getDataType()) {
        case GLES20.GL_FLOAT_VEC2:
                gles.glUniform2fv(variable.getLocation(), variable.getSize(), uniforms, offset);
            break;
        case GLES20.GL_FLOAT_VEC3:
                gles.glUniform3fv(variable.getLocation(), variable.getSize(), uniforms, offset);
            break;
        case GLES20.GL_FLOAT_VEC4:
                gles.glUniform4fv(variable.getLocation(), variable.getSize(), uniforms, offset);
            break;
        case GLES20.GL_FLOAT_MAT2:
                gles.glUniformMatrix2fv(variable.getLocation(), variable.getSize(), false, uniforms, offset);
            break;
        case GLES20.GL_FLOAT_MAT3:
                gles.glUniformMatrix3fv(variable.getLocation(), variable.getSize(), false, uniforms, offset);
            break;
        case GLES20.GL_FLOAT_MAT4:
                gles.glUniformMatrix4fv(variable.getLocation(), variable.getSize(), false, uniforms, offset);
            break;
        case GLES20.GL_SAMPLER_2D:
                gles.glUniform1iv(variable.getLocation(), variable.getSize(), samplers, offset);
                break;
        default:
                throw new IllegalArgumentException("Not implemented for dataType: " + variable.getDataType());
        }
        GLUtils.handleError(gles, "setVectorUniform(), dataType: " + variable.getDataType());

    }

    /**
     * Internal method, creates the array storage for uniform matrix and vector variables.
     * This will create the float array storage in the mesh, indexing must be done by the apropriate program
     * when uniform variables are set before rendering.
     * 
     * @param variables Shader variables, attribute variables are ignored
     */
    protected void createUniformStorage(ShaderVariable[] variables) {

        int uniformSize = 0;
        int samplerSize = 0;
        if (variables != null) {
            samplerSize = getSamplerSize(variables);
            uniformSize = getVariableSize(variables, VariableType.UNIFORM);
            if (uniformSize > 0) {
                createUniforms(new float[uniformSize], new int[samplerSize]);
            } else {
                SimpleLogger.d(getClass(), "No uniforms used");
            }
        } else {
            throw new IllegalArgumentException("Shader variables is null, forgot to call createProgram()?");
        }
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
     * @return Total size, in floats, of all defined shader variables of the specified type
     */
    protected int getVariableSize(ShaderVariable[] variables, VariableType type) {
        int size = 0;
        for (ShaderVariable v : variables) {
            if (v != null && v.getType() == type && v.getDataType() != GLES20.GL_SAMPLER_2D) {
                size += v.getSizeInFloats();
            }
        }
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
            if (v != null && v.getType() == VariableType.UNIFORM && v.getDataType() == GLES20.GL_SAMPLER_2D) {
                size += v.getSizeInFloats();
            }
        }
        return size;

    }

    /**
     * Returns the size of shader variable that are mapped to the specified buffer index.
     * Use this to fetch the size per attribute for the different buffers.
     * 
     * @param variables
     * @param index
     * @return
     */
    protected int getVariableSize(ShaderVariable[] variables, BufferIndex index) {
        int size = 0;
        for (ShaderVariable v : variables) {
            if (v != null) {
                VariableMapping vm = getVariableMapping(v);
                if (vm.getBufferIndex() == index) {
                    size += v.getSizeInFloats();
                }
            }
        }
        return size;
    }

    /**
     * Sets the data for the uniforms needed by the program - the default implementation will set the modelview and
     * projection matrices.
     * 
     * @param gles
     * @param modelviewMatrix The matrix to use for the modelview transform
     * @param projectionMatrix The projection matrix
     * @param mesh
     */
    public void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, float[] projectionMatrix, Mesh mesh)
            throws GLException {
        // Refresh the uniform matrixes
        System.arraycopy(modelviewMatrix, 0, uniforms,
                shaderVariables[ShaderVariables.uMVMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
        System.arraycopy(projectionMatrix, 0, uniforms,
                shaderVariables[ShaderVariables.uProjectionMatrix.index].getOffset(),
                Matrix.MATRIX_ELEMENTS);
    }
    
    
    /**
     * Internal method, sets the uniform data from the uniform data into the mapping provided by the attribute mapping.
     * 
     * @param gles
     * @param uniformMapping Variable mapping for the uniform data
     * @throws GLException
     */
    protected void setUniforms(GLES20Wrapper gles, VariableMapping[] uniformMapping)
            throws GLException {
        for (VariableMapping am : uniformMapping) {
            ShaderVariable v = getShaderVariable(am);
            // If null then declared in program but not used, silently ignore
            if (v != null) {
                setUniform(gles, v, v.getOffset());
            }
        }
    }

    /**
     * Sets UV fraction for the tiled texture + number of frames in x.
     * Use this for programs that use tiled texture behavior.
     * 
     * @param texture
     * @param destination Will store 1 / tilewidth, 1 / tilewidth, tilewidth, beginning at offset
     * @param variable The shader variable
     * @param offset Offset into destination where fraction is set
     */
    protected void setTextureUniforms(TiledTexture2D texture, float[] destination, ShaderVariable variable,
            int offset) {
        if (texture.getWidth() == 0 || texture.getHeight() == 0) {
            SimpleLogger.d(getClass(), "ERROR! Texture size is 0: " + texture.getWidth() + ", " + texture.getHeight());
        }
        offset += variable.getOffset();
        destination[offset++] = (((float) texture.getWidth()) / texture.getTileWidth()) / (texture.getWidth());
        destination[offset++] = (((float) texture.getHeight()) / texture.getTileHeight()) / (texture.getHeight());
        destination[offset++] = texture.getTileWidth();
    }

    /**
     * Sets the screensize to uniform storage
     * 
     * @param uniforms
     * @param uniformScreenSize
     */
    protected void setScreenSize(float[] uniforms, ShaderVariable uniformScreenSize) {
        if (uniformScreenSize != null) {
            int screenSizeOffset = uniformScreenSize.getOffset();
            uniforms[screenSizeOffset++] = Window.getInstance().getWidth();
            uniforms[screenSizeOffset++] = Window.getInstance().getHeight();
        }
    }

    /**
     * Sets the ambient light color in uniform data
     * 
     * @param uniforms
     * @param uniformAmbient
     * @param material
     */
    protected void setAmbient(float[] uniforms, ShaderVariable uniformAmbient, float[] ambient) {
        int offset = uniformAmbient.getOffset();
        uniforms[offset++] = ambient[0];
        uniforms[offset++] = ambient[1];
        uniforms[offset++] = ambient[2];
        uniforms[offset++] = ambient[3];
    }

    /**
     * Returns the key value for this shader program, this is the classname and possible name of shader used.
     * This method is used by {@link AssetManager} when programs are compiled and stored.
     * 
     * @return Key value for this shader program.
     */
    public String getKey() {
        return getClass().getCanonicalName() + (shading != null ? shading.name() : "");
    }

    /**
     * Returns the shading that this program supports
     * 
     * @return
     */
    public Shading getShading() {
        return shading;
    }

    /**
     * Returns one or more defined uniform vectors used when rendering.
     * 
     * @return One or more uniform vector as used by the shader program implementation
     */
    public float[] getUniforms() {
        return uniforms;
    }

    /**
     * Sets a reference to an array with float values that can be used by when rendering this Mesh.
     * Note that the use of uniforms is depending on the shader program used.
     * 
     * @param uniforms Values to reference in this class, note that values are NOT copied.
     * @param samplers Sampler (texture unit) values
     * 
     */
    private void createUniforms(float[] uniforms, int[] samplers) {
        this.uniforms = uniforms;
        this.samplers = samplers;
    }
    
}
