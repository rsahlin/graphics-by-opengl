package com.nucleus.android;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES32Wrapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Renderers;

public class AndroidGLES32Wrapper extends GLES32Wrapper {

    /**
     * Implementation constructor - DO NOT USE - fetch wrapper from {@link NucleusRenderer}
     */
    protected AndroidGLES32Wrapper() {
        super(Platform.GLES, Renderers.GLES32);
    }

    @Override
    public void glAttachShader(int program, int shader) {
        android.opengl.GLES32.glAttachShader(program, shader);
    }

    @Override
    public void glLinkProgram(int program) {
        android.opengl.GLES32.glLinkProgram(program);
    }

    @Override
    public void glShaderSource(int shader, String shaderSource) {
        android.opengl.GLES32.glShaderSource(shader, shaderSource);
    }

    @Override
    public void glCompileShader(int shader) {
        android.opengl.GLES32.glCompileShader(shader);
    }

    @Override
    public int glCreateShader(int type) {
        return android.opengl.GLES32.glCreateShader(type);
    }

    @Override
    public int glCreateProgram() {
        return android.opengl.GLES32.glCreateProgram();
    }

    @Override
    public void glDeleteProgram(int program) {
        android.opengl.GLES32.glDeleteProgram(program);
    }

    @Override
    public void glDeleteTextures(int[] textures) {
        android.opengl.GLES32.glDeleteTextures(textures.length, textures, 0);
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        android.opengl.GLES32.glGetShaderiv(shader, pname, params);
    }

    @Override
    public int glGetError() {
        return android.opengl.GLES32.glGetError();
    }

    @Override
    public void glGetProgramiv(int program, int pname, int[] params, int offset) {
        android.opengl.GLES32.glGetProgramiv(program, pname, params, offset);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        // Fix for some stupid devices that can't write into the same destination array.
        int[] l = new int[1];
        int[] t = new int[1];
        int[] s = new int[1];
        android.opengl.GLES32.glGetActiveAttrib(program, index, name.length, l, 0, s, 0, t,
                0, name, 0);
        length[lengthOffset] = l[0];
        type[typeOffset] = t[0];
        size[sizeOffset] = s[0];
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return android.opengl.GLES32.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return android.opengl.GLES32.glGetAttribLocation(program, name);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer ptr) {
        android.opengl.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, FloatBuffer ptr) {
        android.opengl.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        android.opengl.GLES32.glEnableVertexAttribArray(index);
    }

    @Override
    public void glDisableVertexAttribArray(int index) {
        android.opengl.GLES20.glDisableVertexAttribArray(index);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        android.opengl.GLES32.glDrawArrays(mode, first, count);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        android.opengl.GLES32.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        // Fix for some stupid devices that can't write into the same destination array.
        int[] l = new int[1];
        int[] t = new int[1];
        int[] s = new int[1];
        android.opengl.GLES32.glGetActiveUniform(program, index, name.length, l, 0, s, 0, t,
                0, name, 0);
        length[lengthOffset] = l[0];
        type[typeOffset] = t[0];
        size[sizeOffset] = s[0];
    }

    @Override
    public void glUseProgram(int program) {
        android.opengl.GLES32.glUseProgram(program);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        android.opengl.GLES32.glViewport(x, y, width, height);
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return android.opengl.GLES32.glGetShaderInfoLog(shader);

    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return android.opengl.GLES32.glGetProgramInfoLog(program);

    }

    @Override
    public void glGenTextures(int[] textures) {
        android.opengl.GLES32.glGenTextures(textures.length, textures, 0);
    }

    @Override
    public void glActiveTexture(int texture) {
        android.opengl.GLES32.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        android.opengl.GLES32.glBindTexture(target, texture);
    }

