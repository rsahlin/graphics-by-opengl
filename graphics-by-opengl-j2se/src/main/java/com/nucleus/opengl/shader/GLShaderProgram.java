package com.nucleus.opengl.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nucleus.SimpleLogger;
import com.nucleus.common.BufferUtils;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.GLCompilerException;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES30Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.Window;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.DefaultGraphicsShader;
import com.nucleus.shader.FloatBlockBuffer;
import com.nucleus.shader.GenericShaderProgram;
import com.nucleus.shader.GraphicsShader;
import com.nucleus.shader.Shader;
import com.nucleus.shader.ShaderSource;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableIndexer;
import com.nucleus.texturing.TiledTexture2D;

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
 *
 */
public abstract class GLShaderProgram extends DefaultGraphicsShader implements Shader {

    protected final static String NO_ACTIVE_UNIFORMS = "No active uniforms, forgot to call createProgram()?";

    protected GenericShaderProgram shadowPass1;
    protected GenericShaderProgram shadowPass2;

    /**
     * Unmapped variable types
     */
    protected List<Integer> unMappedTypes = new ArrayList<>();

    protected GLShaderProgram(Pass pass, Shading shading, String category,
            GraphicsShader.ProgramType shaders) {
        super(pass, shading, category, shaders);
    }

    protected GLShaderProgram(Categorizer function, GraphicsShader.ProgramType shaders) {
        super(function, shaders);
    }

    /**
     * Returns the indexer used when creating this program, or null if not set.
     * 
     * @return
     */
    @Override
    public VariableIndexer getIndexer() {
        return variableIndexer;
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

    @Override
    protected NamedShaderVariable getVariableByName(String name, NamedShaderVariable[] variables) {
        for (NamedShaderVariable v : variables) {
            if (v != null && v.getName().contentEquals(name)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Logs the shader source for the specified shader, using numbered lines.
     * 
     * @param gles
     * @param shader
     */
    protected void logNumberedShaderSource(GLES20Wrapper gles, int shader) {
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
     * Returns the shading that this program supports
     * 
     * @return
     */
    public Shading getShading() {
        return function.getShading();
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

    @Override
    public String toString() {
        return shaders + " : " + function.toString();
    }

    /**
     * Returns the defines to insert into the shader source
     * 
     * @param type
     * @return
     */
    protected String getDefines(ShaderType type) {
        return null;
    }

}
