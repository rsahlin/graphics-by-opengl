package com.nucleus.opengl;

/**
 * Wrapper for GLES30
 *
 */
public abstract class GLES30Wrapper extends GLES20Wrapper {

    public static String SHADING_LANGUAGE_300 = "300";
    public static String GL_VERSION_430 = "430";

    /**
     * Implementation constructor - DO NOT USE!!!
     * TODO - protect/hide this constructor
     */
    protected GLES30Wrapper(Platform platform) {
        super(platform);
    }

    @Override
    public String getShaderVersion(String sourceVersion) {
        if (sourceVersion.trim().toLowerCase().endsWith(ES) && platform != Platform.GLES) {
            return GL_VERSION_430;
        }
        return sourceVersion;
    }

    /**
     * Abstraction for glSamplerParameteri( GLuint sampler, GLenum pname, GLint param);
     * 
     * @param sampler
     * @param pname
     * @param param
     */
    public abstract void glSamplerParameteri(int sampler, int pname, int param);

    /**
     * Abstraction for glBindBufferBase(GLenum target, GLuint index, GLuint buffer);
     * 
     * @param target
     * @param index
     * @param buffer
     */
    public abstract void glBindBufferBase(int target, int index, int buffer);

    /**
     * Abstraction for glUniformBlockBinding(GLuint program, GLuint uniformBlockIndex, GLuint uniformBlockBinding);
     * 
     * @param program
     * @param uniformBlockIndex
     * @param uniformBlockBinding
     */
    public abstract void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

    /**
     * Abstraction for glBindBufferRange( GLenumtarget, GLuintindex, GLuintbuffer, GLintptroffset, GLsizeiptrsize);
     * 
     * @param target
     * @param index
     * @param buffer
     * @param ptroffset
     * @param ptrsize
     */
    public abstract void glBindBufferRange(int target, int index, int buffer, int ptroffset, int ptrsize);

    /**
     * Abstraction for GLuint glGetUniformBlockIndex( GLuint program, const GLchar *uniformBlockName);
     * 
     * @param program
     * @param uniformBlockName
     * @return
     */
    public abstract int glGetUniformBlockIndex(int program, String uniformBlockName);

    /**
     * Astraction for glGetActiveUniformBlockiv(GLuint program, GLuint uniformBlockIndex, GLenum pname, GLint *params);
     * 
     * @param program
     * @param uniformBlockIndex
     * @param pname
     * @param params
     */
    public abstract void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, int[] params,
            int offset);

    /**
     * Abstraction for void glGetActiveUniformBlockName( GLuint program, GLuint uniformBlockIndex, GLsizei bufSize,
     * GLsizei *length, GLchar *uniformBlockName);
     * 
     * @param program
     * @param uniformBlockIndex
     * @return
     */
    public abstract String glGetActiveUniformBlockName(int program, int uniformBlockIndex);

    /**
     * Abstraction for void glGetActiveUniformsiv( GLuint program, GLsizei uniformCount, const GLuint *uniformIndices,
     * GLenum pname, GLint *params);
     * 
     * @param program
     * @param uniformCount
     * @param uniformIndices
     * @param pname
     * @param params
     */
    public abstract void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int pname,
            int[] params);

}
