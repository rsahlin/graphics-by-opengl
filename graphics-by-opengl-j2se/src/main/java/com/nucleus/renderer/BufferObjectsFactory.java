package com.nucleus.renderer;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;

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
     * @param renderer
     * @param mesh
     * @throws GLException If there is an error setting buffer data
     */
    public void createVBOs(NucleusRenderer renderer, Mesh mesh) throws GLException {
        int vboCount = mesh.getBufferNameCount();
        // TODO Need a way to tie the allocated buffer names to the element/vertex buffers
        int[] names = new int[vboCount];
        renderer.genBuffers(names);
        mesh.setBufferNames(0, names, 0);
        ElementBuffer indices = mesh.getElementBuffer();
        GLUtils.handleError(renderer.getGLES(), "before create vbos");
        for (AttributeBuffer attribs : mesh.getVerticeBuffers()) {
            if (attribs != null) {
                renderer.bindBuffer(GLESWrapper.GLES20.GL_ARRAY_BUFFER, attribs.getBufferName());
                renderer.bufferData(GLESWrapper.GLES20.GL_ARRAY_BUFFER, attribs.getSizeInBytes(),
                        attribs.getBuffer().position(0), GLESWrapper.GLES20.GL_STATIC_DRAW);
                attribs.setDirty(false);
            }
            GLUtils.handleError(renderer.getGLES(), "createVBOs GL_ARRAY_BUFFER name " + attribs.getBufferName());
        }
        if (indices != null) {
            renderer.bindBuffer(GLESWrapper.GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
            renderer.bufferData(GLESWrapper.GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getSizeInBytes(),
                    indices.getBuffer().position(0), GLESWrapper.GLES20.GL_STATIC_DRAW);
            indices.setDirty(false);
            GLUtils.handleError(renderer.getGLES(),
                    "createVBOs  GL_ELEMENT_ARRAY_BUFFER name " + indices.getBufferName());
        }
    }

}
