package com.nucleus.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES20Wrapper;

public class LWJGL3GLES20Wrapper extends GLES20Wrapper {

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
    public void glGenBuffers(int n, int[] buffers, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glGenBuffers(buffers);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glDeleteBuffers(buffers);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        org.lwjgl.opengles.GLES20.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        org.lwjgl.opengles.GLES20.glBufferData(target, (ByteBuffer) data, usage);
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
        // TODO handle offset
        org.lwjgl.opengles.GLES20.glGetProgramiv(program, pname, params);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int nameSize, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
        throw new IllegalArgumentException();
    }

    @Override
    public void glGetActiveUniform(int program, int index, int nameSize, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
        throw new IllegalArgumentException();
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
        // TODO Auto-generated method stub

    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        // TODO Auto-generated method stub

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
    public void glGenTextures(int count, int[] textures, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glGenTextures(textures);
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
        org.lwjgl.opengles.GLES20.glGetIntegerv(pname, params);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glUniform4fv(location, v);
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glUniform3fv(location, v);
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glUniform2fv(location, v);
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
    public void glDeleteTextures(int count, int[] textures, int offset) {
        // TODO handle count and offset
        org.lwjgl.opengles.GLES20.glDeleteTextures(textures);
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

}
