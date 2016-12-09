package com.nucleus.android;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES20Wrapper;

public class AndroidGLES20Wrapper extends GLES20Wrapper {

    @Override
    public void glAttachShader(int program, int shader) {
        android.opengl.GLES20.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        android.opengl.GLES20.glLinkProgram(program);
    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        android.opengl.GLES20.glShaderSource(shader, shaderSource);
    }

    @Override
    public void glCompileShader(int shader) {
        android.opengl.GLES20.glCompileShader(shader);
    }

    @Override
    public int glCreateShader(int type) {
        return android.opengl.GLES20.glCreateShader(type);
    }

    @Override
    public int glCreateProgram() {
        return android.opengl.GLES20.glCreateProgram();
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        android.opengl.GLES20.glGetShaderiv(shader, pname, params);
    }

    @Override
    public int glGetError() {
        return android.opengl.GLES20.glGetError();
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        android.opengl.GLES20.glGetProgramiv(program, pname, params, offset);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int nameSize, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
        // Fix for some stupid devices that can't write into the same destination array.
        int[] l = new int[1];
        int[] t = new int[1];
        int[] s = new int[1];
        android.opengl.GLES20.glGetActiveAttrib(program, index, nameSize, l, 0, s, 0, t,
                0, name, nameOffset);
        length[lengthOffset] = l[0];
        type[typeOffset] = t[0];
        size[sizeOffset] = s[0];
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return android.opengl.GLES20.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return android.opengl.GLES20.glGetAttribLocation(program, name);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, Buffer ptr) {
        android.opengl.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        android.opengl.GLES20.glEnableVertexAttribArray(index);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        android.opengl.GLES20.glUniformMatrix4fv(location, count, transpose, v, offset);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset) {
        android.opengl.GLES20.glUniformMatrix3fv(location, count, transpose, v, offset);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset) {
        android.opengl.GLES20.glUniformMatrix2fv(location, count, transpose, v, offset);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        android.opengl.GLES20.glDrawArrays(mode, first, count);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        android.opengl.GLES20.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int nameSize, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
        // Fix for some stupid devices that can't write into the same destination array.
        int[] l = new int[1];
        int[] t = new int[1];
        int[] s = new int[1];
        android.opengl.GLES20.glGetActiveUniform(program, index, nameSize, l, 0, s, 0, t,
                0, name, nameOffset);
        length[lengthOffset] = l[0];
        type[typeOffset] = t[0];
        size[sizeOffset] = s[0];
    }

    @Override
    public void glUseProgram(int program) {
        android.opengl.GLES20.glUseProgram(program);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        android.opengl.GLES20.glViewport(x, y, width, height);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return android.opengl.GLES20.glGetShaderInfoLog(shader);

    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return android.opengl.GLES20.glGetProgramInfoLog(program);

    }

    @Override
    public void glGenTextures(int count, int[] textures, int offset) {
        android.opengl.GLES20.glGenTextures(count, textures, offset);
    }

    @Override
    public void glActiveTexture(int texture) {
        android.opengl.GLES20.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        android.opengl.GLES20.glBindTexture(target, texture);
    }

    @Override
    public String glGetString(int name) {
        return android.opengl.GLES20.glGetString(name);
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        android.opengl.GLES20.glUniform3fv(location, count, v, offset);
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        android.opengl.GLES20.glUniform2fv(location, count, v, offset);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        android.opengl.GLES20.glUniform4fv(location, count, v, offset);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        android.opengl.GLES20.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        android.opengl.GLES20.glTexParameteri(target, pname, param);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        android.opengl.GLES20.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClear(int mask) {
        android.opengl.GLES20.glClear(mask);
    }

    @Override
    public void glDisable(int cap) {
        android.opengl.GLES20.glDisable(cap);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        android.opengl.GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);

    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        android.opengl.GLES20.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        android.opengl.GLES20.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        android.opengl.GLES20.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        android.opengl.GLES20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glEnable(int cap) {
        android.opengl.GLES20.glEnable(cap);
    }

    @Override
    public void glGetIntegerv(int pname, int[] params, int offset) {
        android.opengl.GLES20.glGetIntegerv(pname, params, offset);
    }

    @Override
    public void glGenBuffers(int n, int[] buffers, int offset) {
        android.opengl.GLES20.glGenBuffers(n, buffers, offset);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        android.opengl.GLES20.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        android.opengl.GLES20.glBufferData(target, size, data, usage);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        android.opengl.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        android.opengl.GLES20.glDeleteBuffers(n, buffers, offset);
    }

    @Override
    public void glGenerateMipmap(int target) {
        android.opengl.GLES20.glGenerateMipmap(target);
    }

    @Override
    public void glCullFace(int mode) {
        android.opengl.GLES20.glCullFace(mode);
    }

    @Override
    public void glDepthFunc(int func) {
        android.opengl.GLES20.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        android.opengl.GLES20.glDepthMask(flag);
    }

    @Override
    public void glClearDepthf(float depth) {
        android.opengl.GLES20.glClearDepthf(depth);
    }

    @Override
    public void glDepthRangef(float nearVal, float farVal) {
        android.opengl.GLES20.glDepthRangef(nearVal, farVal);
    }

    @Override
    public void glFinish() {
        android.opengl.GLES20.glFinish();
    }
}

