package com.nucleus.opengl;

import java.nio.ByteBuffer;

import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;

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
     * 
     * @param platform
     * @param renderVersion If higher than GLES30, otherwise null
     */
    protected GLES30Wrapper(Platform platform, Renderers renderVersion) {
        super(platform, renderVersion == null ? Renderers.GLES30 : renderVersion);
    }

    @Override
    public ProgramInfo getProgramInfo(int program) {
        int[] activeInfo = new int[3];
        int[] nameLength = new int[3];
        glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, activeInfo, VariableType.ATTRIBUTE.index);
        glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, activeInfo, VariableType.UNIFORM.index);
        glGetProgramiv(program, GLES30.GL_ACTIVE_UNIFORM_BLOCKS, activeInfo, VariableType.UNIFORM_BLOCK.index);
        glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, nameLength, VariableType.ATTRIBUTE.index);
        glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORM_MAX_LENGTH, nameLength, VariableType.UNIFORM.index);
        glGetProgramiv(program, GLES30.GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH, nameLength,
                VariableType.UNIFORM_BLOCK.index);
        return new ProgramInfo(program, activeInfo, nameLength);
    }

    @Override
    public InterfaceBlock[] getUniformBlocks(ProgramInfo info) {
        if (info == null || info.getActiveVariables(VariableType.UNIFORM_BLOCK) < 1) {
            return null;
        }
        InterfaceBlock[] uniformBlock = new InterfaceBlock[info.getActiveVariables(VariableType.UNIFORM_BLOCK)];
        int[] blockInfo = new int[4];
        int[] indices = null;
        for (int i = 0; i < uniformBlock.length; i++) {
            // GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS
            int program = info.getProgram();
            glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, blockInfo, 0);
            if (blockInfo[0] > 0) {
                indices = new int[blockInfo[0]];
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, indices,
                        0);
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_DATA_SIZE, blockInfo, 1);
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER,
                        blockInfo, 2);
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER,
                        blockInfo, 3);
                uniformBlock[i] = new InterfaceBlock(info.getProgram(), i,
                        glGetActiveUniformBlockName(info.getProgram(), i), blockInfo, indices);
            }
        }
        return uniformBlock;
    }

    @Override
    public ShaderVariable getActiveVariable(int program, VariableType type, int index, byte[] nameBuffer)
            throws GLException {

        switch (type) {
            case ATTRIBUTE:
            case UNIFORM:
                return super.getActiveVariable(program, type, index, nameBuffer);
            case UNIFORM_BLOCK:
                int[] params = new int[10];
                int[] indices = new int[] { index };
                params[ShaderVariable.ACTIVE_INDEX_OFFSET] = index;
                glGetActiveUniform(program, index, params, ShaderVariable.NAME_LENGTH_OFFSET, params,
                        ShaderVariable.SIZE_OFFSET, params, ShaderVariable.TYPE_OFFSET, nameBuffer);
                glGetActiveUniformsiv(program, 1, indices, 0, GLES30.GL_UNIFORM_BLOCK_INDEX, params,
                        ShaderVariable.BLOCK_INDEX_OFFSET);
                glGetActiveUniformsiv(program, 1, indices, 0, GLES30.GL_UNIFORM_OFFSET, params,
                        ShaderVariable.DATA_OFFSET);
                GLUtils.handleError(this, "glGetActiveUniform for " + type);
                // Create shader variable using name excluding [] and .
                return new ShaderVariable(VariableType.UNIFORM_BLOCK,
                        getVariableName(nameBuffer, params[ShaderVariable.NAME_LENGTH_OFFSET]),
                        params, 0);
            default:
                throw new IllegalArgumentException("Invalid variable type " + type);

        }

    }

    @Override
    public String getShaderVersion(String sourceVersion, int version) {
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
     * @param indicesOffset
     * @param pname
     * @param params
     * @param paramsOffset
     */
    public abstract void glGetActiveUniformsiv(int program, int uniformCount, int[] uniformIndices, int indicesOffset,
            int pname, int[] params, int paramsOffset);

    /**
     * Abstraction for void *glMapBufferRange( GLenum target, GLintptr offset, GLsizeiptr length, GLbitfield access);
     * 
     * @param target
     * @param offset
     * @param length
     * @param access
     * @return
     */
    public abstract ByteBuffer glMapBufferRange(int target, int offset, int length, int access);

    /**
     * Abstraction for GLboolean glUnmapBuffer( GLenum target);
     * 
     * @param target
     * @return
     */
    public abstract boolean glUnmapBuffer(int target);

    /**
     * Abstraction for void glFlushMappedBufferRange(GLenum target, GLintptr offset, GLsizeiptr length);
     * 
     * @param target
     * @param offset
     * @param length
     */
    public abstract void glFlushMappedBufferRange(int target, int offset, int length);

}
