package com.nucleus.opengl;

import java.util.ArrayList;

import com.nucleus.Backend;
import com.nucleus.BackendException;
import com.nucleus.SimpleLogger;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;
import com.nucleus.opengl.shader.BlockBuffer;
import com.nucleus.opengl.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.renderer.BufferFactory;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.Primitive;

/**
 * This class takes care of allocation and release of buffer objects
 * Use this as much as possible instead of {@link GLES20Wrapper#glGenBuffers(int, int[], int)} to keep track of
 * allocated buffers objects.
 * 
 * @author Richard Sahlin
 *
 */
public class GLESBufferFactory implements BufferFactory {

    protected GLES20Wrapper gles;
    protected GLES30Wrapper gles30;

    /**
     * 
     * @param gles Must be at least GLES30 to support UBOs
     */
    public GLESBufferFactory(GLES20Wrapper gles) {
        this.gles = gles;
        if (gles instanceof GLES30Wrapper) {
            gles30 = (GLES30Wrapper) gles;
        }
    }

    @Override
    public void createVBOs(Mesh mesh) throws BackendException {
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

    @Override
    public void destroyVBOs(Mesh mesh) {
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices != null && indices.getBufferName() > 0) {
            gles.glDeleteBuffers(1, new int[] { indices.getBufferName() }, 0);
        }
        AttributeBuffer[] attributes = mesh.getAttributeBuffers();
        if (attributes != null) {
            for (AttributeBuffer buffer : attributes) {
                if (buffer.getBufferName() > 0) {
                    gles.glDeleteBuffers(1, new int[] { buffer.getBufferName() }, 0);
                }
            }
        }
    }

    @Override
    public void createUBOs(BlockBuffer[] uniformBlocks) throws BackendException {
        if (uniformBlocks == null) {
            return;
        }
        int[] names = new int[uniformBlocks.length];
        gles30.glGenBuffers(names);
        int index = 0;
        for (BlockBuffer bb : uniformBlocks) {
            InterfaceBlock block = bb.getInterfaceBlock();
            bb.position(0);
            gles30.glUniformBlockBinding(block.program, block.blockIndex, index);
            gles30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, names[index]);
            gles30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, block.blockIndex, names[index]);
            gles30.glBufferData(GLES30.GL_UNIFORM_BUFFER, bb.getSizeInBytes(), bb.getBuffer(),
                    GLES30.GL_STATIC_DRAW);
            bb.setBufferName(names[index]);
            bb.setDirty(false);
            GLUtils.handleError(gles30, "Create UBOs for " + bb.getBlockName());
            index++;
        }
    }

    @Override
    public void createVBOs(Primitive primitive) throws BackendException {
        createVBOs(primitive.getBufferArray());
    }

    @Override
    public void createVBOs(ArrayList<Buffer> buffers) throws BackendException {
        for (Buffer buffer : buffers) {
            createVBO(buffer);
        }
    }

    @Override
    public void createVBOs(Buffer[] buffers) throws BackendException {
        for (Buffer buffer : buffers) {
            createVBO(buffer);
        }
    }

    @Override
    public void createVBO(Buffer buffer) throws BackendException {
        if (buffer.getBufferName() <= 0) {
            SimpleLogger.d(getClass(),
                    "Allocating VBO for buffer: " + buffer.getUri() + ", name: " + buffer.getName() + ", total size: "
                            + buffer.getByteLength());
            int[] names = new int[1];
            gles.glGenBuffers(names);
            buffer.setBufferName(names[0]);
            GLUtils.handleError(gles, "Create VBO for buffer " + buffer.getUri());
            gles.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer.getBufferName());
            gles.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.getByteLength(), buffer.getBuffer().position(0),
                    GLESWrapper.GLES20.GL_STATIC_DRAW);
            GLUtils.handleError(gles, "BufferData for buffer " + buffer.getUri());
        }
    }

    @Override
    public void destroyVBOs(NucleusRenderer renderer, ArrayList<Buffer> buffers) throws BackendException {
        int[] names = new int[1];
        int deleted = 0;
        StringBuffer bufferStr = new StringBuffer();
        Backend backend = renderer.getBackend();
        for (Buffer buffer : buffers) {
            names[0] = buffer.getBufferName();
            if (names[0] > 0) {
                bufferStr.append("[" + (buffer.getUri() != null ? buffer.getUri() : buffer.getName()) + "]");
                gles.glDeleteBuffers(1, names, 0);
                deleted++;
                buffer.setBufferName(0);
            }
        }
        SimpleLogger.d(getClass(), "Deleted " + deleted + " buffers, " + bufferStr.toString());
    }

}
