package com.nucleus.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.nucleus.opengl.GLES31Wrapper;
import com.nucleus.renderer.NucleusRenderer;

public class LWJGL3GLES31Wrapper extends GLES31Wrapper {

    /**
     * Implementation constructor - DO NOT USE - fetch wrapper from {@link NucleusRenderer}
     */
    protected LWJGL3GLES31Wrapper() {
        super(Platform.GL);
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
        IntBuffer ib = ByteBuffer.allocateDirect(buffers.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
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
        IntBuffer v = ByteBuffer.allocateDirect((params.length - offset) * 4).order(ByteOrder.nativeOrder())
                .asIntBuffer();
        org.lwjgl.opengles.GLES20.glGetProgramiv(program, pname, v);
        LWJGLUtils.toArray((IntBuffer) v.rewind(), params, offset);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        IntBuffer lengthBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer sizeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer typeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ByteBuffer nameBuffer = ByteBuffer.allocateDirect(name.length).order(ByteOrder.nativeOrder());
        org.lwjgl.opengles.GLES20.glGetActiveAttrib(program, index, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
        LWJGLUtils.toArray((IntBuffer) lengthBuffer.rewind(), length, lengthOffset);
        LWJGLUtils.toArray((IntBuffer) sizeBuffer.rewind(), size, sizeOffset);
        LWJGLUtils.toArray((IntBuffer) typeBuffer.rewind(), type, typeOffset);
        LWJGLUtils.toArray((ByteBuffer) nameBuffer.rewind(), name, 0);
    }

    @Override
    public void glGetActiveUniform(int program, int index, int[] length, int lengthOffset, int[] size,
            int sizeOffset, int[] type, int typeOffset, byte[] name) {
        IntBuffer lengthBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer sizeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer typeBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ByteBuffer nameBuffer = ByteBuffer.allocateDirect(name.length).order(ByteOrder.nativeOrder());
        org.lwjgl.opengles.GLES20.glGetActiveUniform(program, index, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
        LWJGLUtils.toArray((IntBuffer) lengthBuffer.rewind(), length, lengthOffset);
        LWJGLUtils.toArray((IntBuffer) sizeBuffer.rewind(), size, sizeOffset);
        LWJGLUtils.toArray((IntBuffer) typeBuffer.rewind(), type, typeOffset);
        LWJGLUtils.toArray((ByteBuffer) nameBuffer.rewind(), name, 0);
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
        org.lwjgl.opengles.GLES20.glVertexAttribPointer(index, size, type, normalized, stride, (ByteBuffer) ptr);

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
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniformMatrix4fv(location, transpose,
                LWJGLUtils.toFloatBuffer(v, count * 16, offset));
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniformMatrix3fv(location, transpose,
                LWJGLUtils.toFloatBuffer(v, count * 12, offset));
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniformMatrix2fv(location, transpose,
                LWJGLUtils.toFloatBuffer(v, count * 8, offset));
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
        IntBuffer ib = ByteBuffer.allocateDirect(params.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        org.lwjgl.opengles.GLES20.glGetIntegerv(pname, ib);
        LWJGLUtils.toArray((IntBuffer) ib.rewind(), params, 0);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniform4fv(location, LWJGLUtils.toFloatBuffer(v, 4 * count, offset));
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniform3fv(location, LWJGLUtils.toFloatBuffer(v, 3 * count, offset));
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v0, int offset) {
        org.lwjgl.opengles.GLES20.glUniform1iv(location, LWJGLUtils.toIntBuffer(v0, count, offset));
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset) {
        org.lwjgl.opengles.GLES20.glUniform2fv(location, LWJGLUtils.toFloatBuffer(v, 2 * count, offset));

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
    public void glSamplerParameteri(int sampler, int pname, int param) {
        org.lwjgl.opengles.GLES30.glSamplerParameteri(sampler, pname, sampler);
    }

    @Override
    public void glValidateProgram(int program) {
        org.lwjgl.opengles.GLES20.glValidateProgram(program);
    }

    @Override
    public void glGetShaderSource(int shader, int bufsize, int[] length, byte[] source) {
        ByteBuffer bufferSource = ByteBuffer.wrap(source);
        org.lwjgl.opengles.GLES20.glGetShaderSource(shader, length, bufferSource);
    }

    /**
     * 
     * -----------------------------------------------------------------------------
     * GLES30 methods
     * -----------------------------------------------------------------------------
     * 
     */

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