    @Override
    public String glGetString(int name) {
        return android.opengl.GLES32.glGetString(name);
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer buffer) {
        android.opengl.GLES20.glUniform3fv(location, count, buffer);
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer buffer) {
        android.opengl.GLES20.glUniform2fv(location, count, buffer);
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer buffer) {
        android.opengl.GLES20.glUniform1fv(location, count, buffer);
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer buffer) {
        android.opengl.GLES20.glUniform4fv(location, count, buffer);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        android.opengl.GLES20.glUniformMatrix4fv(location, count, transpose, buffer);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        android.opengl.GLES20.glUniformMatrix3fv(location, count, transpose, buffer);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer buffer) {
        android.opengl.GLES20.glUniformMatrix2fv(location, count, transpose, buffer);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        android.opengl.GLES32.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        android.opengl.GLES32.glTexParameteri(target, pname, param);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        android.opengl.GLES32.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClear(int mask) {
        android.opengl.GLES32.glClear(mask);
    }

    @Override
    public void glDisable(int cap) {
        android.opengl.GLES32.glDisable(cap);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
            int type, Buffer pixels) {
        android.opengl.GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        android.opengl.GLES32.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset) {
        android.opengl.GLES32.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        android.opengl.GLES32.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        android.opengl.GLES32.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glEnable(int cap) {
        android.opengl.GLES32.glEnable(cap);
    }

    @Override
    public void glGetIntegerv(int pname, int[] params) {
        android.opengl.GLES32.glGetIntegerv(pname, params, 0);
    }

    @Override
    public void glGenBuffers(int[] buffers) {
        android.opengl.GLES32.glGenBuffers(buffers.length, buffers, 0);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        android.opengl.GLES32.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        android.opengl.GLES32.glBufferData(target, size, data, usage);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        android.opengl.GLES32.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        android.opengl.GLES32.glDeleteBuffers(n, buffers, offset);
    }

    @Override
    public void glGenerateMipmap(int target) {
        android.opengl.GLES32.glGenerateMipmap(target);
    }

    @Override
    public void glCullFace(int mode) {
        android.opengl.GLES32.glCullFace(mode);
    }

    @Override
    public void glDepthFunc(int func) {
        android.opengl.GLES32.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        android.opengl.GLES32.glDepthMask(flag);
    }

    @Override
    public void glClearDepthf(float depth) {
        android.opengl.GLES32.glClearDepthf(depth);
    }

    @Override
    public void glDepthRangef(float nearVal, float farVal) {
        android.opengl.GLES32.glDepthRangef(nearVal, farVal);
    }

    @Override
    public void glFinish() {
        android.opengl.GLES32.glFinish();
    }

    @Override
    public void glLineWidth(float width) {
        android.opengl.GLES32.glLineWidth(width);
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        android.opengl.GLES32.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glGenFramebuffers(int[] buffers) {
        android.opengl.GLES32.glGenFramebuffers(buffers.length, buffers, 0);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return android.opengl.GLES32.glCheckFramebufferStatus(target);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        android.opengl.GLES32.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        android.opengl.GLES32.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {
        android.opengl.GLES32.glSamplerParameteri(sampler, pname, param);

    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer buffer) {
        android.opengl.GLES20.glUniform1iv(location, count, buffer);
    }

    @Override
    public void glValidateProgram(int program) {
        android.opengl.GLES32.glValidateProgram(program);
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source) {
        android.opengl.GLES32.glGetShaderSource(shader, bufsize, length, 0, source, 0);
    }

    /**
     * **************************************************************************************************
     * GLES30
     * **************************************************************************************************
     */

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {
        android.opengl.GLES32.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        android.opengl.GLES32.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, int offset, int size) {
        android.opengl.GLES32.glBindBufferRange(target, index, buffer, offset, size);
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {
        return android.opengl.GLES32.glGetUniformBlockIndex(program, uniformBlockName);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params) {
        android.opengl.GLES32.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return android.opengl.GLES32.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset, int pname,
            int[] params, int paramsOffset) {
        android.opengl.GLES32.glGetActiveUniformsiv(program, uniformCount, uniformIndices, indicesOffset, pname,
                params, paramsOffset);
    }

    @Override
    public ByteBuffer glMapBufferRange(int target, int offset, int length, int access) {
        return (ByteBuffer) android.opengl.GLES32.glMapBufferRange(target, offset, length, access);
    }

    @Override
    public boolean glUnmapBuffer(int target) {
        return android.opengl.GLES32.glUnmapBuffer(target);
    }

    @Override
    public void glFlushMappedBufferRange(int target, int offset, int length) {
        android.opengl.GLES32.glFlushMappedBufferRange(target, offset, length);
    }

    /**
     * **************************************************************************************************
     * GLES31
     * **************************************************************************************************
     */

    @Override
    public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        android.opengl.GLES31.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);

    }

    @Override
    public void glDispatchComputeIndirect(int offset) {
        android.opengl.GLES31.glDispatchComputeIndirect(offset);
    }

    @Override
    public void glDrawArraysIndirect(int mode, int offset) {
        android.opengl.GLES31.glDrawArraysIndirect(mode, offset);
    }

    @Override
    public void glDrawElementsIndirect(int mode, int type, int offset) {
        android.opengl.GLES31.glDrawElementsIndirect(mode, type, offset);
    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params) {
        android.opengl.GLES31.glGetFramebufferParameteriv(target, pname, params);
    }

    @Override
    public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
        android.opengl.GLES31.glGetProgramInterfaceiv(program, programInterface, pname, params);
    }

    @Override
    public int glGetProgramResourceIndex(int program, int programInterface, String name) {
        return android.opengl.GLES31.glGetProgramResourceIndex(program, programInterface, name);
    }

    @Override
    public String glGetProgramResourceName(int program, int programInterface, int index) {
        return android.opengl.GLES31.glGetProgramResourceName(program, programInterface, index);
    }

    @Override
    public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, IntBuffer props,
            int bufSize, IntBuffer length, IntBuffer params) {
        android.opengl.GLES31.glGetProgramResourceiv(program, programInterface, index, propCount, props, bufSize,
                length, params);
    }

    @Override
    public int glGetProgramResourceLocation(int program, int programInterface, String name) {
        return android.opengl.GLES31.glGetProgramResourceLocation(program, programInterface, name);
    }

    @Override
    public void glUseProgramStages(int pipeline, int stages, int program) {
        android.opengl.GLES31.glUseProgramStages(pipeline, stages, program);
    }

    @Override
    public void glActiveShaderProgram(int pipeline, int program) {
        android.opengl.GLES31.glActiveShaderProgram(pipeline, program);
    }

    @Override
    public int glCreateShaderProgramv(int type, String[] strings) {
        return android.opengl.GLES31.glCreateShaderProgramv(type, strings);
    }

    @Override
    public void glBindProgramPipeline(int pipeline) {
        android.opengl.GLES31.glBindProgramPipeline(pipeline);
    }

    @Override
    public void glDeleteProgramPipelines(int n, IntBuffer pipelines) {
        android.opengl.GLES31.glDeleteProgramPipelines(n, pipelines);
    }

    @Override
    public void glGenProgramPipelines(int n, IntBuffer pipelines) {
        android.opengl.GLES31.glGenProgramPipelines(n, pipelines);
    }

    @Override
    public boolean glIsProgramPipeline(int pipeline) {
        return android.opengl.GLES31.glIsProgramPipeline(pipeline);
    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params) {
        android.opengl.GLES31.glGetProgramPipelineiv(pipeline, pname, params);
    }

    @Override
    public void glProgramUniform1i(int program, int location, int v0) {
        android.opengl.GLES31.glProgramUniform1f(program, location, v0);
    }

    @Override
    public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
        android.opengl.GLES31.glProgramUniform4i(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
        android.opengl.GLES31.glProgramUniform4ui(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
        android.opengl.GLES31.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, IntBuffer value) {
        android.opengl.GLES31.glProgramUniform4iv(program, location, count, value);
    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, IntBuffer value) {
        android.opengl.GLES31.glProgramUniform4uiv(program, location, count, value);
    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, FloatBuffer value) {
        android.opengl.GLES31.glProgramUniform4fv(program, location, count, value);
    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        android.opengl.GLES31.glProgramUniformMatrix2fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        android.opengl.GLES31.glProgramUniformMatrix3fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        android.opengl.GLES31.glProgramUniformMatrix4fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        android.opengl.GLES31.glProgramUniformMatrix3x4fv(program, location, count, transpose, value);
    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        android.opengl.GLES31.glProgramUniformMatrix4x3fv(program, location, count, transpose, value);
    }

    @Override
    public void glValidateProgramPipeline(int pipeline) {
        android.opengl.GLES31.glValidateProgramPipeline(pipeline);
    }

    @Override
    public String glGetProgramPipelineInfoLog(int program) {
        return android.opengl.GLES31.glGetProgramPipelineInfoLog(program);
    }

    @Override
    public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
            int format) {
        android.opengl.GLES31.glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    @Override
    public void glGetBooleani_v(int target, int index, IntBuffer data) {
        android.opengl.GLES31.glGetBooleani_v(target, index, data);
    }

    @Override
    public void glMemoryBarrier(int barriers) {
        android.opengl.GLES31.glMemoryBarrier(barriers);
    }

    @Override
    public void glMemoryBarrierByRegion(int barriers) {
        android.opengl.GLES31.glMemoryBarrierByRegion(barriers);
    }

    @Override
    public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations) {
        android.opengl.GLES31.glTexStorage2DMultisample(target, samples, internalformat, width, height,
                fixedsamplelocations);
    }

    @Override
    public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
        android.opengl.GLES31.glGetMultisamplefv(pname, index, val);
    }

