package com.nucleus.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.StringTokenizer;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.io.StreamUtils;
import com.nucleus.opengl.GLException.Error;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RendererInfo;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.texturing.Image;
import com.nucleus.texturing.ParameterData;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Parameter;
import com.nucleus.texturing.TextureUtils;

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

    public static String VERSION = "#version";
    public static String ES = "es";
    public static String SHADING_LANGUAGE_100 = "100";

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
     * @param names
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
            Buffer ptr);

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
     * @param buffer
     * @param target
     * @param position Position in buffer where the data for this attribute is.
     * @param attrib Array of attributes to set
     */
    public void glVertexAttribPointer(AttributeBuffer buffer, int target, ShaderVariable[] attribs) {
        if (buffer.getBufferName() > 0) {
            glBindBuffer(target, buffer.getBufferName());
            if (buffer.isDirty()) {
                glBufferData(target, buffer.getSizeInBytes(), buffer.getBuffer().position(0),
                        GLES20.GL_STATIC_DRAW);
                buffer.setDirty(false);
            }
            for (ShaderVariable a : attribs) {
                if (a != null) {
                    glEnableVertexAttribArray(a.getLocation());
                    glVertexAttribPointer(a.getLocation(), a.getComponentCount(), buffer.getDataType(), false,
                            buffer.getByteStride(), a.getOffset() * 4);
                }
            }
        } else {
            for (ShaderVariable a : attribs) {
                if (a != null) {
                    glEnableVertexAttribArray(a.getLocation());
                    glVertexAttribPointer(a.getLocation(), a.getComponentCount(), buffer.getDataType(), false,
                            buffer.getByteStride(), buffer.getBuffer().position(a.getOffset()));
                }
            }
        }
    }

    /**
     * Abstraction for glEnableVertexAttribArray()
     * 
     * @param index
     */
    public abstract void glEnableVertexAttribArray(int index);

    /**
     * Abstraction for glUniformMatrix4fv()
     * 
     * @param location
     * @param count
     * @param transpose
     * @param transform
     * @param v
     * @param offset
     */
    public abstract void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset);

    /**
     * Abstraction for glUniformMatrix3fv()
     * 
     * @param location
     * @param count
     * @param transpose
     * @param transform
     * @param v
     * @param offset
     */
    public abstract void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset);

    /**
     * Abstraction for glUniformMatrix2fv()
     * 
     * @param location
     * @param count
     * @param transpose
     * @param transform
     * @param v
     * @param offset
     */
    public abstract void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset);

    /**
     * Abstraction for glUniform1iv();
     * 
     * @param location
     * @param count
     * @param v0
     * @param offset
     */
    public abstract void glUniform1iv(int location, int count, int[] v0, int offset);

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
     * @param v
     * @param offset
     */
    public abstract void glUniform4fv(int location, int count, float[] v, int offset);

    public abstract void glUniform3fv(int location, int count, float[] v, int offset);

    public abstract void glUniform2fv(int location, int count, float[] v, int offset);

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
        IntBuffer sourceLength = IntBuffer.allocate(1);
        glGetShaderiv(shader, GLES20.GL_SHADER_SOURCE_LENGTH, sourceLength);
        StringBuffer result = new StringBuffer();
        if (sourceLength.get(0) == 0) {
            return "No shader source";
        }
        byte[] buffer = new byte[sourceLength.get(0)];
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
                values[TextureParameter.MIN_FILTER_INDEX].value);
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                values[TextureParameter.MAG_FILTER_INDEX].value);
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, values[TextureParameter.WRAP_S_INDEX].value);
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, values[TextureParameter.WRAP_T_INDEX].value);
        ParameterData[] parameters = texParameters.getParameterData();
        if (parameters != null) {
            for (ParameterData data : parameters) {
                glTexParameteri(data.target.target, data.name.name, data.param.param);
            }
        }
        GLUtils.handleError(this, "glTexParameteri ");
    }

    /**
     * Binds the frambebuffer texture target - this is used to create different behavior depending
     * on the OpenGL ES implementation (2.X vs 3.X)
     * 
     * @param texture
     * @param fbName
     * @param attachement
     * @param textureName
     * @throws GLException
     */
    public void bindFramebufferTexture(Texture2D texture, int fbName, Attachement attachement) throws GLException {
        glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbName);
        glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, attachement.value, GLES20.GL_TEXTURE_2D,
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
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, TextureUtils.getInternalFormat(texture), texture.getWidth(),
                texture.getHeight(), 0, texture.getFormat().format,
                texture.getType().type, null);
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
    public void texImage(Texture2D texture, Image image, int level) {
        glTexImage2D(GLES20.GL_TEXTURE_2D, level, TextureUtils.getInternalFormat(texture), texture.getWidth(),
                texture.getHeight(), 0, texture.getFormat().format,
                texture.getType().type, image.getBuffer().position(0));
    }

    @Override
    public String getShaderVersion() {
        return SHADING_LANGUAGE_100;
    }

    /**
     * Checks if the first (non empty) line contains version
     * 
     * @param source
     * @return True if the first, non empty, line contains #version declaration
     */
    protected boolean hasVersion(String source) {
        StringTokenizer st = new StringTokenizer(source, "\n");
        String t = st.nextToken().trim();
        if (t.toLowerCase().startsWith(VERSION)) {
            return true;
        }
        return false;
    }

    @Override
    public String getVersionedShaderSource(InputStream shaderStream, int type, boolean library) throws IOException {
        String source = StreamUtils.readStringFromStream(shaderStream);
        if (!library && !hasVersion(source)) {
            // Insert version 100
            return VERSION + " " + SHADING_LANGUAGE_100 + System.lineSeparator() + source;
        }
        return source;
    }

    @Override
    public RendererInfo getInfo() {
        if (rendererInfo == null) {
            rendererInfo = new RendererInfo(this);
        }
        return rendererInfo;
    }

}
