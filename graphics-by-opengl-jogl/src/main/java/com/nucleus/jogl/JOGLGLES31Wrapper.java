package com.nucleus.jogl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4ES3;
import com.nucleus.opengl.GLES31Wrapper;

public class JOGLGLES31Wrapper extends GLES31Wrapper {

    private final static String GLES_NULL = "GLES31 is null";

    /**
     * Wrapper for gles20/30 methods that can be used if they are not a simple one liner.
     */
    private JOGLGLES30Wrapper gles30;
    private JOGLGLES20Wrapper gles20;
    GL4ES3 gles;

    /**
     * Creates a new instance of the GLES30 wrapper for JOGL
     * 
     * @param gles The JOGL GLES30 instance
     * @param renderVersion If higher than GLES31, otherwise null
     * @throws IllegalArgumentException If gles is null
     */
    public JOGLGLES31Wrapper(GL4ES3 gles, Renderers renderVersion) {
        super(Platform.GL, renderVersion);
        if (gles == null) {
            throw new IllegalArgumentException(GLES_NULL);
        }
        this.gles = gles;
        gles30 = new JOGLGLES30Wrapper(gles, renderVersion);
        gles20 = new JOGLGLES20Wrapper(gles, renderVersion);
    }

    /**
     * ---------------------------------------------------
     * GLES20 calls - just pass on to GLES20 wrapper if needed
     * ---------------------------------------------------
     */

    @Override
    public void glAttachShader(int program, int shader) {
        gles.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        gles.glLinkProgram(program);

    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        gles.glShaderSource(shader, 1, new String[] { shaderSource }, null);

    }

    @Override
    public void glCompileShader(int shader) {
        gles.glCompileShader(shader);
    }

    @Override
    public int glCreateShader(int type) {
        return gles.glCreateShader(type);
    }

    @Override
    public int glCreateProgram() {
        return gles.glCreateProgram();
    }

    @Override
    public void glDeleteProgram(int program) {
        gles.glDeleteProgram(program);
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        gles.glGetShaderiv(shader, pname, params);

    }

    @Override
    public void glUseProgram(int program) {
        gles.glUseProgram(program);

    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        gles.glGetProgramiv(program, pname, params, offset);

    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        gles.glGetActiveAttrib(program, index, name.length, length, lengthOffset, size, sizeOffset, type, typeOffset,
                name, 0);

    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        gles.glGetActiveUniform(program, index, name.length, length, lengthOffset, size, sizeOffset, type, typeOffset,
                name, 0);

    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return gles.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return gles.glGetAttribLocation(program, name);
    }

    @Override
    public int glGetError() {
        return gles.glGetError();
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer ptr) {
        gles20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, FloatBuffer ptr) {
        gles20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        gles.glEnableVertexAttribArray(index);
    }

    @Override
    public void glDisableVertexAttribArray(int index) {
        gles.glDisableVertexAttribArray(index);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        gles.glDrawArrays(mode, first, count);

    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        gles30.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        gles.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        gles.glBindAttribLocation(program, index, name);

    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        gles.glViewport(x, y, width, height);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return gles30.glGetShaderInfoLog(shader);
    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return gles30.glGetProgramInfoLog(program);
    }

    @Override
    public void glGenTextures(int[] textures) {
        gles.glGenTextures(textures.length, textures, 0);

    }

    @Override
    public void glActiveTexture(int texture) {
        gles.glActiveTexture(texture);

    }

    @Override
    public void glBindTexture(int target, int texture) {
        gles.glBindTexture(target, texture);

    }

    @Override
    public String glGetString(int name) {
        return gles.glGetString(name);
    }

    @Override
    public void glGetIntegerv(int pname, int[] params) {
        gles.glGetIntegerv(pname, params, 0);

    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        gles.glUniformMatrix4fv(location, count, transpose, buffer);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        gles.glUniformMatrix3fv(location, count, transpose, buffer);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        gles.glUniformMatrix2fv(location, count, transpose, buffer);
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer buffer) {
        gles.glUniform4fv(location, count, buffer);
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer buffer) {
        gles.glUniform3fv(location, count, buffer);
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer buffer) {
        gles.glUniform2fv(location, count, buffer);

    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer buffer) {
        gles.glUniform1fv(location, count, buffer);
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer buffer) {
        gles.glUniform1iv(location, count, buffer);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        gles.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        gles.glTexParameteri(target, pname, param);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        gles.glClearColor(red, green, blue, alpha);

    }

