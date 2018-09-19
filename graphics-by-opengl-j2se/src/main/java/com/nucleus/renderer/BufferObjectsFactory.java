package com.nucleus.renderer;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES30Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.shader.BlockBuffer;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;

/**
 * This class takes care of allocation and release of buffer objects
 * Use this as much as possible instead of {@link GLES20Wrapper#glGenBuffers(int, int[], int)} to keep track of
 * allocated buffers objects.
 * This class shall not have dependency to GLES implementations
 * 
 * @author Richard Sahlin
 *
 */
public class BufferObjectsFactory {

    private final static BufferObjectsFactory instance = new BufferObjectsFactory();

    /**
     * Returns the buffer objects factory, this is a singleton and will always stay the same.
     * 
     * @return The buffer object factory instance.
     */
    public static BufferObjectsFactory getInstance() {
        return instance;
    }

    /**
     * Creates the vbos for the specified mesh, the buffer objects will be stored in the contained buffers in the mesh.
     * After this call the mesh can be rendered using the specified buffer objects (VBO)
     * 
     * @param gles
     * @param mesh
     * @throws GLException If there is an error setting buffer data
     */
    public void createVBOs(GLES20Wrapper gles, Mesh mesh) throws GLException {
        int vboCount = mesh.getBufferNameCount();
        // TODO Need a way to tie the allocated buffer names to the element/vertex buffers
        int[] names = new int[vboCount];
        gles.glGenBuffers(names);
        mesh.setBufferNames(0, names, 0);
        ElementBuffer indices = mesh.getElementBuffer();
        GLUtils.handleError(gles, "before create vbos");
        for (AttributeBuffer attribs : mesh.getAttributeBuffers()) {
            if (attribs != null) {
                gles.glBindBuffer(GLES20.GL_ARRAY_BUFFER, attribs.getBufferName());
                gles.glBufferData(GLES20.GL_ARRAY_BUFFER, attribs.getSizeInBytes(),
                        attribs.getBuffer().position(0), GLESWrapper.GLES20.GL_STATIC_DRAW);
                attribs.setDirty(false);
                GLUtils.handleError(gles, "createVBOs GL_ARRAY_BUFFER name " + attribs.getBufferName());
            }
        }
        if (indices != null) {
            gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
            gles.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getSizeInBytes(),
                    indices.getBuffer().position(0), GLESWrapper.GLES20.GL_STATIC_DRAW);
            indices.setDirty(false);
            GLUtils.handleError(gles, "createVBOs  GL_ELEMENT_ARRAY_BUFFER name " + indices.getBufferName());
        }
    }

    /**
     * Creates buffer objects for the uniform blocks, buffers are allocated and names stored in uniformBlocks
     * 
     * @param gles
     * @param uniformBlocks uniform block buffers to create buffer objects for, or null
     * @throws GLException
     */
    public void createUBOs(GLES30Wrapper gles, BlockBuffer[] uniformBlocks) throws GLException {
        if (uniformBlocks == null) {
            return;
        }
        int[] names = new int[uniformBlocks.length];
        gles.glGenBuffers(names);
        int index = 0;
        for (BlockBuffer bb : uniformBlocks) {
            InterfaceBlock block = bb.getInterfaceBlock();
            bb.position(0);
            gles.glUniformBlockBinding(block.program, block.blockIndex, index);
            gles.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, names[index]);
            gles.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, block.blockIndex, names[index]);
            gles.glBufferData(GLES30.GL_UNIFORM_BUFFER, bb.getSizeInBytes(), bb.getBuffer(),
                    GLES30.GL_STATIC_DRAW);
            bb.setBufferName(names[index]);
            bb.setDirty(false);
            GLUtils.handleError(gles, "Create UBOs for " + bb.getBlockName());
            index++;
        }
    }

}
