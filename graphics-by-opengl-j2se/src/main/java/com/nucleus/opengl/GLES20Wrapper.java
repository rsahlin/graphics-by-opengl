package com.nucleus.opengl;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.nucleus.geometry.VertexBuffer;
import com.nucleus.shader.ShaderVariable;

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
     * Abstraction for glGenBuffers()
     * 
     * @param n Number of buffer names to create
     * @param buffers Storage for buffer names
     * @param offset Offset into buffers where names are put
     */
    public abstract void glGenBuffers(int n, int[] buffers, int offset);

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
     * @param bufsize
     * @param length
     * @param lengthOffset
     * @param size
     * @param sizeOffset
     * @param type
     * @param typeOffset
     * @param name
     * @param nameOffset
     */
    public abstract void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int lengthOffset,
            int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset);

    /**
     * Abstraction for glGetActiveUniform()
     * 
     * @param program
     * @param index
     * @param bufsize
     * @param length
     * @param lengthOffset
     * @param size
     * @param sizeOffset
     * @param type
     * @param typeOffset
     * @param name
     * @param nameOffset
     */
    public abstract void glGetActiveUniform(int program, int index, int bufsize, int[] length, int lengthOffset,
            int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset);

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
     * @param offsets Offset, in number of floats, into the buffer where the data is for the different attributes
     */
    public void glVertexAttribPointer(VertexBuffer buffer, int target, ShaderVariable[] attribs, int[] offsets) {
        if (buffer.getBufferName() > 0) {
            glBindBuffer(target, buffer.getBufferName());
            glBufferData(target, buffer.getSizeInBytes(), buffer.getBuffer().position(0),
                    GLES20.GL_STATIC_DRAW);
            int index = 0;
            for (ShaderVariable a : attribs) {
                glEnableVertexAttribArray(a.getLocation());
                glVertexAttribPointer(a.getLocation(), buffer.getComponentCount(), buffer.getDataType(), false,
                        buffer.getByteStride(), offsets[index++] * 4);
            }
            glBindBuffer(target, 0);

        } else {
            int index = 0;
            for (ShaderVariable a : attribs) {
                glEnableVertexAttribArray(a.getLocation());
                glVertexAttribPointer(a.getLocation(), buffer.getComponentCount(), buffer.getDataType(), false,
                        buffer.getByteStride(), buffer.getBuffer().position(offsets[index++]));
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
     * Abstraction for glDrawArrays()
     * 
     * @param mode
     * @param first
     * @param count
     */
    public abstract void glDrawArrays(int mode, int first, int count);

    /**
     * Abstraction for glDrawElements()
     * 
     * @param mode
     * @param count
     * @param type
     * @param indices
     */
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
     * @param count
     * @param textures
     * @param offset
     */
    public abstract void glGenTextures(int count, int[] textures, int offset);

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
    public abstract void glGetIntegerv(int pname, int[] params, int offset);

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
     * Abstraction for glTexImage2D()
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
    public abstract void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, Buffer pixels);

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

}
