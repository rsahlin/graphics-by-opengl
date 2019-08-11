package com.nucleus.opengl.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.common.BufferUtils;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer.Renderers;

public class LWJGL3GLES20Wrapper extends GLES20Wrapper {

    /**
     * Implementation constructor - DO NOT USE
     * 
     * @param renderVersion If higher than GLES30, otherwise null
     */
    public LWJGL3GLES20Wrapper(Renderers version) {
        super(Platform.GL, version);
    }

    @Override
    public void glAttachShader(int program, int shader) {
        org.lwjgl.opengles.GLES20.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        org.lwjgl.opengles.GLES20.glLinkProgram(program);
    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        org.lwjgl.opengles.GLES20.glShaderSource(shader, shaderSource);
    }

    @Override
    public void glCompileShader(int shader) {
        org.lwjgl.opengles.GLES20.glCompileShader(shader);
    }

    @Override
    public int glCreateShader(int type) {
        return org.lwjgl.opengles.GLES20.glCreateShader(type);
    }

    @Override
    public int glCreateProgram() {
        return org.lwjgl.opengles.GLES20.glCreateProgram();
    }

    @Override
    public void glDeleteProgram(int program) {
        org.lwjgl.opengles.GLES20.glDeleteProgram(program);
    }

    @Override
    public void glGenBuffers(int[] buffers) {
        IntBuffer ib = BufferUtils.createIntBuffer(buffers.length);
        org.lwjgl.opengles.GLES20.glGenBuffers(ib);
        LWJGLUtils.toArray((IntBuffer) ib.rewind(), buffers, 0);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        IntBuffer ib = LWJGLUtils.toIntBuffer(buffers, buffers.length - offset, offset);
        org.lwjgl.opengles.GLES20.glDeleteBuffers(ib);
        LWJGLUtils.toArray(ib, buffers, offset);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        org.lwjgl.opengles.GLES20.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        if (data instanceof FloatBuffer) {
            org.lwjgl.opengles.GLES20.glBufferData(target, (FloatBuffer) data, usage);
        } else if (data instanceof ByteBuffer) {
            org.lwjgl.opengles.GLES20.glBufferData(target, (ByteBuffer) data, usage);
        } else {
            throw new IllegalArgumentException("Not handled");
        }
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        org.lwjgl.opengles.GLES20.glGetShaderiv(shader, pname, params);
    }

