package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.shader.BlockBuffer;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.Primitive;

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
     * @throws RenderBackendException If there is an error setting buffer data
     */
    public void createVBOs(Mesh mesh) throws RenderBackendException;

    /**
     * Creates buffer objects for the uniform blocks, buffers are allocated and names stored in uniformBlocks
     * 
     * @param uniformBlocks uniform block buffers to create buffer objects for, or null
     * @throws RenderBackendException
     */
    public void createUBOs(BlockBuffer[] uniformBlocks) throws RenderBackendException;

    /**
     * Creates VBO's and uploads data for the buffer(s) that are used by the primitive.
     * 
     * @param primitive
     * @throws RenderBackendException
     */
    public void createVBOs(Primitive primitive) throws RenderBackendException;

    /**
     * Creates VBO's and uploads data for the buffer(s)
     * 
     * @param buffers
     * @throws RenderBackendException
     */
    public void createVBOs(ArrayList<Buffer> buffers) throws RenderBackendException;

    /**
     * Creates VBO's and uploads data for the buffer(s)
     * 
     * @param buffers
     * @throws RenderBackendException
     */
    public void createVBOs(Buffer[] buffers) throws RenderBackendException;

    /**
     * Creates VBO's for the specified Buffer
     * 
     * @param buffer
     * @throws RenderBackendException
     */
    public void createVBO(Buffer buffer) throws RenderBackendException;

    /**
     * Destroys the buffers if VBOs have been allocated.
     * 
     * @param buffers
     * @throws RenderBackendException
     */
    public void destroyVBOs(NucleusRenderer renderer, ArrayList<Buffer> buffers) throws RenderBackendException;
}
