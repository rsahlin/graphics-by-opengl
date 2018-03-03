package com.nucleus.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES30Wrapper;
import com.nucleus.renderer.NucleusRenderer;

/**
 * Implementation of the LWJGL3 wrapper
 * TODO - Shall this be made static since the underlying GL/GLES is static?
 * It would simplify handling GLES2.0, 3.0, 3.1
 *
 */
public class LWJGL3GLES30Wrapper extends GLES30Wrapper {

    protected LWJGL3GLES20Wrapper gles20;

    /**
     * Implementation constructor - DO NOT USE - fetch wrapper from {@link NucleusRenderer}
     * 
     * @param renderVersion If higher than GLES30, otherwise null
     */
    public LWJGL3GLES30Wrapper(Renderers version) {
        super(Platform.GL, version);
        gles20 = new LWJGL3GLES20Wrapper(version);
    }

    /**
     * ---------------------------------------------------
     * GLES20 calls - just pass on to GLES20 wrapper
     * ---------------------------------------------------
     */

    @Override
    public void glAttachShader(int program, int shader) {
        gles20.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        gles20.glLinkProgram(program);
    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        gles20.glShaderSource(shader, shaderSource);
    }

    @Override
    public void glCompileShader(int shader) {
        gles20.glCompileShader(shader);
    }

    @Override
    public int glCreateShader(int type) {
        return gles20.glCreateShader(type);
    }

    @Override
    public int glCreateProgram() {
        return gles20.glCreateProgram();
    }

    @Override
    public void glDeleteProgram(int program) {
        gles20.glDeleteProgram(program);
    }

    @Override
    public void glGenBuffers(int[] buffers) {
        gles20.glGenBuffers(buffers);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        gles20.glDeleteBuffers(n, buffers, offset);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        gles20.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        gles20.glBufferData(target, size, data, usage);
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        gles20.glGetShaderiv(shader, pname, params);
    }

    @Override
    public void glUseProgram(int program) {
        gles20.glUseProgram(program);
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        gles20.glGetProgramiv(program, pname, params, offset);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        gles20.glGetActiveAttrib(program, index, length, lengthOffset, size, sizeOffset, type, typeOffset, name);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        gles20.glGetActiveUniform(program, index, length, lengthOffset, size, sizeOffset, type, typeOffset, name);
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return gles20.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return gles20.glGetAttribLocation(program, name);
    }

    @Override
    public int glGetError() {
        return gles20.glGetError();
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, Buffer ptr) {
        gles20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);

    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        gles20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        gles20.glEnableVertexAttribArray(index);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        gles20.glUniformMatrix4fv(location, count, transpose, v, offset);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset) {
        gles20.glUniformMatrix3fv(location, count, transpose, v, offset);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset) {
        gles20.glUniformMatrix2fv(location, count, transpose, v, offset);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        gles20.glDrawArrays(mode, first, count);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        gles20.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        gles20.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        gles20.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        gles20.glViewport(x, y, width, height);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return gles20.glGetShaderInfoLog(shader);
    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return gles20.glGetProgramInfoLog(program);
    }

    @Override
    public void glGenTextures(int[] textures) {
        gles20.glGenTextures(textures);
    }

    @Override
    public void glActiveTexture(int texture) {
        gles20.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        gles20.glBindTexture(target, texture);
    }

    @Override
    public String glGetString(int name) {
        return gles20.glGetString(name);
    }

    @Override
    public void glGetIntegerv(int pname, int[] params) {
        gles20.glGetIntegerv(pname, params);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        gles20.glUniform4fv(location, count, v, offset);
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        gles20.glUniform3fv(location, count, v, offset);
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v0, int offset) {
        gles20.glUniform1iv(location, count, v0, offset);
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        gles20.glUniform2fv(location, count, v, offset);

    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        gles20.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        gles20.glTexParameteri(target, pname, param);

    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        gles20.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClear(int mask) {
        gles20.glClear(mask);
    }

    @Override
    public void glDisable(int cap) {
        gles20.glDisable(cap);
    }

    @Override
    public void glEnable(int cap) {
        gles20.glEnable(cap);
    }

    @Override
    public void glCullFace(int mode) {
        gles20.glCullFace(mode);
    }

    @Override
    public void glLineWidth(float width) {
        gles20.glLineWidth(width);
    }

    @Override
    public void glDepthFunc(int func) {
        gles20.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        gles20.glDepthMask(flag);
    }

    @Override
    public void glClearDepthf(float depth) {
        gles20.glClearDepthf(depth);
    }

    @Override
    public void glDepthRangef(float nearVal, float farVal) {
        gles20.glDepthRangef(nearVal, farVal);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        gles20.glTexImage2D(target, level, internalformat, width, height, border, format, type,
                pixels);
    }

    @Override
    public void glDeleteTextures(int[] textures) {
        gles20.glDeleteTextures(textures);
    }

    @Override
    public void glGenerateMipmap(int target) {
        gles20.glGenerateMipmap(target);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        gles20.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        gles20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glFinish() {
        gles20.glFinish();
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        gles20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glGenFramebuffers(int[] buffers) {
        gles20.glGenFramebuffers(buffers);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return gles20.glCheckFramebufferStatus(target);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        gles20.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        gles20.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glValidateProgram(int program) {
        gles20.glValidateProgram(program);
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source) {
        gles20.glGetShaderSource(shader, bufsize, length, source);
    }

    /**
     * 
     * GLES30--------------------------------------------------------------------------------
     * 
     */

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {
        org.lwjgl.opengles.GLES30.glSamplerParameteri(sampler, pname, sampler);
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {
        org.lwjgl.opengles.GLES30.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        org.lwjgl.opengles.GLES30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, int ptroffset, int ptrsize) {
        org.lwjgl.opengles.GLES30.glBindBufferRange(target, index, buffer, ptroffset, ptrsize);
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {
        return org.lwjgl.opengles.GLES30.glGetUniformBlockIndex(program, uniformBlockName);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {
        IntBuffer intBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        org.lwjgl.opengles.GLES30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, intBuffer);
        params[offset] = intBuffer.get(0);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return org.lwjgl.opengles.GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset,
            int pname, int[] params, int paramsOffset) {
        IntBuffer indicesBuffer = ByteBuffer.allocateDirect(uniformCount * 4).asIntBuffer();
        indicesBuffer.put(uniformIndices, indicesOffset, uniformCount);
        indicesBuffer.position(0);
        IntBuffer paramsBuffer = ByteBuffer.allocateDirect(uniformCount * 4).asIntBuffer();
        org.lwjgl.opengles.GLES30.glGetActiveUniformsiv(program, indicesBuffer, pname, paramsBuffer);
        paramsBuffer.position(0);
        paramsBuffer.get(params, paramsOffset, uniformCount);

    }

    @Override
    public ByteBuffer glMapBufferRange(int target, int offset, int length, int access) {
        return org.lwjgl.opengles.GLES30.glMapBufferRange(target, offset, length, access);
    }

    @Override
    public boolean glUnmapBuffer(int target) {
        return org.lwjgl.opengles.GLES30.glUnmapBuffer(target);
    }

    @Override
    public void glFlushMappedBufferRange(int target, int offset, int length) {
        org.lwjgl.opengles.GLES30.glFlushMappedBufferRange(target, offset, length);
    }

}