    @Override
    public void glUseProgram(int program) {
        org.lwjgl.opengles.GLES20.glUseProgram(program);
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        IntBuffer v = BufferUtils.createIntBuffer(1);
        org.lwjgl.opengles.GLES20.glGetProgramiv(program, pname, v);
        v.position(0);
        params[offset] = v.get();
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer sizeBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer typeBuffer = BufferUtils.createIntBuffer(1);
        ByteBuffer nameBuffer = BufferUtils.createByteBuffer(name.length);
        org.lwjgl.opengles.GLES20.glGetActiveAttrib(program, index, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
        lengthBuffer.position(0);
        sizeBuffer.position(0);
        typeBuffer.position(0);
        length[lengthOffset] = lengthBuffer.get();
        size[sizeOffset] = sizeBuffer.get();
        type[typeOffset] = typeBuffer.get();
        nameBuffer.get(name);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer sizeBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer typeBuffer = BufferUtils.createIntBuffer(1);
        ByteBuffer nameBuffer = BufferUtils.createByteBuffer(name.length);
        org.lwjgl.opengles.GLES20.glGetActiveUniform(program, index, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
        lengthBuffer.position(0);
        sizeBuffer.position(0);
        typeBuffer.position(0);
        length[lengthOffset] = lengthBuffer.get();
        size[sizeOffset] = sizeBuffer.get();
        type[typeOffset] = typeBuffer.get();
        nameBuffer.get(name);
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return org.lwjgl.opengles.GLES20.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return org.lwjgl.opengles.GLES20.glGetAttribLocation(program, name);
    }

    @Override
    public int glGetError() {
        return org.lwjgl.opengles.GLES20.glGetError();
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer ptr) {
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, FloatBuffer ptr) {
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        org.lwjgl.opengles.GLES20.glEnableVertexAttribArray(index);
    }

    @Override
    public void glDisableVertexAttribArray(int index) {
        org.lwjgl.opengles.GLES20.glDisableVertexAttribArray(index);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniformMatrix4fv(location, transpose, buffer);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniformMatrix3fv(location, transpose, buffer);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniformMatrix2fv(location, transpose, buffer);
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniform4fv(location, buffer);
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniform3fv(location, buffer);
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniform2fv(location, buffer);
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniform1fv(location, buffer);
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer buffer) {
        org.lwjgl.opengles.GLES20.glUniform1iv(location, buffer);
    }

    @Override
    public void glUniform1i(int location, int unit) {
        org.lwjgl.opengles.GLES20.glUniform1i(location, unit);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        org.lwjgl.opengles.GLES20.glDrawArrays(mode, first, count);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        org.lwjgl.opengles.GLES20.glDrawElements(mode, type, (ByteBuffer) indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        org.lwjgl.opengles.GLES20.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        org.lwjgl.opengles.GLES20.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        org.lwjgl.opengles.GLES20.glViewport(x, y, width, height);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return org.lwjgl.opengles.GLES20.glGetShaderInfoLog(shader);
    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return org.lwjgl.opengles.GLES20.glGetProgramInfoLog(program);
    }

    @Override
    public void glGenTextures(int[] textures) {
        IntBuffer ib = BufferUtils.createIntBuffer(textures.length);
        org.lwjgl.opengles.GLES20.glGenTextures(ib);
        LWJGLUtils.toArray((IntBuffer) ib.rewind(), textures, 0);
    }

    @Override
    public void glActiveTexture(int texture) {
        org.lwjgl.opengles.GLES20.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        org.lwjgl.opengles.GLES20.glBindTexture(target, texture);
    }

    @Override
    public String glGetString(int name) {
        return org.lwjgl.opengles.GLES20.glGetString(name);
    }

    @Override
    public void glGetIntegerv(int pname, int[] params) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(params.length);
        org.lwjgl.opengles.GLES20.glGetIntegerv(pname, intBuffer);
        intBuffer.position(0);
        intBuffer.get(params);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        org.lwjgl.opengles.GLES20.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        org.lwjgl.opengles.GLES20.glTexParameteri(target, pname, param);

    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        org.lwjgl.opengles.GLES20.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClear(int mask) {
        org.lwjgl.opengles.GLES20.glClear(mask);
    }

    @Override
    public void glDisable(int cap) {
        org.lwjgl.opengles.GLES20.glDisable(cap);
    }

    @Override
    public void glEnable(int cap) {
        org.lwjgl.opengles.GLES20.glEnable(cap);
    }

    @Override
    public void glCullFace(int mode) {
        org.lwjgl.opengles.GLES20.glCullFace(mode);
    }

    @Override
    public void glLineWidth(float width) {
        org.lwjgl.opengles.GLES20.glLineWidth(width);
    }

    @Override
    public void glDepthFunc(int func) {
        org.lwjgl.opengles.GLES20.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        org.lwjgl.opengles.GLES20.glDepthMask(flag);
    }

    @Override
    public void glClearDepthf(float depth) {
        org.lwjgl.opengles.GLES20.glClearDepthf(depth);
    }

    @Override
    public void glDepthRangef(float nearVal, float farVal) {
        org.lwjgl.opengles.GLES20.glDepthRangef(nearVal, farVal);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        org.lwjgl.opengles.GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type,
                (ByteBuffer) pixels);
    }

    @Override
    public void glDeleteTextures(int[] textures) {
        org.lwjgl.opengles.GLES20.glDeleteTextures(LWJGLUtils.toIntBuffer(textures, textures.length, 0));
    }

    @Override
    public void glGenerateMipmap(int target) {
        org.lwjgl.opengles.GLES20.glGenerateMipmap(target);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        org.lwjgl.opengles.GLES20.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        org.lwjgl.opengles.GLES20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glFinish() {
        org.lwjgl.opengles.GLES20.glFinish();
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        org.lwjgl.opengles.GLES20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glGenFramebuffers(int[] buffers) {
        org.lwjgl.opengles.GLES20.glGenFramebuffers(buffers);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return org.lwjgl.opengles.GLES20.glCheckFramebufferStatus(target);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        org.lwjgl.opengles.GLES20.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        org.lwjgl.opengles.GLES20.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glValidateProgram(int program) {
        org.lwjgl.opengles.GLES20.glValidateProgram(program);
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source) {
        ByteBuffer bufferSource = BufferUtils.createByteBuffer(bufsize);
        org.lwjgl.opengles.GLES20.glGetShaderSource(shader, length, bufferSource);
        bufferSource.position(0);
        bufferSource.get(source, 0, length[0]);
    }

    @Override
    public void glPixelStorei(int pname, int param) {
        org.lwjgl.opengles.GLES20.glPixelStorei(pname, param);
    }

}
