package com.nucleus.android;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES30Wrapper;
import com.nucleus.renderer.NucleusRenderer;

public class AndriodGLES30Wrapper extends GLES30Wrapper {

    /**
     * Implementation constructor - DO NOT USE - fetch wrapper from {@link NucleusRenderer}
     */
    protected AndriodGLES30Wrapper() {
        super(Platform.GLES);
    }

    @Override
    public void glAttachShader(int program, int shader) {
        android.opengl.GLES30.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        android.opengl.GLES30.glLinkProgram(program);
    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        android.opengl.GLES30.glShaderSource(shader, shaderSource);
    }

    @Override
    public void glCompileShader(int shader) {
        android.opengl.GLES30.glCompileShader(shader);
    }

    @Override
    public int glCreateShader(int type) {
        return android.opengl.GLES30.glCreateShader(type);
    }

    @Override
    public int glCreateProgram() {
        return android.opengl.GLES30.glCreateProgram();
    }

    @Override
    public void glDeleteProgram(int program) {
        android.opengl.GLES30.glDeleteProgram(program);
    }

    @Override
    public void glDeleteTextures(int[] textures) {
        android.opengl.GLES30.glDeleteTextures(textures.length, textures, 0);
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        android.opengl.GLES30.glGetShaderiv(shader, pname, params);
    }

    @Override
    public int glGetError() {
        return android.opengl.GLES30.glGetError();
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        android.opengl.GLES30.glGetProgramiv(program, pname, params, offset);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        // Fix for some stupid devices that can't write into the same destination array.
        int[] l = new int[1];
        int[] t = new int[1];
        int[] s = new int[1];
        android.opengl.GLES30.glGetActiveAttrib(program, index, name.length, l, 0, s, 0, t,
                0, name, 0);
        length[lengthOffset] = l[0];
        type[typeOffset] = t[0];
        size[sizeOffset] = s[0];
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return android.opengl.GLES30.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return android.opengl.GLES30.glGetAttribLocation(program, name);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, Buffer ptr) {
        android.opengl.GLES30.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        android.opengl.GLES30.glEnableVertexAttribArray(index);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        android.opengl.GLES30.glUniformMatrix4fv(location, count, transpose, v, offset);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset) {
        android.opengl.GLES30.glUniformMatrix3fv(location, count, transpose, v, offset);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset) {
        android.opengl.GLES30.glUniformMatrix2fv(location, count, transpose, v, offset);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        android.opengl.GLES30.glDrawArrays(mode, first, count);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        android.opengl.GLES30.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        // Fix for some stupid devices that can't write into the same destination array.
        int[] l = new int[1];
        int[] t = new int[1];
        int[] s = new int[1];
        android.opengl.GLES30.glGetActiveUniform(program, index, name.length, l, 0, s, 0, t,
                0, name, 0);
        length[lengthOffset] = l[0];
        type[typeOffset] = t[0];
        size[sizeOffset] = s[0];
    }

    @Override
    public void glUseProgram(int program) {
        android.opengl.GLES30.glUseProgram(program);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        android.opengl.GLES30.glViewport(x, y, width, height);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return android.opengl.GLES30.glGetShaderInfoLog(shader);

    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return android.opengl.GLES30.glGetProgramInfoLog(program);

    }

    @Override
    public void glGenTextures(int[] textures) {
        android.opengl.GLES30.glGenTextures(textures.length, textures, 0);
    }

    @Override
    public void glActiveTexture(int texture) {
        android.opengl.GLES30.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        android.opengl.GLES30.glBindTexture(target, texture);
    }

    @Override
    public String glGetString(int name) {
        return android.opengl.GLES30.glGetString(name);
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        android.opengl.GLES30.glUniform3fv(location, count, v, offset);
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        android.opengl.GLES30.glUniform2fv(location, count, v, offset);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        android.opengl.GLES30.glUniform4fv(location, count, v, offset);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        android.opengl.GLES30.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        android.opengl.GLES30.glTexParameteri(target, pname, param);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        android.opengl.GLES30.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClear(int mask) {
        android.opengl.GLES30.glClear(mask);
    }

    @Override
    public void glDisable(int cap) {
        android.opengl.GLES30.glDisable(cap);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        android.opengl.GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        android.opengl.GLES30.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        android.opengl.GLES30.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        android.opengl.GLES30.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        android.opengl.GLES30.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glEnable(int cap) {
        android.opengl.GLES30.glEnable(cap);
    }

    @Override
    public void glGetIntegerv(int pname, int[] params) {
        android.opengl.GLES30.glGetIntegerv(pname, params, 0);
    }

    @Override
    public void glGenBuffers(int[] buffers) {
        android.opengl.GLES30.glGenBuffers(buffers.length, buffers, 0);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        android.opengl.GLES30.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        android.opengl.GLES30.glBufferData(target, size, data, usage);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        android.opengl.GLES30.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        android.opengl.GLES30.glDeleteBuffers(n, buffers, offset);
    }

    @Override
    public void glGenerateMipmap(int target) {
        android.opengl.GLES30.glGenerateMipmap(target);
    }

    @Override
    public void glCullFace(int mode) {
        android.opengl.GLES30.glCullFace(mode);
    }

    @Override
    public void glDepthFunc(int func) {
        android.opengl.GLES30.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        android.opengl.GLES30.glDepthMask(flag);
    }

    @Override
    public void glClearDepthf(float depth) {
        android.opengl.GLES30.glClearDepthf(depth);
    }

    @Override
    public void glDepthRangef(float nearVal, float farVal) {
        android.opengl.GLES30.glDepthRangef(nearVal, farVal);
    }

    @Override
    public void glFinish() {
        android.opengl.GLES30.glFinish();
    }

    @Override
    public void glLineWidth(float width) {
        android.opengl.GLES30.glLineWidth(width);
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        android.opengl.GLES30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glGenFramebuffers(int[] buffers) {
        android.opengl.GLES30.glGenFramebuffers(buffers.length, buffers, 0);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return android.opengl.GLES30.glCheckFramebufferStatus(target);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        android.opengl.GLES30.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        android.opengl.GLES30.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {
        android.opengl.GLES30.glSamplerParameteri(sampler, pname, param);

    }

    @Override
    public void glUniform1iv(int location, int count, int[] v0, int offset) {
        android.opengl.GLES30.glUniform1iv(location, count, v0, offset);
    }

    @Override
    public void glValidateProgram(int program) {
        android.opengl.GLES30.glValidateProgram(program);
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source) {
        android.opengl.GLES30.glGetShaderSource(shader, bufsize, length, 0, source, 0);
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {
        android.opengl.GLES30.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        android.opengl.GLES30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, int offset, int size) {
        android.opengl.GLES30.glBindBufferRange(target, index, buffer, offset, size);
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {
        return android.opengl.GLES30.glGetUniformBlockIndex(program, uniformBlockName);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {
        android.opengl.GLES30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params, offset);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return android.opengl.GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset, int pname,
            int[] params, int paramsOffset) {
        android.opengl.GLES30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, indicesOffset, pname,
                params, paramsOffset);
    }

}
