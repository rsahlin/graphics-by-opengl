package com.nucleus.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.nucleus.shader.ShaderSource.ESSLVersion;
import com.nucleus.common.BufferUtils;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * Wrapper for GLES30
 *
 */
public abstract class GLES30Wrapper extends GLES20Wrapper {

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
        IntBuffer blockInfo = BufferUtils.createIntBuffer(4);
        for (int i = 0; i < uniformBlock.length; i++) {
            // GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS
            int program = info.getProgram();
            blockInfo.position(InterfaceBlock.ACTIVE_COUNT_INDEX);
            glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, blockInfo);
            if (blockInfo.get(0) > 0) {
                IntBuffer indices = BufferUtils.createIntBuffer(blockInfo.get(0));
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES,
                        indices);
                blockInfo.position(InterfaceBlock.BLOCK_DATA_SIZE_INDEX);
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_DATA_SIZE, blockInfo);
                blockInfo.position(InterfaceBlock.VERTEX_REFERENCE_INDEX);
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER,
                        blockInfo);
                blockInfo.position(InterfaceBlock.FRAGMENT_REFERENCE_INDEX);
                glGetActiveUniformBlockiv(program, i, GLES30.GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER,
                        blockInfo);
                // Block name is fetched using blockIndex so we know the blockIndex to be correct.
                uniformBlock[i] = new InterfaceBlock(info.getProgram(), i,
                        glGetActiveUniformBlockName(info.getProgram(), i), blockInfo, indices);
            }
        }
        return uniformBlock;
    }

    @Override
    public ShaderVariable getActiveVariable(int program, VariableType type, int index, byte[] nameBuffer)
            throws GLException {

        int[] params = null;
        int[] indices = new int[] { index };
        switch (type) {
            case ATTRIBUTE:
                return super.getActiveVariable(program, type, index, nameBuffer);
            case UNIFORM:
                GLUtils.handleError(this, "UNIFORM_BLOCK clear error");
                params = new int[ShaderVariable.DATA_OFFSET + 1];
                params[ShaderVariable.ACTIVE_INDEX_OFFSET] = index;
                glGetActiveUniform(program, index, params, ShaderVariable.NAME_LENGTH_OFFSET, params,
                        ShaderVariable.SIZE_OFFSET, params, ShaderVariable.TYPE_OFFSET, nameBuffer);
                GLUtils.handleError(this, "UNIFORM glGetActiveUniform for " + new String(nameBuffer));
                return new ShaderVariable(type,
                        getVariableName(nameBuffer, params[ShaderVariable.NAME_LENGTH_OFFSET]),
                        params, 0);
            case UNIFORM_BLOCK:
                params = new int[10];
                params[ShaderVariable.ACTIVE_INDEX_OFFSET] = index;
                glGetActiveUniform(program, index, params, ShaderVariable.NAME_LENGTH_OFFSET, params,
                        ShaderVariable.SIZE_OFFSET, params, ShaderVariable.TYPE_OFFSET, nameBuffer);
                GLUtils.handleError(this, "UNIFORM_BLOCK glGetActiveUniforms for " + new String(nameBuffer));
                glGetActiveUniformsiv(program, 1, indices, 0, GLES30.GL_UNIFORM_BLOCK_INDEX, params,
                        ShaderVariable.BLOCK_INDEX_OFFSET);
                glGetActiveUniformsiv(program, 1, indices, 0, GLES30.GL_UNIFORM_OFFSET, params,
                        ShaderVariable.DATA_OFFSET);
                GLUtils.handleError(this, "UNIFORM_BLOCK glGetActiveUniformsiv for " + new String(nameBuffer));
                // Create shader variable using name excluding [] and .
                return new ShaderVariable(type,
                        getVariableName(nameBuffer, params[ShaderVariable.NAME_LENGTH_OFFSET]),
                        params, 0);
            default:
                throw new IllegalArgumentException("Invalid variable type " + type);

        }

    }

    @Override
    public ESSLVersion replaceShaderVersion(ESSLVersion version) {
        switch (version) {
            case VERSION100:
                return version;
            case VERSION300:
            case VERSION310:
            case VERSION320:
                return platform != Platform.GLES ? ESSLVersion.VERSION430 : version;
            case VERSION430:
                return version;
            default:
                throw new IllegalArgumentException("Not implemented for " + version);
        }
    }

    /**
     * This method works with buffer object, add method with element buffer if needed.
     * 
     * @param mode
     * @param start
     * @param end
     * @param count
     * @param type
     * @param offset Specifies a byte offset into the buffer bound to GL_ELEMENT_ARRAY_BUFFER to start reading indices
     * from
     */
    public abstract void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset);

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
     * @param buffer
     */
    public abstract void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer buffer);

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

    /**
     * 
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @param height
     */
    public abstract void glTexStorage2D(int target, int levels, int internalformat, int width, int height);

}
