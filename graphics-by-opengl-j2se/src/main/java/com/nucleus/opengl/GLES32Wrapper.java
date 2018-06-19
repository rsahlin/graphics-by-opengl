package com.nucleus.opengl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class GLES32Wrapper extends GLES31Wrapper {

    /**
     * Implementation constructor - DO NOT USE!!!
     * TODO - protect/hide this constructor
     * 
     * @param platform
     * @param renderVersion If higher than GLES32, otherwise null
     */
    protected GLES32Wrapper(Platform platform, Renderers renderVersion) {
        super(platform, renderVersion == null ? Renderers.GLES32 : renderVersion);
    }

    /**
     * 
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param basevertex
     */
    public abstract void glDrawElementsBaseVertex(int mode, int count, int type, ByteBuffer indices, int basevertex);

    /**
     * 
     * @param mode
     * @param start
     * @param end
     * @param count
     * @param type
     * @param indices
     * @param basevertex
     */
    public abstract void glDrawRangeElementsBaseVertex(int mode, int start, int end, int count, int type,
            ByteBuffer indices,
            int basevertex);

    /**
     * 
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param instancecount
     * @param basevertex
     */
    public abstract void glDrawElementsInstancedBaseVertex(int mode, int count, int type, ByteBuffer indices,
            int instancecount, int basevertex);

    /**
     * 
     * @param target
     * @param attachment
     * @param texture
     * @param level
     */
    public abstract void glFramebufferTexture(int target, int attachment, int texture, int level);

    /**
     * 
     * @param minX
     * @param minY
     * @param minZ
     * @param minW
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param maxW
     */
    public abstract void glPrimitiveBoundingBox(float minX, float minY, float minZ, float minW, float maxX, float maxY,
            float maxZ, float maxW);

    /**
     * 
     * @return
     */
    public abstract int glGetGraphicsResetStatus();

    /**
     * 
     * @param value
     */
    public abstract void glMinSampleShading(float value);

    /**
     * 
     * @param pname
     * @param value
     */
    public abstract void glPatchParameteri(int pname, int value);

    /**
     * 
     * @param target
     * @param pname
     * @param params
     */
    public abstract void glTexParameterIiv(int target, int pname, IntBuffer params);

    /**
     * 
     * @param target
     * @param pname
     * @param params
     */
    public abstract void glTexParameterIuiv(int target, int pname, IntBuffer params);

    /**
     * 
     * @param sampler
     * @param pname
     * @param param
     */
    public abstract void glSamplerParameterIiv(int sampler, int pname, IntBuffer param);

    /**
     * 
     * @param sampler
     * @param pname
     * @param param
     */
    public abstract void glSamplerParameterIuiv(int sampler, int pname, IntBuffer param);

    /**
     * 
     * @param target
     * @param internalformat
     * @param buffer
     */
    public abstract void glTexBuffer(int target, int internalformat, int buffer);

    /**
     * 
     * @param target
     * @param internalformat
     * @param buffer
     * @param offset
     * @param size
     */
    public abstract void glTexBufferRange(int target, int internalformat, int buffer, int offset,
            int size);

    /**
     * 
     * @param target
     * @param samples
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @param fixedsamplelocations
     */
    public abstract void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height,
            int depth, boolean fixedsamplelocations);

    /**
     * 
     */
    public abstract void glBlendBarrier();

    /**
     * 
     * @param srcName
     * @param srcTarget
     * @param srcLevel
     * @param srcX
     * @param srcY
     * @param srcZ
     * @param dstName
     * @param dstTarget
     * @param dstLevel
     * @param dstX
     * @param dstY
     * @param dstZ
     * @param srcWidth
     * @param srcHeight
     * @param srcDepth
     */
    public abstract void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY,
            int srcZ, int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ,
            int srcWidth, int srcHeight, int srcDepth);

    /**
     * 
     * @param source
     * @param type
     * @param severity
     * @param count
     * @param ids
     * @param enabled
     */
    public abstract void glDebugMessageControl(int source, int type, int severity, int count, IntBuffer ids,
            boolean enabled);

    /**
     * 
     * @param source
     * @param type
     * @param id
     * @param severity
     * @param length
     * @param buf
     */
    public abstract void glDebugMessageInsert(int source, int type, int id, int severity, int length, String buf);

    // public abstract void glDebugMessageCallback (GLDEBUGPROC callback, void *userParam);

    /**
     * 
     * @param count
     * @param bufSize
     * @param sources
     * @param types
     * @param ids
     * @param severities
     * @param lengths
     * @param messageLog
     * @return
     */
    public abstract int glGetDebugMessageLog(int count, int bufSize, IntBuffer sources, IntBuffer types, IntBuffer ids,
            IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog);

    /**
     * 
     * @param source
     * @param id
     * @param length
     * @param message
     */
    public abstract void glPushDebugGroup(int source, int id, int length, String message);

    /**
     * 
     */
    public abstract void glPopDebugGroup();

    /**
     * 
     * @param identifier
     * @param name
     * @param length
     * @param label
     */
    public abstract void glObjectLabel(int identifier, int name, int length, String label);

    /**
     * 
     * @param identifier
     * @param name
     * @return
     */
    public abstract String glGetObjectLabel(int identifier, int name);

    /**
     * 
     * @param ptr
     * @param label
     */
    public abstract void glObjectPtrLabel(long ptr, String label);

    /**
     * 
     * @param ptr
     * @return
     */
    public abstract String glGetObjectPtrLabel(long ptr);

    /**
     * 
     * @param pname
     * @return
     */
    public abstract long glGetPointerv(int pname);

    /**
     * 
     * @param target
     * @param index
     */
    public abstract void glEnablei(int target, int index);

    /**
     * 
     * @param target
     * @param index
     */
    public abstract void glDisablei(int target, int index);

    /**
     * 
     * @param buf
     * @param mode
     */
    public abstract void glBlendEquationi(int buf, int mode);

    /**
     * 
     * @param buf
     * @param modeRGB
     * @param modeAlpha
     */
    public abstract void glBlendEquationSeparatei(int buf, int modeRGB, int modeAlpha);

    /**
     * 
     * @param buf
     * @param src
     * @param dst
     */
    public abstract void glBlendFunci(int buf, int src, int dst);

    /**
     * 
     * @param buf
     * @param srcRGB
     * @param dstRGB
     * @param srcAlpha
     * @param dstAlpha
     */
    public abstract void glBlendFuncSeparatei(int buf, int srcRGB, int dstRGB, int srcAlpha,
            int dstAlpha);

    /**
     * 
     * @param index
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public abstract void glColorMaski(int index, boolean r, boolean g, boolean b, boolean a);

    /**
     * 
     * @param target
     * @param index
     * @return
     */
    public abstract boolean glIsEnabledi(int target, int index);

    /**
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param format
     * @param type
     * @param bufSize
     * @param data
     */
    public abstract void glReadnPixels(int x, int y, int width, int height, int format, int type, int bufSize,
            Buffer data);

    /**
     * 
     * @param program
     * @param location
     * @param bufSize
     * @param params
     */
    public abstract void glGetnUniformfv(int program, int location, int bufSize, FloatBuffer params);

    /**
     * 
     * @param program
     * @param location
     * @param bufSize
     * @param params
     */
    public abstract void glGetnUniformiv(int program, int location, int bufSize, IntBuffer params);

    /**
     * 
     * @param program
     * @param location
     * @param bufSize
     * @param params
     */
    public abstract void glGetnUniformuiv(int program, int location, int bufSize, IntBuffer params);

    /**
     * 
     * @param target
     * @param pname
     * @param params
     */
    public abstract void glGetTexParameterIiv(int target, int pname, IntBuffer params);

    /**
     * 
     * @param target
     * @param pname
     * @param params
     */
    public abstract void glGetTexParameterIuiv(int target, int pname, IntBuffer params);

    /**
     * 
     * @param sampler
     * @param pname
     * @param params
     */
    public abstract void glGetSamplerParameterIiv(int sampler, int pname, IntBuffer params);

    /**
     * 
     * @param sampler
     * @param pname
     * @param params
     */
    public abstract void glGetSamplerParameterIuiv(int sampler, int pname, IntBuffer params);

}
