package com.nucleus.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.io.StreamUtils;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.TiledTexture2D;

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

        APPEND(0),
        PREFIX(1);
        final private int mode;

        private AttribNameMapping(int mode) {
            this.mode = mode;
        }

    }

    public final static String SHADER_SOURCE_ERROR = "Error setting shader source: ";
    public final static String COMPILE_SHADER_ERROR = "Error compiling shader: ";
    public final static String CREATE_SHADER_ERROR = "Error creating shader object, shader is null";
    public final static String LINK_PROGRAM_ERROR = "Error linking program: ";
    public final static String BIND_ATTRIBUTE_ERROR = "Error binding attribute: ";
    public final static String VARIABLE_LOCATION_ERROR = "Could not get shader variable location: ";
    public final static String NULL_VARIABLES_ERROR = "ShaderVariables are null, program not created? Must call fetchProgramInfo()";

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

    protected ShaderVariable[] shaderVariables;

    private AttribNameMapping attribNameMapping = null;

    /**
     * Unmapped variable types, by default Sampler2D is added to avoid having to add Sampler2D variables.
     * Sampler is set by activating textures and uploading textures not by attribute or uniform.
     */
    protected List<Integer> unMappedTypes = new ArrayList<>();

    /**
     * Get the index to the shader variable, as defined in the implementing ShaderProgram class.
     * 
     * @param variable
     */
    public abstract int getVariableIndex(ShaderVariable variable);

    /**
     * Set the attribute pointer(s) using the data in the vertexbuffer, this shall make the necessary calls to
     * set the pointers for used attributes, enable pointers as needed.
     * This will make the actual connection between the attribute data in the vertex buffer and the shader.
     * 
     * TODO Move this method to NucleusRenderer, or similar class that has knowledge of GLES implementation, DO NOT
     * spread GLES20 wrapper across the implementation, doing so will make it very hard to update to newer versions of
     * GLES
     * 
     * @param gles
     * @param mesh
     */
    public abstract void bindAttributes(GLES20Wrapper gles, Mesh mesh) throws GLException;

    /**
     * Sets the uniforms needed by the program, this will make the binding between the shader and uniforms
     * 
     * TODO Move this method to NucleusRenderer, or similar class that has knowledge of GLES implementation, DO NOT
     * spread GLES20 wrapper across the implementation, doing so will make it very hard to update to newer versions of
     * GLES
     * 
     * @param gles
     * @param modelviewMatrix The matrix to use for the MVP matrix
     * @param mesh
     */
    public abstract void bindUniforms(GLES20Wrapper gles, float[] modelviewMatrix, Mesh mesh) throws GLException;

    /**
     * Returns the number of defined attribute + uniform variables in the program.
     * This is to make it easier when developing so that temporarily unused variabled do not need to be removed.
     * 
     * @return Number of defined variables in the shader program, all variables do not need to be used.
     */
    public abstract int getVariableCount();

    /**
     * Creates a new ShaderProgram
     */
    public ShaderProgram() {
        super();
        unMappedTypes.add(GLES20.GL_SAMPLER_2D);
    }

    /**
     * Create the programs for the shader program implementation.
     * This method must be called before the program is used, or the other methods are called.
     * 
     * @param gles The GLES20 wrapper to use when compiling and linking program.
     * @throws RuntimeException If there is an error reading shader sources or compiling/linking program.
     */
    public abstract void createProgram(GLES20Wrapper gles);

    /**
     * Utility method to automatically load, compile and link the specified vertex and fragment shaders.
     * Vertex shader, fragment shader and program objects will be created.
     * If program compiles succesfully then the program info is fetched.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param vertexShader
     * @param fragmentShader
     * @return The program object to use the compiled program.
     * @throws IOException If there is an error reading from stream
     * @throws GLException if there is an error setting or compiling shader sources or linking the program.
     */
    protected int createProgram(GLES20Wrapper gles, InputStream vertexStream, InputStream fragmentStream)
            throws IOException, GLException {

        vertexShader = gles.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (vertexShader == 0) {
            // Only need to check first source for 0. At least GL has current context.
            throw new GLException(CREATE_SHADER_ERROR, GLES20.GL_NO_ERROR);
        }
        fragmentShader = gles.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        program = gles.glCreateProgram();
        compileShader(gles, vertexStream, vertexShader);
        compileShader(gles, fragmentStream, fragmentShader);
        linkProgram(gles, program, vertexShader, fragmentShader);
        fetchProgramInfo(gles);
        bindAttributeNames(gles);
        return program;
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
     * This will read active attribute and uniform names
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
        shaderVariables = new ShaderVariable[getVariableCount()];
        fetchActiveVariables(gles, VariableType.ATTRIBUTE, attribInfo, shaderVariables);
        fetchActiveVariables(gles, VariableType.UNIFORM, uniformInfo, shaderVariables);
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
     * @param result The destination array
     * @throws GLException If attribute or uniform location(s) are -1, ie they could not be found using the name.
     */
    private void fetchActiveVariables(GLES20Wrapper gles, VariableType type, int[] info,
            ShaderVariable[] result) throws GLException {

        int count = info[ACTIVE_COUNT_OFFSET];
        // ShaderVariable[] variables = new ShaderVariable[info[ACTIVE_COUNT_OFFSET]];
        byte[] nameBuffer = new byte[info[MAX_NAME_LENGTH_OFFSET]];
        int[] written = new int[3];

        for (int i = 0; i < count; i++) {
            switch (type) {
            case ATTRIBUTE:
                gles.glGetActiveAttrib(program, i, nameBuffer.length, written, NAME_LENGTH_OFFSET, written,
                        SIZE_OFFSET, written, TYPE_OFFSET, nameBuffer, 0);
                break;
            case UNIFORM:
                gles.glGetActiveUniform(program, i, nameBuffer.length, written, NAME_LENGTH_OFFSET, written,
                        SIZE_OFFSET, written, TYPE_OFFSET, nameBuffer, 0);
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
     * Fetch the GLES shader variable location and set in the ShaderVariable
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
     * @throws IOException If there is an error reading from stream
     * @throws GLException If there is an error setting or compiling shader source.
     */
    public void compileShader(GLES20Wrapper gles, InputStream shaderStream, int shader) throws IOException, GLException {
        String shaderStr = new String(StreamUtils.readFromStream(shaderStream));
        gles.glShaderSource(shader, shaderStr);
        GLUtils.handleError(gles, SHADER_SOURCE_ERROR);
        gles.glCompileShader(shader);
        GLUtils.handleError(gles, COMPILE_SHADER_ERROR);
        checkCompileStatus(gles, shader);
    }

    /**
     * Checks the compile status of the specified shader program - if shader is not sucessfully compiled an exception
     * is thrown.
     * 
     * @param gles GLES20 platform specific wrapper.
     * @param shader
     * @throws GLException
     */
    public void checkCompileStatus(GLES20Wrapper gles, int shader) throws GLException {
        IntBuffer compileStatus = IntBuffer.allocate(1);
        gles.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus);
        if (compileStatus.get(0) != GLES20.GL_TRUE) {
            throw new GLException(COMPILE_SHADER_ERROR + "\n" + gles.glGetShaderInfoLog(shader), GLES20.GL_FALSE);
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
     * Returns the shader variable for the specified index, use this to map indexes to variables.
     * 
     * @param index
     * @return
     * @throws IllegalArgumentException If shader variables are null, the program has probably not been created.
     */
    public ShaderVariable getShaderVariable(int index) {
        if (shaderVariables == null) {
            throw new IllegalArgumentException(NULL_VARIABLES_ERROR);
        }
        return shaderVariables[index];
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
     * @throws IllegalArgumentException If shader variables are null, the program has probably not been created.
     */
    protected void addShaderVariable(ShaderVariable variable) {
        if (shaderVariables == null) {
            throw new IllegalArgumentException(NULL_VARIABLES_ERROR);
        }
        // If variable type is is unMappedTypes then skip.
        if (unMappedTypes.contains(variable.getDataType())) {
            return;
        }
        // Check for unused variables, index will be out of range if names are defined in program but not used
        int index = getVariableIndex(variable);
        if (index < shaderVariables.length) {
            shaderVariables[index] = variable;
        }
    }

    /**
     * Internal utility method to set vertex attribute pointers from one or more arrays.
     * 
     * @param gles
     * @param mesh
     * @param variables
     */
    protected void setAttributePointers(GLES20Wrapper gles, Mesh mesh, ShaderVariable[] variables) throws GLException {

        VertexBuffer buffer;
        for (ShaderVariable v : variables) {
            buffer = mesh.getVerticeBuffer(BufferIndex.VERTICES);
            gles.glEnableVertexAttribArray(v.getLocation());
            GLUtils.handleError(gles, "glEnableVertexAttribArray ");
            gles.glVertexAttribPointer(v.getLocation(), buffer.getComponentCount(), buffer.getDataType(), false,
                    buffer.getByteStride(), buffer.getBuffer());

        }
    }

    /**
     * Utility method to create the vertex and shader program using the specified shader names.
     * The shaders will be loaded, compiled and linked.
     * 
     * @param gles
     * @param vertexShader Name of vertex shader to load, compile and link
     * @param fragmentShader Name of fragment shader to load, compile and link
     */
    protected void createProgram(GLES20Wrapper gles, String vertexShader, String fragmentShader) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            createProgram(gles,
                    classLoader.getResourceAsStream(vertexShader),
                    classLoader.getResourceAsStream(fragmentShader));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GLException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Sets one of more vector float uniforms for the specified variable, supports VEC2, VEC3 and VEC4 types
     * 
     * @param gles
     * @param variable Shader variable to set uniform data for, datatype and size is read.
     * @param uniform One ore more float vectors to set.
     * @param offset Offset into uniform array where data starts.
     */
    protected final void setVectorUniform(GLES20Wrapper gles, ShaderVariable variable, float[] uniform, int offset) {

        switch (variable.getDataType()) {
        case GLES20.GL_FLOAT_VEC2:
            gles.glUniform2fv(variable.getLocation(), variable.getSize(), uniform, offset);
            break;
        case GLES20.GL_FLOAT_VEC3:
            gles.glUniform3fv(variable.getLocation(), variable.getSize(), uniform, offset);
            break;
        case GLES20.GL_FLOAT_VEC4:
            gles.glUniform4fv(variable.getLocation(), variable.getSize(), uniform, offset);
            break;
        }

    }

    /**
     * Internal method to create the arrays needed for uniform vector and matrix storage.
     * The arrays will be set into the mesh.
     * 
     * @param mesh The mesh to set vector and matrix uniform data arrays into.
     * @param vectors Shader variable for the uniform vectors
     * @param matrices Shader variable for the uniform matrices
     */
    protected void setUniformArrays(Mesh mesh, ShaderVariable vectors, ShaderVariable matrices) {

        float[] uniformVectors = new float[vectors.getSizeInFloats()];
        mesh.setUniformVectors(uniformVectors);
        float[] uniformMatrices = new float[matrices.getSizeInFloats()];
        mesh.setUniformMatrices(uniformMatrices);
    }

    /**
     * Sets UV fraction for the tiled texture + number of frames in x.
     * Use this for programs that use tiled texture behavior.
     * 
     * @param texture
     * @param destination Will store 1 / framesx, 1 / framesy, framesex, beginning at offset
     * @param offset
     */
    protected void setTextureUniforms(TiledTexture2D texture, float[] destination, int offset) {
        destination[offset++] = 1f / texture.getFramesX();
        destination[offset++] = 1f / texture.getFramesY();
        destination[offset++] = texture.getFramesX();
    }

}
