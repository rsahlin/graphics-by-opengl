package com.nucleus.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES30Wrapper;

public class LWJGL3GLES20Wrapper extends GLES30Wrapper {

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
        IntBuffer ib = ByteBuffer.allocateDirect(buffers.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        org.lwjgl.opengles.GLES20.glGenBuffers(ib);
        toArray((IntBuffer) ib.rewind(), buffers, 0);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        IntBuffer ib = toIntBuffer(buffers, buffers.length - offset, offset);
        org.lwjgl.opengles.GLES20.glDeleteBuffers(ib);
        toArray(ib, buffers, offset);
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
        IntBuffer v = ByteBuffer.allocateDirect((params.length - offset) * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        org.lwjgl.opengles.GLES20.glGetProgramiv(program, pname, v);
        toArray((IntBuffer) v.rewind(), params, offset);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        IntBuffer lengthBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer sizeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer typeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ByteBuffer nameBuffer = ByteBuffer.allocateDirect(name.length).order(ByteOrder.nativeOrder());
        org.lwjgl.opengles.GLES20.glGetActiveAttrib(program, index, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
        toArray((IntBuffer) lengthBuffer.rewind(), length, lengthOffset);
        toArray((IntBuffer) sizeBuffer.rewind(), size, sizeOffset);
        toArray((IntBuffer) typeBuffer.rewind(), type, typeOffset);
        toArray((ByteBuffer) nameBuffer.rewind(), name, 0);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        IntBuffer lengthBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer sizeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer typeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ByteBuffer nameBuffer = ByteBuffer.allocateDirect(name.length).order(ByteOrder.nativeOrder());
        org.lwjgl.opengles.GLES20.glGetActiveUniform(program, index, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
        toArray((IntBuffer) lengthBuffer.rewind(), length, lengthOffset);
        toArray((IntBuffer) sizeBuffer.rewind(), size, sizeOffset);
        toArray((IntBuffer) typeBuffer.rewind(), type, typeOffset);
        toArray((ByteBuffer) nameBuffer.rewind(), name, 0);
    }

    /**
     * Copies the data from the source intbuffer to the destination
     * 
     * @param source
     * @param dest
     * @param destOffset
     */
    private void toArray(IntBuffer source, int[] dest, int destOffset) {
        while (source.hasRemaining()) {
            dest[destOffset++] = source.get();
        }
    }

    /**
     * Copies the data from the source bytebuffer to the destination
     * 
     * @param source
     * @param dest
     * @param destOffset
     */
    private void toArray(ByteBuffer source, byte[] dest, int destOffset) {
        while (source.hasRemaining()) {
            dest[destOffset++] = source.get();
        }
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
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, Buffer ptr) {
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, (FloatBuffer) ptr);

    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        org.lwjgl.opengles.GLES20.glEnableVertexAttribArray(index);
    }

    private FloatBuffer toFloatBuffer(float[] data, int length, int offset) {
        FloatBuffer fb = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(data, offset, length);
        return fb;
    }

    private IntBuffer toIntBuffer(int[] data, int length, int offset) {
        IntBuffer ib = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ib.put(data, offset, length);
        return ib;
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniformMatrix4fv(location, transpose, toFloatBuffer(v, count * 16, offset));
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniformMatrix3fv(location, transpose, toFloatBuffer(v, count * 12, offset));
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniformMatrix2fv(location, transpose, toFloatBuffer(v, count * 8, offset));
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
        IntBuffer ib = ByteBuffer.allocateDirect(textures.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        org.lwjgl.opengles.GLES20.glGenTextures(ib);
        toArray((IntBuffer) ib.rewind(), textures, 0);
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
        IntBuffer ib = ByteBuffer.allocateDirect(params.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        org.lwjgl.opengles.GLES20.glGetIntegerv(pname, ib);
        toArray((IntBuffer) ib.rewind(), params, 0);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniform4fv(location, toFloatBuffer(v, 4 * count, offset));
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniform3fv(location, toFloatBuffer(v, 3 * count, offset));
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v0, int offset) {
        org.lwjgl.opengles.GLES20.glUniform1iv(location, toIntBuffer(v0, count, offset));
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniform2fv(location, toFloatBuffer(v, 2 * count, offset));

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
        org.lwjgl.opengles.GLES20.glDeleteTextures(toIntBuffer(textures, textures.length, 0));
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

}
