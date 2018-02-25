package com.nucleus.lwjgl3;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES31Wrapper;
import com.nucleus.renderer.NucleusRenderer;

public class LWJGL3GLES31Wrapper extends GLES31Wrapper {

    LWJGL3GLES20Wrapper gles20 = new LWJGL3GLES20Wrapper();
    LWJGL3GLES30Wrapper gles30 = new LWJGL3GLES30Wrapper();

    /**
     * Implementation constructor - DO NOT USE - fetch wrapper from {@link NucleusRenderer}
     */
    protected LWJGL3GLES31Wrapper() {
        super(Platform.GL, Renderers.GLES31);
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
     * -----------------------------------------------------------------------------
     * GLES30 methods - just pass on to gles30 wrapper
     * -----------------------------------------------------------------------------
     * 
     */

    @Override
    public void glSamplerParameteri(int sampler, int pname, int param) {
        gles30.glSamplerParameteri(sampler, pname, sampler);
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {
        gles30.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        gles30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, int ptroffset, int ptrsize) {
        gles30.glBindBufferRange(target, index, buffer, ptroffset, ptrsize);
    }

    @Override
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {
        return gles30.glGetUniformBlockIndex(program, uniformBlockName);
    }

    @Override
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params, int offset) {
        gles30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params, offset);
    }

    @Override
    public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
        return gles30.glGetActiveUniformBlockName(program, uniformBlockIndex);
    }

    @Override
    public void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset,
            int pname, int[] params, int paramsOffset) {
        gles30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, indicesOffset, pname, params,
                paramsOffset);
    }

    /**
     * 
     * -----------------------------------------------------------------------------
     * GLES31 methods
     * -----------------------------------------------------------------------------
     * 
     */

    @Override
    public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        org.lwjgl.opengles.GLES31.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
    }

    @Override
    public void glDispatchComputeIndirect(long indirect) {
        org.lwjgl.opengles.GLES31.glDispatchComputeIndirect(indirect);
    }

    @Override
    public void glDrawArraysIndirect(int mode, long indirect) {
        org.lwjgl.opengles.GLES31.glDrawArraysIndirect(mode, indirect);
    }

    @Override
    public void glDrawElementsIndirect(int mode, int type, long indirect) {
        org.lwjgl.opengles.GLES31.glDrawElementsIndirect(mode, type, indirect);
    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, int[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetProgramInterfaceiv(int program, int programInterface, int pname, int[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glGetProgramResourceIndex(int program, int programInterface, String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String glGetProgramResourceName(int program, int programInterface, int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, int[] props,
            int propsOffset, int bufSize, int[] length, int lengthOffset, int[] params, int paramsOffset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, IntBuffer props,
            int bufSize, IntBuffer length, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glGetProgramResourceLocation(int program, int programInterface, String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glUseProgramStages(int pipeline, int stages, int program) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glActiveShaderProgram(int pipeline, int program) {
        // TODO Auto-generated method stub

    }

    @Override
    public int glCreateShaderProgramv(int type, String[] strings) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void glBindProgramPipeline(int pipeline) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDeleteProgramPipelines(int n, int[] pipelines, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glDeleteProgramPipelines(int n, IntBuffer pipelines) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGenProgramPipelines(int n, int[] pipelines, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGenProgramPipelines(int n, IntBuffer pipelines) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean glIsProgramPipeline(int pipeline) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, int[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform1i(int program, int location, int v0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, int[] value, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4iv(int program, int location, int count, IntBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, int[] value, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4uiv(int program, int location, int count, IntBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, float[] value, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniform4fv(int program, int location, int count, FloatBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, float[] value,
            int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, float[] value,
            int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, float[] value,
            int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose, float[] value,
            int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose, float[] value,
            int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
            FloatBuffer value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glValidateProgramPipeline(int pipeline) {
        // TODO Auto-generated method stub

    }

    @Override
    public String glGetProgramPipelineInfoLog(int program) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
            int format) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetBooleani_v(int target, int index, boolean[] data, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetBooleani_v(int target, int index, IntBuffer data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glMemoryBarrier(int barriers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glMemoryBarrierByRegion(int barriers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetMultisamplefv(int pname, int index, float[] val, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glSampleMaski(int maskNumber, int mask) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, int[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, float[] params, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        // TODO Auto-generated method stub

    }

    @Override
    public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        // TODO Auto-generated method stub
    }

    @Override
    public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        // TODO Auto-generated method stub
    }

    @Override
    public void glVertexAttribBinding(int attribindex, int bindingindex) {
        // TODO Auto-generated method stub
    }

    @Override
    public void glVertexBindingDivisor(int bindingindex, int divisor) {
        // TODO Auto-generated method stub
    }

}