    @Override
    public void glSampleMaski(int maskNumber, int mask) {
        android.opengl.GLES31.glSampleMaski(maskNumber, mask);
    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params) {
        android.opengl.GLES31.glGetTexLevelParameteriv(target, level, pname, params);
    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params) {
        android.opengl.GLES31.glGetTexLevelParameterfv(target, level, pname, params);
    }

    @Override
    public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        android.opengl.GLES31.glBindVertexBuffer(bindingindex, buffer, offset, stride);
    }

    @Override
    public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        android.opengl.GLES31.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
    }

    @Override
    public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        android.opengl.GLES31.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
    }

    @Override
    public void glVertexAttribBinding(int attribindex, int bindingindex) {
        android.opengl.GLES31.glVertexAttribBinding(attribindex, bindingindex);
    }

    @Override
    public void glVertexBindingDivisor(int bindingindex, int divisor) {
        android.opengl.GLES31.glVertexBindingDivisor(bindingindex, divisor);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {
        android.opengl.GLES31.glDrawRangeElements(mode, start, end, count, type, offset);
    }

    @Override
    public void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        android.opengl.GLES31.glTexStorage2D(target, levels, internalformat, width, height);
    }

    /**
     * **************************************************************************************************
     * GLES32
     * **************************************************************************************************
     */

    @Override
    public void glDrawElementsBaseVertex(int mode, int count, int type, ByteBuffer indices, int basevertex) {
        android.opengl.GLES32.glDrawElementsBaseVertex(mode, count, type, indices, basevertex);
    }

    @Override
    public void glDrawRangeElementsBaseVertex(int mode, int start, int end, int count, int type, ByteBuffer indices,
            int basevertex) {
        android.opengl.GLES32.glDrawRangeElementsBaseVertex(mode, start, end, count, type, indices, basevertex);
    }

    @Override
    public void glDrawElementsInstancedBaseVertex(int mode, int count, int type, ByteBuffer indices, int instancecount,
            int basevertex) {
        android.opengl.GLES32.glDrawElementsInstancedBaseVertex(mode, count, type, indices, instancecount, basevertex);
    }

    @Override
    public void glFramebufferTexture(int target, int attachment, int texture, int level) {
        android.opengl.GLES32.glFramebufferTexture(target, attachment, texture, level);
    }

    @Override
    public void glPrimitiveBoundingBox(float minX, float minY, float minZ, float minW, float maxX, float maxY,
            float maxZ, float maxW) {
        android.opengl.GLES32.glPrimitiveBoundingBox(minX, minY, minZ, minW, maxX, maxY, maxZ, maxW);
    }

    @Override
    public int glGetGraphicsResetStatus() {
        return android.opengl.GLES32.glGetGraphicsResetStatus();
    }

    @Override
    public void glMinSampleShading(float value) {
        android.opengl.GLES32.glMinSampleShading(value);
    }

    @Override
    public void glPatchParameteri(int pname, int value) {
        android.opengl.GLES32.glPatchParameteri(pname, value);
    }

    @Override
    public void glTexParameterIiv(int target, int pname, IntBuffer params) {
        android.opengl.GLES32.glTexParameterIiv(target, pname, params);
    }

    @Override
    public void glTexParameterIuiv(int target, int pname, IntBuffer params) {
        android.opengl.GLES32.glTexParameterIuiv(target, pname, params);
    }

    @Override
    public void glSamplerParameterIiv(int sampler, int pname, IntBuffer param) {
        android.opengl.GLES32.glSamplerParameterIiv(sampler, pname, param);
    }

    @Override
    public void glSamplerParameterIuiv(int sampler, int pname, IntBuffer param) {
        android.opengl.GLES32.glSamplerParameterIuiv(sampler, pname, param);
    }

    @Override
    public void glTexBuffer(int target, int internalformat, int buffer) {
        android.opengl.GLES32.glTexBuffer(target, internalformat, buffer);
    }

    @Override
    public void glTexBufferRange(int target, int internalformat, int buffer, int offset, int size) {
        android.opengl.GLES32.glTexBufferRange(target, internalformat, buffer, offset, size);
    }

    @Override
    public void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth,
            boolean fixedsamplelocations) {
        android.opengl.GLES32.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth,
                fixedsamplelocations);
    }

    @Override
    public void glBlendBarrier() {
        android.opengl.GLES32.glBlendBarrier();
    }

    @Override
    public void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName,
            int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
        android.opengl.GLES32.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget,
                dstLevel, dstX, dstY, dstZ, srcWidth, srcHeight, srcDepth);
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids, boolean enabled) {
        android.opengl.GLES32.glDebugMessageControl(source, type, severity, count, ids, enabled);
    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, int length, String buf) {
        android.opengl.GLES32.glDebugMessageInsert(source, type, id, severity, length, buf);
    }

    @Override
    public int glGetDebugMessageLog(int count, int bufSize, IntBuffer sources, IntBuffer types, IntBuffer ids,
            IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog) {
        return android.opengl.GLES32.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
    }

    @Override
    public void glPushDebugGroup(int source, int id, int length, String message) {
        android.opengl.GLES32.glPushDebugGroup(source, id, length, message);
    }

    @Override
    public void glPopDebugGroup() {
        android.opengl.GLES32.glPopDebugGroup();
    }

    @Override
    public void glObjectLabel(int identifier, int name, int length, String label) {
        android.opengl.GLES32.glObjectLabel(identifier, name, length, label);
    }

    @Override
    public String glGetObjectLabel(int identifier, int name) {
        return android.opengl.GLES32.glGetObjectLabel(identifier, name);
    }

    @Override
    public void glObjectPtrLabel(long ptr, String label) {
        android.opengl.GLES32.glObjectPtrLabel(ptr, label);
    }

    @Override
    public String glGetObjectPtrLabel(long ptr) {
        return android.opengl.GLES32.glGetObjectPtrLabel(ptr);
    }

    @Override
    public long glGetPointerv(int pname) {
        return android.opengl.GLES32.glGetPointerv(pname);
    }

    @Override
    public void glEnablei(int target, int index) {
        android.opengl.GLES32.glEnablei(target, index);
    }

    @Override
    public void glDisablei(int target, int index) {
        android.opengl.GLES32.glDisablei(target, index);
    }

    @Override
    public void glBlendEquationi(int buf, int mode) {
        android.opengl.GLES32.glBlendEquationi(buf, mode);
    }

    @Override
    public void glBlendEquationSeparatei(int buf, int modeRGB, int modeAlpha) {
        android.opengl.GLES32.glBlendEquationSeparatei(buf, modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFunci(int buf, int src, int dst) {
        android.opengl.GLES32.glBlendFunci(buf, src, dst);
    }

    @Override
    public void glBlendFuncSeparatei(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        android.opengl.GLES32.glBlendFuncSeparatei(buf, srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glColorMaski(int index, boolean r, boolean g, boolean b, boolean a) {
        android.opengl.GLES32.glColorMaski(index, r, g, b, a);
    }

    @Override
    public boolean glIsEnabledi(int target, int index) {
        return android.opengl.GLES32.glIsEnabledi(target, index);
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, int bufSize, Buffer data) {
        android.opengl.GLES32.glReadnPixels(x, y, width, height, format, type, bufSize, data);
    }

    @Override
    public void glGetnUniformfv(int program, int location, int bufSize, FloatBuffer params) {
        android.opengl.GLES32.glGetnUniformfv(program, location, bufSize, params);
    }

    @Override
    public void glGetnUniformiv(int program, int location, int bufSize, IntBuffer params) {
        android.opengl.GLES32.glGetnUniformiv(program, location, bufSize, params);
    }

    @Override
    public void glGetnUniformuiv(int program, int location, int bufSize, IntBuffer params) {
        android.opengl.GLES32.glGetnUniformuiv(program, location, bufSize, params);
    }

    @Override
    public void glGetTexParameterIiv(int target, int pname, IntBuffer params) {
        android.opengl.GLES32.glGetTexParameterIiv(target, pname, params);
    }

    @Override
    public void glGetTexParameterIuiv(int target, int pname, IntBuffer params) {
        android.opengl.GLES32.glGetTexParameterIuiv(target, pname, params);
    }

    @Override
    public void glGetSamplerParameterIiv(int sampler, int pname, IntBuffer params) {
        android.opengl.GLES32.glGetSamplerParameterIiv(sampler, pname, params);
    }

    @Override
    public void glGetSamplerParameterIuiv(int sampler, int pname, IntBuffer params) {
        android.opengl.GLES32.glGetSamplerParameterIuiv(sampler, pname, params);
    }

}
