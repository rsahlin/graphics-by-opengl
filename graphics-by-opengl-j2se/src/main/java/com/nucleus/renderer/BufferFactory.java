package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.BackendException;
import com.nucleus.geometry.Mesh;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.shader.BlockBuffer;

/**
 * Creates VertexBufferObjects and UniformBufferObjects
 *
 */
public interface BufferFactory {

    /**
     * Creates the vbos and uploads data for the specified mesh, the buffer objects will be stored in the contained
     * buffers in the mesh.
     * After this call the mesh can be rendered using the specified buffer objects (VBO)
     * 
     * @param mesh
     * @throws BackendException If there is an error setting buffer data
     */
    public void createVBOs(Mesh mesh) throws BackendException;

    /**
     * Destroys the vbos for the mesh, if previously allocated with a call to {@link #createVBOs(Mesh)}, otherwise
     * this method does nothing.
     * After calling this method the VBOs are not available.
     * 
     * @param mesh
     */
    public void destroyVBOs(Mesh mesh);

    /**
     * Creates buffer objects for the uniform blocks, buffers are allocated and names stored in uniformBlocks
     * 
     * @param uniformBlocks uniform block buffers to create buffer objects for, or null
     * @throws BackendException
     */
    public void createUBOs(BlockBuffer[] uniformBlocks) throws BackendException;

    /**
     * Creates VBO's and uploads data for the buffer(s) that are used by the primitive.
     * 
     * @param primitive
     * @throws BackendException
     */
    public void createVBOs(Primitive primitive) throws BackendException;

    /**
     * Creates VBO's and uploads data for the buffer(s)
     * 
     * @param buffers
     * @throws BackendException
     */
    public void createVBOs(ArrayList<Buffer> buffers) throws BackendException;

    /**
     * Creates VBO's and uploads data for the buffer(s)
     * 
     * @param buffers
     * @throws BackendException
     */
    public void createVBOs(Buffer[] buffers) throws BackendException;

    /**
     * Creates VBO's for the specified Buffer
     * 
     * @param buffer
     * @throws BackendException
     */
    public void createVBO(Buffer buffer) throws BackendException;

    /**
     * Destroys the buffers if VBOs have been allocated.
     * 
     * @param buffers
     * @throws BackendException
     */
    public void destroyVBOs(NucleusRenderer renderer, ArrayList<Buffer> buffers) throws BackendException;
}