    @Override
    public void glClear(int mask) {
        gles.glClear(mask);

    }

    @Override
    public void glDisable(int cap) {
        gles.glDisable(cap);

    }

    @Override
    public void glEnable(int cap) {
        gles.glEnable(cap);

    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        gles.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);

    }

    @Override
    public void glDeleteTextures(int[] textures) {
        gles.glDeleteTextures(textures.length, textures, 0);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        gles.glBlendEquationSeparate(modeRGB, modeAlpha);

    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        gles.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);

    }

    @Override
    public void glGenBuffers(int[] buffers) {
        gles.glGenBuffers(buffers.length, buffers, 0);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        gles.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        gles.glBufferData(target, size, data, usage);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        gles.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        gles.glDeleteBuffers(n, buffers, offset);

    }

    @Override
    public void glGenerateMipmap(int target) {
        gles.glGenerateMipmap(target);
    }

    @Override
    public void glCullFace(int mode) {
        gles.glCullFace(mode);
    }

    @Override
    public void glDepthFunc(int func) {
        gles.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        gles.glDepthMask(flag);
    }

    @Override
    public void glClearDepthf(float depth) {
        gles.glClearDepthf(depth);
    }

    @Override
    public void glDepthRangef(float nearVal, float farVal) {
        gles.glDepthRangef(nearVal, farVal);
    }

    @Override
    public void glFinish() {
        gles.glFinish();
    }

    @Override
    public void glLineWidth(float width) {
        gles.glLineWidth(width);
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        gles.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glGenFramebuffers(int[] buffers) {
        gles.glGenFramebuffers(buffers.length, buffers, 0);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return gles.glCheckFramebufferStatus(target);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        gles.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        gles.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {
        gles.glSamplerParameteri(sampler, pname, param);
    }

    @Override
    public void glValidateProgram(int program) {
        gles.glValidateProgram(program);
    }

    /**
     * 
     * ---------------------------------------------------
     * GLES30 calls
     * ---------------------------------------------------
     * 
     */

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source) {
        gles.glGetShaderSource(shader, bufsize, length, 0, source, 0);
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {
        gles.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        gles.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, int ptroffset, int ptrsize) {
        gles.glBindBufferRange(target, index, buffer, ptroffset, ptrsize);
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {
        return gles.glGetUniformBlockIndex(program, uniformBlockName);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params) {
        gles.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return gles30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset,
            int pname, int[] params, int paramsOffset) {
        gles.glGetActiveUniformsiv(program, uniformCount, uniformIndices, indicesOffset, pname, params,
                paramsOffset);
    }

    @Override
    public ByteBuffer glMapBufferRange(int target, int offset, int length, int access) {
        return gles.glMapBufferRange(target, offset, length, access);
    }

    @Override
    public boolean glUnmapBuffer(int target) {
        return gles.glUnmapBuffer(target);
    }

    @Override
    public void glFlushMappedBufferRange(int target, int offset, int length) {
        gles.glFlushMappedBufferRange(target, offset, length);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {
        gles.glDrawRangeElements(mode, start, end, count, type, offset);

    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        gles.glTexStorage2D(target, levels, internalformat, width, height);
    }

    /**
     * 
     * *******************************************************************
     * GLES31
     * *******************************************************************
     * 
     */

    @Override
    public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        gles.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
    }

    @Override
    public void glDispatchComputeIndirect(int offset) {
        gles.glDispatchComputeIndirect(offset);
    }

    @Override
    public void glDrawArraysIndirect(int mode, int offset) {
        gles.glDrawArraysIndirect(mode, offset);
    }

    @Override
    public void glDrawElementsIndirect(int mode, int type, int offset) {
        gles.glDrawElementsIndirect(mode, type, offset);
    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params) {
        gles.glGetFramebufferParameteriv(target, pname, params);
    }

    @Override
    public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
        gles.glGetProgramInterfaceiv(program, programInterface, pname, params);

    }

    @Override
    public int glGetProgramResourceIndex(int program, int programInterface, String name) {
        return gles.glGetProgramResourceIndex(program, programInterface, name.getBytes(), 0);
    }

    @Override
    public String glGetProgramResourceName(int program, int programInterface, int index) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, IntBuffer props,
            int bufSize, IntBuffer length, IntBuffer params) {
        gles.glGetProgramResourceiv(program, programInterface, index, propCount, props, bufSize, length, params);
    }

    @Override
    public int glGetProgramResourceLocation(int program, int programInterface, String name) {
        return gles.glGetProgramResourceLocation(program, programInterface, name.getBytes(), 0);
    }

    @Override
    public void glUseProgramStages(int pipeline, int stages, int program) {
        gles.glUseProgramStages(pipeline, stages, program);
    }

    @Override
    public void glActiveShaderProgram(int pipeline, int program) {
        gles.glActiveShaderProgram(pipeline, program);
    }

    @Override
    public int glCreateShaderProgramv(int type, String[] strings) {
        return gles.glCreateShaderProgramv(type, strings.length, strings);
    }

    @Override
    public void glBindProgramPipeline(int pipeline) {
        gles.glBindProgramPipeline(pipeline);
    }

    @Override
    public void glDeleteProgramPipelines(int n, IntBuffer pipelines) {
        gles.glDeleteProgramPipelines(n, pipelines);
    }

    @Override
    public void glGenProgramPipelines(int n, IntBuffer pipelines) {
        gles.glGenProgramPipelines(n, pipelines);
    }

    @Override
    public boolean glIsProgramPipeline(int pipeline) {
        return gles.glIsProgramPipeline(pipeline);
    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params) {
        gles.glGetProgramPipelineiv(pipeline, pname, params);
    }

    @Override
    public void glProgramUniform1i(int program, int location, int v0) {
        gles.glProgramUniform1f(program, location, v0);
    }

    @Override
    public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
        gles.glProgramUniform4i(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
        gles.glProgramUniform4ui(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
        gles.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, IntBuffer value) {
        gles.glProgramUniform4iv(program, location, count, value);
    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, IntBuffer value) {
        gles.glProgramUniform4uiv(program, location, count, value);
    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, FloatBuffer value) {
        gles.glProgramUniform4fv(program, location, count, value);
    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        gles.glProgramUniformMatrix2fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        gles.glProgramUniformMatrix3fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        gles.glProgramUniformMatrix4fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        gles.glProgramUniformMatrix3x4fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        gles.glProgramUniformMatrix4x3fv(program, location, count, transpose, value);
    }

    @Override
    public void glValidateProgramPipeline(int pipeline) {
        gles.glValidateProgramPipeline(pipeline);
    }

    @Override
    public String glGetProgramPipelineInfoLog(int program) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
            int format) {
        gles.glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    @Override
    public void glGetBooleani_v(int target, int index, IntBuffer data) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glMemoryBarrier(int barriers) {
        gles.glMemoryBarrier(barriers);
    }

    @Override
    public void glMemoryBarrierByRegion(int barriers) {
        gles.glMemoryBarrierByRegion(barriers);
    }

    @Override
    public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations) {
        gles.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
    }

    @Override
    public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
        gles.glGetMultisamplefv(pname, index, val);
    }

    @Override
    public void glSampleMaski(int maskNumber, int mask) {
        gles.glSampleMaski(maskNumber, mask);
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params) {
        gles.glGetTexLevelParameteriv(target, level, pname, params);
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params) {
        gles.glGetTexLevelParameterfv(target, level, pname, params);
    }

    @Override
    public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        gles.glBindVertexBuffer(bindingindex, buffer, offset, stride);
    }

    @Override
    public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        gles.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
    }

    @Override
    public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        gles.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
    }

    @Override
    public void glVertexAttribBinding(int attribindex, int bindingindex) {
        gles.glVertexAttribBinding(attribindex, bindingindex);
    }

    @Override
    public void glVertexBindingDivisor(int bindingindex, int divisor) {
        gles.glVertexBindingDivisor(bindingindex, divisor);
    }

}
