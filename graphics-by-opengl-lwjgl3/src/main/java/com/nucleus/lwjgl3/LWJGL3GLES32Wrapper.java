package com.nucleus.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES32Wrapper;

public class LWJGL3GLES32Wrapper extends GLES32Wrapper {

    LWJGL3GLES20Wrapper gles20;
    LWJGL3GLES30Wrapper gles30;

    /**
     * Implementation constructor - DO NOT USE - fetch from {@link LWJGLWrapperFactory}
     * 
     * @param renderVersion If higher than GLES30, otherwise null
     * 
     */
    protected LWJGL3GLES32Wrapper(Renderers version) {
        super(Platform.GL, version);
        gles20 = new LWJGL3GLES20Wrapper(version);
        gles30 = new LWJGL3GLES30Wrapper(version);
    }

    /**
     * ---------------------------------------------------
     * GLES20 calls - just pass on to GLES20 wrapper unless simple oneliner
     * ---------------------------------------------------
     */

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
        gles20.glGenBuffers(buffers);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        gles20.glDeleteBuffers(n, buffers, offset);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        org.lwjgl.opengles.GLES20.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        gles20.glBufferData(target, size, data, usage);
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
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, FloatBuffer ptr) {
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer ptr) {
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
        gles20.glGenTextures(textures);
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
        gles20.glGetIntegerv(pname, params);
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v0, int offset) {
        org.lwjgl.opengles.GLES20.glUniform1iv(location, LWJGLUtils.toIntBuffer(v0, count, offset));
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
        gles20.glGetShaderSource(shader, bufsize, length, source);
    }

    /**
     * 
     * -----------------------------------------------------------------------------
     * GLES30 methods - just pass on to gles30 wrapper if not simple oneliner
     * -----------------------------------------------------------------------------
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
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer buffer) {
        gles30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, buffer);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return org.lwjgl.opengles.GLES30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset,
            int pname, int[] params, int paramsOffset) {
        gles30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, indicesOffset, pname, params, paramsOffset);
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

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {
        org.lwjgl.opengles.GLES30.glDrawRangeElements(mode, start, end, count, type, offset);
    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        org.lwjgl.opengles.GLES30.glTexStorage2D(target, levels, internalformat, width, height);
    }

    /**
     * 
     * -----------------------------------------------------------------------------
     * GLES31 methods, pass on to gles31 if not simple oneliner
     * -----------------------------------------------------------------------------
     * 
     */

    @Override
    public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        org.lwjgl.opengles.GLES31.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
    }

    @Override
    public void glDispatchComputeIndirect(int offset) {
        org.lwjgl.opengles.GLES31.glDispatchComputeIndirect(offset);
    }

    @Override
    public void glDrawArraysIndirect(int mode, int offset) {
        org.lwjgl.opengles.GLES31.glDrawArraysIndirect(mode, offset);
    }

    @Override
    public void glDrawElementsIndirect(int mode, int type, int offset) {
        org.lwjgl.opengles.GLES31.glDrawElementsIndirect(mode, type, offset);
    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params) {
        org.lwjgl.opengles.GLES31.glGetFramebufferParameteriv(target, pname, params);
    }

    @Override
    public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
        org.lwjgl.opengles.GLES31.glGetProgramInterfaceiv(program, programInterface, pname, params);
    }

    @Override
    public int glGetProgramResourceIndex(int program, int programInterface, String name) {
        return org.lwjgl.opengles.GLES31.glGetProgramResourceIndex(program, programInterface, name);
    }

    @Override
    public String glGetProgramResourceName(int program, int programInterface, int index) {
        return org.lwjgl.opengles.GLES31.glGetProgramResourceName(program, programInterface, index);
    }

    @Override
    public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, IntBuffer props,
            int bufSize, IntBuffer length, IntBuffer params) {
        org.lwjgl.opengles.GLES31.glGetProgramResourceiv(program, programInterface, index, props, length, params);
    }

    @Override
    public int glGetProgramResourceLocation(int program, int programInterface, String name) {
        return org.lwjgl.opengles.GLES31.glGetProgramResourceLocation(program, programInterface, name);
    }

    @Override
    public void glUseProgramStages(int pipeline, int stages, int program) {
        org.lwjgl.opengles.GLES31.glUseProgramStages(pipeline, stages, program);
    }

    @Override
    public void glActiveShaderProgram(int pipeline, int program) {
        org.lwjgl.opengles.GLES31.glActiveShaderProgram(pipeline, program);
    }

    @Override
    public int glCreateShaderProgramv(int type, String[] strings) {
        return org.lwjgl.opengles.GLES31.glCreateShaderProgramv(type, strings);
    }

    @Override
    public void glBindProgramPipeline(int pipeline) {
        org.lwjgl.opengles.GLES31.glBindProgramPipeline(pipeline);
    }

    @Override
    public void glDeleteProgramPipelines(int n, IntBuffer pipelines) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glGenProgramPipelines(int n, IntBuffer pipelines) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public boolean glIsProgramPipeline(int pipeline) {
        return org.lwjgl.opengles.GLES31.glIsProgramPipeline(pipeline);
    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params) {
        org.lwjgl.opengles.GLES31.glGetProgramPipelineiv(pipeline, pname, params);
    }

    @Override
    public void glProgramUniform1i(int program, int location, int v0) {
        org.lwjgl.opengles.GLES31.glProgramUniform1i(program, location, v0);
    }

    @Override
    public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengles.GLES31.glProgramUniform4i(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengles.GLES31.glProgramUniform4ui(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengles.GLES31.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, IntBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniform4iv(program, location, value);
    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, IntBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniform4uiv(program, location, value);
    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, FloatBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniform4fv(program, location, value);
    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniformMatrix2fv(program, location, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniformMatrix3fv(program, location, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniformMatrix4fv(program, location, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniformMatrix3x4fv(program, location, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        org.lwjgl.opengles.GLES31.glProgramUniformMatrix4x3fv(program, location, transpose, value);
    }

    @Override
    public void glValidateProgramPipeline(int pipeline) {
        org.lwjgl.opengles.GLES31.glValidateProgramPipeline(pipeline);
    }

    @Override
    public String glGetProgramPipelineInfoLog(int program) {
        return org.lwjgl.opengles.GLES31.glGetProgramPipelineInfoLog(program);
    }

    @Override
    public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
            int format) {
        org.lwjgl.opengles.GLES31.glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    @Override
    public void glGetBooleani_v(int target, int index, IntBuffer data) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glMemoryBarrier(int barriers) {
        org.lwjgl.opengles.GLES31.glMemoryBarrier(barriers);
    }

    @Override
    public void glMemoryBarrierByRegion(int barriers) {
        org.lwjgl.opengles.GLES31.glMemoryBarrierByRegion(barriers);
    }

    @Override
    public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations) {
        org.lwjgl.opengles.GLES31.glTexStorage2DMultisample(target, samples, internalformat, width, height,
                fixedsamplelocations);
    }

    @Override
    public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
        org.lwjgl.opengles.GLES31.glGetMultisamplefv(pname, index, val);
    }

    @Override
    public void glSampleMaski(int maskNumber, int mask) {
        org.lwjgl.opengles.GLES31.glSampleMaski(maskNumber, maskNumber);
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params) {
        org.lwjgl.opengles.GLES31.glGetTexLevelParameteriv(target, level, pname, params);
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params) {
        org.lwjgl.opengles.GLES31.glGetTexLevelParameterfv(target, level, pname, params);
    }

    @Override
    public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        org.lwjgl.opengles.GLES31.glBindVertexBuffer(bindingindex, buffer, offset, stride);
    }

    @Override
    public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        org.lwjgl.opengles.GLES31.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
    }

    @Override
    public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        org.lwjgl.opengles.GLES31.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
    }

    @Override
    public void glVertexAttribBinding(int attribindex, int bindingindex) {
        org.lwjgl.opengles.GLES31.glVertexAttribBinding(attribindex, bindingindex);
    }

    @Override
    public void glVertexBindingDivisor(int bindingindex, int divisor) {
        org.lwjgl.opengles.GLES31.glVertexBindingDivisor(bindingindex, divisor);
    }

    /**
     * 
     * ****************************************************************************************************
     * GLES 32
     * ****************************************************************************************************
     * 
     */

    @Override
    public void glDrawElementsBaseVertex(int mode, int count, int type, ByteBuffer indices, int basevertex) {
    }

    @Override
    public void glDrawRangeElementsBaseVertex(int mode, int start, int end, int count, int type, ByteBuffer indices,
            int basevertex) {

    }

    @Override
    public void glDrawElementsInstancedBaseVertex(int mode, int count, int type, ByteBuffer indices, int instancecount,
            int basevertex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glFramebufferTexture(int target, int attachment, int texture, int level) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glPrimitiveBoundingBox(float minX, float minY, float minZ, float minW, float maxX, float maxY,
            float maxZ, float maxW) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glGetGraphicsResetStatus() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glMinSampleShading(float value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glPatchParameteri(int pname, int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexParameterIiv(int target, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexParameterIuiv(int target, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glSamplerParameterIiv(int sampler, int pname, IntBuffer param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glSamplerParameterIuiv(int sampler, int pname, IntBuffer param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexBuffer(int target, int internalformat, int buffer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexBufferRange(int target, int internalformat, int buffer, int offset, int size) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth,
            boolean fixedsamplelocations) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendBarrier() {
        // TODO Auto-generated method stub

    }

    @Override
    public void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName,
            int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids, boolean enabled) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, int length, String buf) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glGetDebugMessageLog(int count, int bufSize, IntBuffer sources, IntBuffer types, IntBuffer ids,
            IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glPopDebugGroup() {
        // TODO Auto-generated method stub

    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, String label) {
        // TODO Auto-generated method stub

    }

    @Override
    public String glGetObjectLabel(int identifier, int name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glObjectPtrLabel(long ptr, String label) {
        // TODO Auto-generated method stub

    }

    @Override
    public String glGetObjectPtrLabel(long ptr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long glGetPointerv(int pname) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glEnablei(int target, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDisablei(int target, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendEquationi(int buf, int mode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendEquationSeparatei(int buf, int modeRGB, int modeAlpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendFunci(int buf, int src, int dst) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBlendFuncSeparatei(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glColorMaski(int index, boolean r, boolean g, boolean b, boolean a) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean glIsEnabledi(int target, int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, int bufSize, Buffer data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, FloatBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetSamplerParameterIiv(int sampler, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetSamplerParameterIuiv(int sampler, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

}
