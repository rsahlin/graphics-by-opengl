package com.nucleus.opengl;

/**
 * Wrapper for GLES 3.1
 *
 */
public abstract class GLES31Wrapper extends GLES30Wrapper {

    public abstract void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z);

    public abstract void glDispatchComputeIndirect(long indirect);

    public abstract void glDrawArraysIndirect(int mode, long indirect);

    public abstract void glDrawElementsIndirect(int mode, int type, long indirect);

    public static native void glFramebufferParameteri(int target, int pname, int param);

    public abstract void glGetFramebufferParameteriv(int target, int pname, int[] params, int offset);

    public abstract void glGetFramebufferParameteriv(int target, int pname, java.nio.IntBuffer params);

    public abstract void glGetProgramInterfaceiv(int program, int programInterface, int pname, int[] params,
            int offset);

    public abstract void glGetProgramInterfaceiv(int program, int programInterface, int pname,
            java.nio.IntBuffer params);

    public abstract int glGetProgramResourceIndex(int program, int programInterface, String name);

    public abstract String glGetProgramResourceName(int program, int programInterface, int index);

    public abstract void glGetProgramResourceiv(int program, int programInterface, int index, int propCount,
            int[] props, int propsOffset, int bufSize, int[] length, int lengthOffset, int[] params, int paramsOffset);

    public abstract void glGetProgramResourceiv(int program, int programInterface, int index, int propCount,
            java.nio.IntBuffer props, int bufSize, java.nio.IntBuffer length, java.nio.IntBuffer params);

    public abstract int glGetProgramResourceLocation(int program, int programInterface, String name);

    public abstract void glUseProgramStages(int pipeline, int stages, int program);

    public abstract void glActiveShaderProgram(int pipeline, int program);

    public abstract int glCreateShaderProgramv(int type, String[] strings);

    public abstract void glBindProgramPipeline(int pipeline);

    public abstract void glDeleteProgramPipelines(int n, int[] pipelines, int offset);

    public abstract void glDeleteProgramPipelines(int n, java.nio.IntBuffer pipelines);

    public abstract void glGenProgramPipelines(int n, int[] pipelines, int offset);

    public abstract void glGenProgramPipelines(int n, java.nio.IntBuffer pipelines);

    public abstract boolean glIsProgramPipeline(int pipeline);

    public abstract void glGetProgramPipelineiv(int pipeline, int pname, int[] params, int offset);

    public abstract void glGetProgramPipelineiv(int pipeline, int pname, java.nio.IntBuffer params);

    public abstract void glProgramUniform1i(int program, int location, int v0);

    public abstract void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3);

    public abstract void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3);

    public abstract void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3);

    public abstract void glProgramUniform4iv(int program, int location, int count, int[] value, int offset);

    public abstract void glProgramUniform4iv(int program, int location, int count, java.nio.IntBuffer value);

    public abstract void glProgramUniform4uiv(int program, int location, int count, int[] value, int offset);

    public abstract void glProgramUniform4uiv(int program, int location, int count, java.nio.IntBuffer value);

    public abstract void glProgramUniform4fv(int program, int location, int count, float[] value, int offset);

    public abstract void glProgramUniform4fv(int program, int location, int count, java.nio.FloatBuffer value);

    public abstract void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose,
            float[] value, int offset);

    public abstract void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose,
            java.nio.FloatBuffer value);

    public abstract void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose,
            float[] value, int offset);

    public abstract void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose,
            java.nio.FloatBuffer value);

    public abstract void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose,
            float[] value, int offset);

    public abstract void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose,
            java.nio.FloatBuffer value);

    public abstract void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
            float[] value, int offset);

    public abstract void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
            java.nio.FloatBuffer value);

    public abstract void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
            float[] value, int offset);

    public abstract void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
            java.nio.FloatBuffer value);

    public abstract void glValidateProgramPipeline(int pipeline);

    public abstract String glGetProgramPipelineInfoLog(int program);

    public abstract void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
            int format);

    public abstract void glGetBooleani_v(int target, int index, boolean[] data, int offset);

    public abstract void glGetBooleani_v(int target, int index, java.nio.IntBuffer data);

    public abstract void glMemoryBarrier(int barriers);

    public abstract void glMemoryBarrierByRegion(int barriers);

    public abstract void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations);

    public abstract void glGetMultisamplefv(int pname, int index, float[] val, int offset);

    public abstract void glGetMultisamplefv(int pname, int index, java.nio.FloatBuffer val);

    public abstract void glSampleMaski(int maskNumber, int mask);

    public abstract void glGetTexLevelParameteriv(int target, int level, int pname, int[] params, int offset);

    public abstract void glGetTexLevelParameteriv(int target, int level, int pname, java.nio.IntBuffer params);

    public abstract void glGetTexLevelParameterfv(int target, int level, int pname, float[] params, int offset);

    public abstract void glGetTexLevelParameterfv(int target, int level, int pname, java.nio.FloatBuffer params);

    public abstract void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride);

    public abstract void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized,
            int relativeoffset);

    public abstract void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset);

    public abstract void glVertexAttribBinding(int attribindex, int bindingindex);

    public abstract void glVertexBindingDivisor(int bindingindex, int divisor);

}
