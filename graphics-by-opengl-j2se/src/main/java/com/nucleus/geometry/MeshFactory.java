package com.nucleus.geometry;

import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;

/**
 * Factory method for creating Meshes, implement support for different types of Meshes in the implementations of
 * this interface and use with the NodeFactory.
 * The factory methods shall allocate buffers and shader programs as needed.
 * It shall be possible to render the returned mesh.
 * 
 * @author Richard Sahlin
 *
 */
public interface MeshFactory {

    /**
     * Creates a mesh for the specified source node. The scene node shall contain all data necessary for the creating of
     * the mesh.
     * 
     * @param renderer
     * @param source
     * @param scene
     * @return The mesh that can be rendered.
     */
    public Mesh createMesh(NucleusRenderer renderer, Node source, RootNode scene);

}
