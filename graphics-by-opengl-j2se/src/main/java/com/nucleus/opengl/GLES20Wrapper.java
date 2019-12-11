package com.nucleus.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.common.BufferUtils;
import com.nucleus.common.FileUtils;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.io.StreamUtils;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.NamedShaderVariable;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Accessor.ComponentType;
import com.nucleus.scene.gltf.Accessor.Type;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Sampler;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.shader.ShaderSource;
import com.nucleus.shader.ShaderSource.SLVersion;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.ParameterData;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Parameter;

/**
 * Abstraction for OpenGL GLES 2.X, this is used for platform independent usage of GLES functions.
 * Use this to make it possible to develop OpenGL (ES) software regardless of target platform GL bindings.
 * This can be used to make it easier to test the code on J2SE platforms (without native GLES bindings) as well as multi
 * platform development.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class GLES20Wrapper extends GLESWrapper {

    protected boolean[] enabledVertexArrays = new boolean[16];
    protected IntBuffer shaderSourceLength = BufferUtils.createIntBuffer(1);

    /**
     * Implementation constructor - DO NOT USE!!!
     * TODO - protect/hide this constructor
     * 
     * @param platform
     * @param renderVersion If higher than GLES20, otherwise null
     */
    protected GLES20Wrapper(Platform platform, Renderers renderVersion) {
        super(platform, renderVersion == null ? Renderers.GLES20 : renderVersion);
    }

    @Override
    public ProgramInfo getProgramInfo(int program) throws GLException {
        int[] activeInfo = new int[2];
        int[] nameLength = new int[2];
        glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, activeInfo, VariableType.ATTRIBUTE.index);
        glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, activeInfo, VariableType.UNIFORM.index);
        glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, nameLength, VariableType.ATTRIBUTE.index);
        glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORM_MAX_LENGTH, nameLength, VariableType.UNIFORM.index);
        GLUtils.handleError(this, "glGetProgramiv");
        return new ProgramInfo(program, activeInfo, nameLength);
    }

    @Override
    public void createInfo() {
        if (rendererInfo == null) {
            rendererInfo = new GLRendererInfo(this, renderVersion);
        }
    }

    @Override
    public InterfaceBlock[] getUniformBlocks(ProgramInfo info) throws GLException {
        // Uniform blocks not supported on GLES2 - return null
        return null;
    }

    @Override
    public NamedShaderVariable getActiveVariable(int program, VariableType type, int index, byte[] nameBuffer)
            throws GLException {
        // DO NOT CREATE ARRAY LARGER THAN THIS - otherwise created uniform will indicate it belongs to a block.
        int[] written = new int[NamedShaderVariable.DATA_OFFSET];
        switch (type) {
            case ATTRIBUTE:
                glGetActiveAttrib(program, index, written, NamedShaderVariable.NAME_LENGTH_OFFSET, written,
                        NamedShaderVariable.SIZE_OFFSET, written, NamedShaderVariable.TYPE_OFFSET, nameBuffer);
                break;
            case UNIFORM:
                glGetActiveUniform(program, index, written, NamedShaderVariable.NAME_LENGTH_OFFSET, written,
                        NamedShaderVariable.SIZE_OFFSET, written, NamedShaderVariable.TYPE_OFFSET, nameBuffer);
                break;
            default:
                throw new IllegalArgumentException("Invalid varible type: " + type);

        }
        GLUtils.handleError(this, "glGetActive : " + type);
        // Create shader variable using name excluding [] and .
        return new NamedShaderVariable(type,
                getVariableName(nameBuffer, written[NamedShaderVariable.NAME_LENGTH_OFFSET]), index,
                written, 0);
    }

    /**
     * Abstraction for glFrameBufferTexture2D
     */
    public abstract void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level);

    /**
     * Abstraction for glGenFrameBuffers
     * 
     * @param buffers
     */
    public abstract void glGenFramebuffers(int[] buffers);

    /**
     * Abstraction for glBindFramebuffer
     */
    public abstract void glBindFramebuffer(int target, int framebuffer);

    /**
     * Abstraction for glAttachShader()
     * 
     * @param program
     * @param shader
     */
    public abstract void glAttachShader(int program, int shader);

    /**
     * Abstraction for glLinkProgram()
     * 
     * @param program
     */
    public abstract void glLinkProgram(int program);

    /**
     * Abstraction for glShaderSource()
     * 
     * @param shader
     * @param shaderSource
     */
    public abstract void glShaderSource(int shader, String shaderSource);

    /**
     * Abstraction for glCompileShader()
     * 
     * @param shader
     */
    public abstract void glCompileShader(int shader);

    /**
     * Abstraction for glValidateProgram()
     * 
     * @param program
     */
    public abstract void glValidateProgram(int program);

    /**
     * Abstraction for glCreateShader()
     * 
     * @param type
     * @return
     */
    public abstract int glCreateShader(int type);

    /**
     * Abstraction for glCreateProgram()
     * 
     * @return
     */
    public abstract int glCreateProgram();

    /**
     * Abstraction for glDeleteProgram()
     * 
     * @param program
     */
    public abstract void glDeleteProgram(int program);

    /**
     * Abstraction for glGenBuffers()
     * 
     * @param buffers Storage for buffer names
     */
    public abstract void glGenBuffers(int[] buffers);

    /**
     * Abstraction for glDeleteBuffers()
     * 
     * @param n
     * @param buffers
     * @param offset
     */
    public abstract void glDeleteBuffers(int n, int[] buffers, int offset);

    /**
     * Abstraction for glBindBuffer()
     * 
     * @param target
     * @param buffer
     */
    public abstract void glBindBuffer(int target, int buffer);

    /**
     * Abstraction for glBufferData()
     * 
     * @param target
     * @param size
     * @param data
     * @param usage
     */
    public abstract void glBufferData(int target, int size, Buffer data, int usage);

    /**
     * Abstraction for glGetShaderiv()
     * 
     * @param shader
     * @param pname
     * @param params
     */
    public abstract void glGetShaderiv(int shader, int pname, IntBuffer params);

    /**
     * Abstraction for glUseProgram()
     * 
     * @param program
     */
    public abstract void glUseProgram(int program);

    /**
     * Abstraction for glGetProgramiv()
     * 
     * @param program
     * @param pname
     * @param params
     * @param offset
     */
    public abstract void glGetProgramiv(int program, int pname, int[] params, int offset);

    /**
     * Abstraction for glGetActiveAttribute()
     * 
     * @param program
     * @param index
     * @param length Result buffer for length
     * @param lengthOffset
     * @param size
     * @param sizeOffset
     * @param type
     * @param typeOffset
     * @param name
     */
    public abstract void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset,
            int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name);

    /**
     * Abstraction for glGetActiveUniform()
     * 
     * @param program
     * @param index
     * @param length Destination for length
     * @param lengthOffset
     * @param size
     * @param sizeOffset
     * @param type
     * @param typeOffset
     * @param name
     */
    public abstract void glGetActiveUniform(int program, int index, int[] length, int lengthOffset,
            int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name);

    /**
     * Abstraction for glGetUniformLocation()
     * 
     * @param program
     * @param name
     * @return
     */
    public abstract int glGetUniformLocation(int program, String name);

    /**
     * Abstraction for glGetAttribLocation
     * 
     * @param program
     * @param name
     * @return
     */
    public abstract int glGetAttribLocation(int program, String name);

    /**
     * Abstraction for glGetError()
     * 
     * @return
     */
    public abstract int glGetError();

    /**
     * Abstraction for glVertexAttribPointer()
     * 
     * @param index
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @param ptr
     */
    public abstract void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            ByteBuffer ptr);

    /**
     * Abstraction for glVertexAttribPointer()
     * 
     * @param index
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @param ptr
     */
    public abstract void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            FloatBuffer ptr);

    /**
     * Abstraction for glVertexAttribPointer()
     * 
     * @param index
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @param offset
     */
    public abstract void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
            int offset);

    /**
     * Sets the vertex attrib pointers for the specified buffer, this call will set all attribute pointers
     * for the specified buffer.
     * If buffer has named object allocated then VBO is used, otherwise glVertexAttribPointer is called
     * with the java.nio.Buffer.
     * 
     * Call {@link #disableAttribPointers()} after drawArrays/elements is called
     * 
     * @param buffer
     * @param target
     * @param attribs Array of attributes to set
     */
    public void glVertexAttribPointer(AttributeBuffer buffer, int target, ShaderVariable[] attribs) {
        int location = 0;
        if (buffer.getBufferName() > 0) {
            glBindBuffer(target, buffer.getBufferName());
            if (buffer.isDirty()) {
                glBufferData(target, buffer.getSizeInBytes(), buffer.getBuffer().position(0),
                        GLES20.GL_STATIC_DRAW);
                buffer.setDirty(false);
            }
            for (ShaderVariable a : attribs) {
                if (a != null) {
                    location = a.getLocation();
                    if (!enabledVertexArrays[location]) {
                        glEnableVertexAttribArray(location);
                        enabledVertexArrays[location] = true;
                    }
                    glVertexAttribPointer(location, a.getComponentCount(), GLES20.GL_FLOAT, false,
                            buffer.getByteStride(), a.getOffset() * 4);
                }
            }
        } else {
            for (ShaderVariable a : attribs) {
                if (a != null) {
                    FloatBuffer fb = buffer.getBuffer();
                    fb.position(a.getOffset());
                    location = a.getLocation();
                    if (!enabledVertexArrays[location]) {
                        glEnableVertexAttribArray(location);
                        enabledVertexArrays[location] = true;
                    }
                    glVertexAttribPointer(location, a.getComponentCount(), GLES20.GL_FLOAT, false,
                            buffer.getByteStride(), fb);
                }
            }
        }
    }

    /**
     * Disables attrib pointers after calls to set vertex attrib pointers.
     * TODO - keep track of needed and already enabled vertex arrays in
     * {@link #glVertexAttribPointer(AttributeBuffer, int, ShaderVariable[])}
     * and
     * {@link #glVertexAttribPointer(Accessor, ShaderVariable)}
     * 
     */
    public void disableAttribPointers() {
        for (int i = 0; i < enabledVertexArrays.length; i++) {
            if (enabledVertexArrays[i]) {
                glDisableVertexAttribArray(i);
                enabledVertexArrays[i] = false;
            }
        }
    }

    /**
     * Binds an accessor to a shader variable
     * 
     * @param accessor
     * @param attribute
     * @throws GLException
     */
    public void glVertexAttribPointer(Accessor accessor, ShaderVariable attribute)
            throws GLException {
        int location = attribute.getLocation();
        if (!enabledVertexArrays[location]) {
            glEnableVertexAttribArray(location);
            enabledVertexArrays[location] = true;
        }
        boolean normalized = accessor.isNormalized();
        BufferView view = accessor.getBufferView();
        com.nucleus.scene.gltf.Buffer b = view.getBuffer();
        ComponentType ct = accessor.getComponentType();
        Type t = accessor.getType();
        if (b.getBufferName() > 0) {
            int target = view.getTarget() != null ? view.getTarget().value : GLES20.GL_ARRAY_BUFFER;
            glBindBuffer(target, b.getBufferName());
            glVertexAttribPointer(location, t.size, ct.value, normalized, view.getByteStride(),
                    accessor.getByteOffset() + view.getByteOffset());
        } else {
            ByteBuffer bb = accessor.getBuffer();
            glVertexAttribPointer(location, t.size, ct.value, normalized, view.getByteStride(), bb);
        }
        GLUtils.handleError(this, "VertexAttribPointer for attribute location: " + attribute.getLocation());
    }

    /**
     * Abstraction for glEnableVertexAttribArray()
     * 
     * @param index
     */
    public abstract void glEnableVertexAttribArray(int index);

    /**
     * Abstraction for glDisableVertexAttribArray
     * 
     * @param index
     */
    public abstract void glDisableVertexAttribArray(int index);

    /**
     * Abstraction for glUniformMatrix4fv()
     * 
     * @param location
     * @param count
     * @param transpose
     * @param buffer
     */
    public abstract void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer buffer);

    /**
     * Abstraction for glUniformMatrix3fv()
     * 
     * @param location
     * @param count
     * @param transpose
     * @param buffer
     */
    public abstract void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer buffer);

    /**
     * Abstraction for glUniformMatrix2fv()
     * 
     * @param location
     * @param count
     * @param transpose
     * @param buffer
     */
    public abstract void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer buffer);

    /**
     * Abstraction for glDrawArrays()
     * 
     * @param mode
     * @param first
     * @param count
     */
    public abstract void glDrawArrays(int mode, int first, int count);

    /**
     * Abstraction for glDrawElements()
     * Use VBO's
     * 
     * @param mode
     * @param count
     * @param type
     * @param indices
     */
    @Deprecated
    public abstract void glDrawElements(int mode, int count, int type, Buffer indices);

    /**
     * Abstraction for glDrawElements()
     * 
     * @param mode
     * @param count
     * @param type
     * @param offset
     */
    public abstract void glDrawElements(int mode, int count, int type, int offset);

    /**
     * Abstraction for glBindAttribLocation()
     * 
     * @param program
     * @param index
     * @param name
     */
    public abstract void glBindAttribLocation(int program, int index, String name);

    /**
     * Abstraction for glViewport()
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public abstract void glViewport(int x, int y, int width, int height);

    /**
     * Abstraction for glGetShaderInfoLog
     * 
     * @param shader
     * @return
     */
    public abstract String glGetShaderInfoLog(int shader);

    /**
     * Abstraction for glGetProgramInfoLog(program)
     * 
     * @param program
     * @return
     */
    public abstract String glGetProgramInfoLog(int program);

    /**
     * Abstraction for glGenTextures()
     * 
     * @param textures
     */
    public abstract void glGenTextures(int[] textures);

    /**
     * Abstraction for glActiveTexture()
     * 
     * @param texture
     */
    public abstract void glActiveTexture(int texture);

    /**
     * Abstraction for glBindTexture()
     * 
     * @param target
     * @param texture
     */
    public abstract void glBindTexture(int target, int texture);

    /**
     * Abstraction for glGetString()
     * 
     * @param name
     * @return
     */
    public abstract String glGetString(int name);

    /**
     * Abstraction for glGetIntegerv()
     * 
     * @param pname
     * @param params
     */
    public abstract void glGetIntegerv(int pname, int[] params);

    /**
     * Abstraction for glUniform4fv()
     * 
     * @param location
     * @param count
     * @param buffer
     */
    public abstract void glUniform4fv(int location, int count, FloatBuffer buffer);

    public abstract void glUniform3fv(int location, int count, FloatBuffer buffer);

    public abstract void glUniform2fv(int location, int count, FloatBuffer buffer);

    public abstract void glUniform1fv(int location, int count, FloatBuffer buffer);

    public abstract void glUniform1iv(int location, int count, IntBuffer buffer);

    public abstract void glUniform1i(int location, int unit);

    /**
     * Abstraction for glTexParemeterf()
     * 
     * @param target
     * @param pname
     * @param param
     */
    public abstract void glTexParameterf(int target, int pname, float param);

    /**
     * Abstraction for glTexParameteri()
     * 
     * @param target
     * @param pname
     * @param param
     */
    public abstract void glTexParameteri(int target, int pname, int param);

    /**
     * Abstraction for glClearColor();
     * 
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public abstract void glClearColor(float red, float green, float blue, float alpha);

    /**
     * Abstraction for glColorMask
     * 
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public abstract void glColorMask(boolean red, boolean green, boolean blue, boolean alpha);

    /**
     * Abstraction for glClear()
     * 
     * @param mask
     */
    public abstract void glClear(int mask);

    /**
     * Abstraction for glDisable()
     * 
     * @param cap
     */
    public abstract void glDisable(int cap);

    /**
     * Abstraction for glEnable()
     * 
     * @param cap
     */
    public abstract void glEnable(int cap);

    /**
     * Abstraction for glCullFace()
     * 
     * @param mode
     */
    public abstract void glCullFace(int mode);

    /**
     * Abstraction for glLineWidth()
     * 
     * @param width
     */
    public abstract void glLineWidth(float width);

    /**
     * Abstraction for glDepthFunc()
     * 
     * @param func
     */
    public abstract void glDepthFunc(int func);

    /**
     * Abstraction for glDepthMask()
     * 
     * @param flag
     */
    public abstract void glDepthMask(boolean flag);

    /**
     * Abstraction for glClearDepthf
     * 
     * @param depth
     */
    public abstract void glClearDepthf(float depth);

    /**
     * Abstraction for glDepthRangef
     * 
     * @param nearVal
     * @param farVal
     */
    public abstract void glDepthRangef(float nearVal, float farVal);

    /**
     * Abstraction for glTexImage2D()
     * Use {@link #texImage(Texture2D)} instead to get support for different OpenGLES versions
     * 
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param border
     * @param format
     * @param type
     * @param pixels
     */
    @Deprecated
    public abstract void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, Buffer pixels);

    /**
     * Abstraction for glPixelStorei
     * 
     * @param pname
     * @param param
     */
    public abstract void glPixelStorei(int pname, int param);

    /**
     * Abstraction for glDeleteTextures()
     * 
     * @param textures
     */
    public abstract void glDeleteTextures(int[] textures);

    /**
     * Abstraction for glGenerateMipmap
     * 
     * @param target
     */
    public abstract void glGenerateMipmap(int target);

    /**
     * Abstraction for glBlendEquationSeparate
     * 
     * @param modeRGB
     * @param modeAlpha
     */
    public abstract void glBlendEquationSeparate(int modeRGB, int modeAlpha);

    /**
     * Abstraction for glBlendFuncSeparate
     * 
     * @param srcRGB
     * @param dstRGB
     * @param srcAlpha
     * @param dstAlpha
     */
    public abstract void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha);

    /**
     * Abstraction for glFinish()
     */
    public abstract void glFinish();

    /**
     * Abstraction for glCheckFramebufferStatus(target);
     * 
     * @param target
     * @return
     */
    public abstract int glCheckFramebufferStatus(int target);

    /**
     * Abstraction for glGetShaderSource(GLuint shader, GLsizei bufSize, GLsizei *length, GLchar *source);
     * 
     * @param shader
     * @return
     */
    public String glGetShaderSource(int shader) {
        glGetShaderiv(shader, GLES20.GL_SHADER_SOURCE_LENGTH, shaderSourceLength);
        StringBuffer result = new StringBuffer();
        if (shaderSourceLength.get(0) == 0) {
            return "No shader source";
        }
        byte[] buffer = new byte[shaderSourceLength.get(0)];
        int[] read = new int[1];
        glGetShaderSource(shader, buffer.length, read, buffer);
        result.append(new String(buffer, 0, read[0]));
        return result.toString();
    }

    /**
     * Abstraction for glGetShaderSource(GLuint shader, GLsizei bufSize, GLsizei *length, GLchar *source);
     * 
     * @param shader
     * @return
     */
    public abstract void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source);

    /**
     * Sets the texture parameter values for the texture parameter to OpenGL, call this to set the correct texture
     * parameters when rendering.
     * 
     * @param texParameters
     */
    public void uploadTexParameters(TextureParameter texParameters) throws GLException {
        Parameter[] values = texParameters.getParameters();
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                getTextureParameter(values[TextureParameter.MIN_FILTER_INDEX]));
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                getTextureParameter(values[TextureParameter.MAG_FILTER_INDEX]));
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                getTextureParameter(values[TextureParameter.WRAP_S_INDEX]));
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                getTextureParameter(values[TextureParameter.WRAP_T_INDEX]));
        ParameterData[] parameters = texParameters.getParameterData();
        if (parameters != null) {
            for (ParameterData data : parameters) {
                glTexParameteri(getTextureTarget(data.target), getTexturePName(data.name),
                        getTextureParameter(data.param));
            }
        }
        GLUtils.handleError(this, "glTexParameteri ");
    }

    /**
     * Sets the texture parameter values for the texture parameter to OpenGL, call this to set the correct texture
     * parameters when rendering.
     * 
     * @param texture
     */
    public void uploadTexParameters(Texture texture) throws GLException {
        Sampler sampler = texture.getSampler();
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, sampler.getMinFilter());
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, sampler.getMagFilter());
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, sampler.getWrapS());
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, sampler.getWrapT());
        GLUtils.handleError(this, "glTexParameters ");
    }

    /**
     * Binds the frambebuffer texture target - this is used to create different behavior depending
     * on the OpenGL ES implementation (2.X vs 3.X)
     * 
     * @param texture
     * @param fbName
     * @param attachement
     * @throws GLException
     */
    public void bindFramebufferTexture(Texture2D texture, int fbName, Attachement attachement) throws GLException {
        glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbName);
        glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, this.attachement[attachement.index], GLES20.GL_TEXTURE_2D,
                texture.getName(), 0);
        GLUtils.handleError(this, "glFramebufferTexture");
        int status = glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalArgumentException("Framebuffer status not complete: " + Error.getError(status));
        }
    }

    /**
     * Creates texture buffer, eg allocate the texture - use this method in favour of calling glTexImage directly since
     * this method will handle texture format differences between GL versions.
     * 
     * @param texture
     */
    public void texImage(Texture2D texture) {
        Format format = texture.getFormat();
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, TextureUtils.getInternalFormat(texture), texture.getWidth(),
                texture.getHeight(), 0, format.format,
                texture.getType().type, null);
        glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, format.rowAlignment);
    }

    /**
     * Upload texture to a texture with allocated buffer - use this method in favour of calling glTexImage directly
     * since this method will handle texture format differences between GL versions.
     * The texture must be bound to the texture name before calling this method
     * 
     * @param texture
     * @param image
     * @param level
     */
    public void texImage(Texture2D texture, BufferImage image, int level) {
        Format format = texture.getFormat();
        glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, format.rowAlignment);
        glTexImage2D(GLES20.GL_TEXTURE_2D, level, TextureUtils.getInternalFormat(texture), texture.getWidth(),
                texture.getHeight(), 0, format.format,
                texture.getType().type, image.getBuffer().position(0));
    }

    /**
     * Uploads the texture image - use this method in favor of calling glTexImage directly since this method will
     * handle format differences between GL versions.
     * The texture must be bound to the texture name before calling this method
     * 
     * @param image
     * @param level
     * @return The internalformat used
     */
    public Format texImage(Image image, int level) {
        BufferImage bufferImage = image.getBufferImage();
        ImageFormat imageFormat = bufferImage.getFormat();
        Format format = TextureUtils.getFormat(imageFormat, bufferImage.getColorModel());
        com.nucleus.texturing.Texture2D.Type type = TextureUtils.getType(imageFormat);
        glTexImage2D(GLES20.GL_TEXTURE_2D, level, format.internalFormat, bufferImage.getWidth(),
                bufferImage.getHeight(), 0, format.format, type.type,
                bufferImage.getBuffer().position(0));
        glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, format.rowAlignment);
        return format;
    }

    @Override
    public SLVersion replaceShaderVersion(SLVersion version) {
        // Make sure version is not too high for es 2
        if (version.number > SLVersion.VERSION100.number) {
            throw new IllegalArgumentException("Illegal shader version " + version);
        }
        return version;
    }

    @Override
    public void loadVersionedShaderSource(ShaderSource source) throws IOException {
        InputStream shaderStream = getClass().getClassLoader()
                .getResourceAsStream(source.getFullSourceName());
        if (shaderStream == null) {
            URL classUrl = FileUtils.getInstance().getClassLocation(this.getClass());
            String classLocation = classUrl != null ? classUrl.getFile() : "null";
            String error = "Could not open " + source.getFullSourceName() + "\n\rClass URL: " + classLocation;
            throw new IllegalArgumentException(error);
        }

        source.setSource(StreamUtils.readStringFromStream(shaderStream));
    }

    @Override
    public void destroy() {
        // Override in subclass to do implementation specific GL teardown.
    }

}
