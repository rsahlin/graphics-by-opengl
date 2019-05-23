package com.nucleus.jogl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4ES3;
import com.nucleus.opengl.GLES32Wrapper;
import com.nucleus.renderer.NucleusRenderer.Renderers;

public class JOGLGLES32Wrapper extends GLES32Wrapper {

    private final static String GLES_NULL = "GLES32 is null";

    /**
     * Wrapper for gles20/30 methods that can be used if they are not a simple one liner.
     */
    private JOGLGLES31Wrapper gles31;
    private JOGLGLES20Wrapper gles20;

    GL4ES3 gles;

    /**
     * Creates a new instance of the GLES30 wrapper for JOGL
     * 
     * @param gles The JOGL GLES30 instance
     * @param renderVersion If higher than GLES32, otherwise null
     * @throws IllegalArgumentException If gles is null
     */
    public JOGLGLES32Wrapper(GL4ES3 gles, Renderers renderVersion) {
        super(Platform.GL, renderVersion);
        if (gles == null) {
            throw new IllegalArgumentException(GLES_NULL);
        }
        this.gles = gles;
        gles31 = new JOGLGLES31Wrapper(gles, renderVersion);
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
        gles31.glDrawElements(mode, count, type, indices);
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
        return gles31.glGetShaderInfoLog(shader);
    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return gles31.glGetProgramInfoLog(program);
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
        return gles31.glGetActiveUniformBlockName(program, uniformBlockIndex);
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

    /**
     * 
     * ****************************************************************************************
     * GLES 32
     * ****************************************************************************************
     * 
     */

    @Override
    public void glDrawElementsBaseVertex(int mode, int count, int type, ByteBuffer indices, int basevertex) {
        gles.glDrawElementsBaseVertex(mode, count, type, indices, basevertex);
    }

    @Override
    public void glDrawRangeElementsBaseVertex(int mode, int start, int end, int count, int type, ByteBuffer indices,
            int basevertex) {
        gles.glDrawRangeElementsBaseVertex(mode, start, end, count, type, indices, basevertex);
    }

    @Override
    public void glDrawElementsInstancedBaseVertex(int mode, int count, int type, ByteBuffer indices, int instancecount,
            int basevertex) {
        gles.glDrawElementsInstancedBaseVertex(mode, count, type, indices, instancecount, basevertex);
    }

    @Override
    public void glFramebufferTexture(int target, int attachment, int texture, int level) {
        gles.glFramebufferTexture(target, attachment, texture, level);
    }

    @Override
    public void glPrimitiveBoundingBox(float minX, float minY, float minZ, float minW, float maxX, float maxY,
            float maxZ, float maxW) {
        gles.glPrimitiveBoundingBox(minX, minY, minZ, minW, maxX, maxY, maxZ, maxW);
    }

    @Override
    public int glGetGraphicsResetStatus() {
        return gles.glGetGraphicsResetStatus();
    }

    @Override
    public void glMinSampleShading(float value) {
        gles.glMinSampleShading(value);
    }

    @Override
    public void glPatchParameteri(int pname, int value) {
        gles.glPatchParameteri(pname, value);
    }

    @Override
    public void glTexParameterIiv(int target, int pname, IntBuffer params) {
        gles.glTexParameterIiv(target, pname, params);
    }

    @Override
    public void glTexParameterIuiv(int target, int pname, IntBuffer params) {
        gles.glTexParameterIuiv(target, pname, params);
    }

    @Override
    public void glSamplerParameterIiv(int sampler, int pname, IntBuffer param) {
        gles.glSamplerParameterIiv(sampler, pname, param);
    }

    @Override
    public void glSamplerParameterIuiv(int sampler, int pname, IntBuffer param) {
        gles.glSamplerParameterIuiv(sampler, pname, param);
    }

    @Override
    public void glTexBuffer(int target, int internalformat, int buffer) {
        gles.glTexBuffer(target, internalformat, buffer);
    }

    @Override
    public void glTexBufferRange(int target, int internalformat, int buffer, int offset, int size) {
        gles.glTexBufferRange(target, internalformat, buffer, offset, size);
    }

    @Override
    public void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth,
            boolean fixedsamplelocations) {
        gles.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
    }

    @Override
    public void glBlendBarrier() {
        gles.glBlendBarrier();
    }

    @Override
    public void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName,
            int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
        gles.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX,
                dstY, dstZ, srcWidth, srcHeight, srcDepth);
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids, boolean enabled) {
        gles.glDebugMessageControl(source, type, severity, count, ids, enabled);
    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, int length, String buf) {
        gles.glDebugMessageInsert(source, type, id, severity, length, buf);
    }

    @Override
    public int glGetDebugMessageLog(int count, int bufSize, IntBuffer sources, IntBuffer types, IntBuffer ids,
            IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, String message) {
        gles.glPushDebugGroup(source, id, length, message.getBytes(), 0);
    }

    @Override
    public void glPopDebugGroup() {
        gles.glPopDebugGroup();

    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, String label) {
        gles.glObjectLabel(identifier, name, length, label.getBytes(), 0);
    }

    @Override
    public String glGetObjectLabel(int identifier, int name) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glObjectPtrLabel(long ptr, String label) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public String glGetObjectPtrLabel(long ptr) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public long glGetPointerv(int pname) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void glEnablei(int target, int index) {
        gles.glEnablei(target, index);
    }

    @Override
    public void glDisablei(int target, int index) {
        gles.glDisablei(target, index);
    }

    @Override
    public void glBlendEquationi(int buf, int mode) {
        gles.glBlendEquationi(buf, mode);
    }

    @Override
    public void glBlendEquationSeparatei(int buf, int modeRGB, int modeAlpha) {
        gles.glBlendEquationSeparatei(buf, modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFunci(int buf, int src, int dst) {
        gles.glBlendFunci(buf, src, dst);
    }

    @Override
    public void glBlendFuncSeparatei(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        gles.glBlendFuncSeparatei(buf, srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glColorMaski(int index, boolean r, boolean g, boolean b, boolean a) {
        gles.glColorMaski(index, r, g, b, a);
    }

    @Override
    public boolean glIsEnabledi(int target, int index) {
        return gles.glIsEnabledi(target, index);
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, int bufSize, Buffer data) {
        gles.glReadnPixels(x, y, width, height, format, type, bufSize, data);
    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, FloatBuffer params) {
        gles.glGetnUniformfv(program, location, bufSize, params);
    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, IntBuffer params) {
        gles.glGetnUniformiv(program, location, bufSize, params);
    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, IntBuffer params) {
        gles.glGetnUniformuiv(program, location, bufSize, params);
    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, IntBuffer params) {
        gles.glGetTexParameterIiv(target, pname, params);
    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, IntBuffer params) {
        gles.glGetTexParameterIuiv(target, pname, params);
    }

    @Override
    public void glGetSamplerParameterIiv(int sampler, int pname, IntBuffer params) {
        gles.glGetSamplerParameterIuiv(sampler, pname, params);
    }

    @Override
    public void glGetSamplerParameterIuiv(int sampler, int pname, IntBuffer params) {
        gles.glGetSamplerParameterIuiv(sampler, pname, params);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {
        gles.glDrawRangeElements(mode, start, end, count, type, offset);
    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        gles.glTexStorage2D(target, levels, internalformat, width, height);
    }

}
