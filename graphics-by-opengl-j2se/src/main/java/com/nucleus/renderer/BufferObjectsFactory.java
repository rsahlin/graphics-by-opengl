package com.nucleus.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

import com.nucleus.SimpleLogger;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper;

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

    private final static float[] debug_float_data = new float[32];
    private final static short[] debug_short_data = new short[32];

    /**
     * Creates the vbos for the specified mesh, the buffer objects will be stored in the contained buffers in the mesh.
     * After this call the mesh can be rendered using the specified buffer objects (VBO)
     * 
     * @param renderer
     * @param mesh
     */
    public void createVBOs(NucleusRenderer renderer, Mesh mesh) {
        int vboCount = mesh.getBufferNameCount();
        // TODO Need a way to tie the allocated buffer names to the element/vertex buffers
        int[] names = new int[vboCount];
        renderer.genBuffers(vboCount, names, 0);
        mesh.setBufferNames(0, names, 0);
        ElementBuffer indices = mesh.getElementBuffer();
        for (VertexBuffer attribs : mesh.getVerticeBuffers()) {
            ((FloatBuffer) attribs.getBuffer().position(0)).get(debug_float_data);
            SimpleLogger.d(BufferObjectsFactory.class,
                    "Uploading array data to vertex buffer object: " + Arrays.toString(debug_float_data));
            renderer.bindBuffer(GLESWrapper.GLES20.GL_ARRAY_BUFFER, attribs.getBufferName());
            renderer.bufferData(GLESWrapper.GLES20.GL_ARRAY_BUFFER, attribs.getSizeInBytes(),
                    attribs.getBuffer().position(0), GLESWrapper.GLES20.GL_STATIC_DRAW);
            attribs.setDirty(false);
        }
        if (indices != null) {
            ((ByteBuffer) indices.getBuffer().position(0)).asShortBuffer().get(debug_short_data);
            SimpleLogger.d(BufferObjectsFactory.class,
                    "Uploading element data to vertex buffer object: " + Arrays.toString(debug_short_data));
            renderer.bindBuffer(GLESWrapper.GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
            renderer.bufferData(GLESWrapper.GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getSizeInBytes(),
                    indices.getBuffer().position(0), GLESWrapper.GLES20.GL_STATIC_DRAW);
            indices.setDirty(false);
        }
    }

}
