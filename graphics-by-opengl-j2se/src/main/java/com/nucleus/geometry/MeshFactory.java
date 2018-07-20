package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RenderableNode;

/**
 * Factory method for creating Meshes, implement support for different types of Meshes in the implementations of
 * this interface and use with the NodeFactory.
 * The factory methods shall allocate buffers and shader programs as needed.
 * It shall be possible to render the returned mesh.
 * TODO - Make sure that meshes are not created by calling new Mesh() - use factory instead.
 * 
 * @author Richard Sahlin
 *
 */
public interface MeshFactory<T> {

    /**
     * Creates a mesh for the specified parent node and adds it to the Node
     * The scene node shall contain all data necessary for the creating of the mesh.
     * All resources needed for the mesh shall be fetched/loaded as needed.
     * 
     * @param renderer
     * @param parent The node that the mesh will belong to.
     * @return The mesh that can be rendered.
     * @throws IOException If an asset such as texture could not be loaded.
     * @throws GLException If there is a GL related error, for instance when setting VBO data
     * 
     */
    public T createMesh(NucleusRenderer renderer, RenderableNode<T> parent) throws IOException, GLException;

    /**
     * If a node containing custom mesh, currently the meshnode, is used the MeshCreator can be set to get callback
     * when the mesh shall be created.
     * 
     * TODO Shall custom mesh creator be used on all node types if set?
     * 
     * @param creator The implementation that shall create the custom mesh.
     */
    public void setMeshCreator(MeshCreator<T> creator);

    /**
     * For custom implementations of a mesh creator - this is used when implementation of MeshFactory finds a node
     * that is of type 'meshnode'
     *
     */
    public interface MeshCreator<T> {

        /**
         * Called by the MeshFactory when a meshnode is found. Implementations shall create and return the appropriate
         * mesh.
         * 
         * @param renderer
         * @param parent
         * @return
         * @throws IOException
         * @throws GLException
         */
        public T createCustomMesh(NucleusRenderer renderer, RenderableNode<T> parent) throws IOException, GLException;

    }

}
