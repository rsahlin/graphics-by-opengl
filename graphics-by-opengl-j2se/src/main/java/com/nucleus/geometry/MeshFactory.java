package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.io.ExternalReference;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.shader.ShaderProgram;

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
public interface MeshFactory {

    /**
     * Creates a mesh for the specified parent node. The scene node shall contain all data necessary for the creating of
     * the mesh.
     * All resources needed for the mesh shall be fetched/loaded as needed.
     * 
     * @param renderer
     * @param parent The node that the mesh will belong to.
     * @return The mesh that can be rendered.
     * @throws IOException If an asset such as texture could not be loaded.
     */
    public Mesh createMesh(NucleusRenderer renderer, Node parent) throws IOException;

    /**
     * 
     * @param renderer
     * @param program
     * @param material
     * @param textureRef
     * @return
     * @throws IOException
     */
    public Mesh createMesh(NucleusRenderer renderer, ShaderProgram program, Material material,
            ExternalReference textureRef,
            int vertexCount, int indiceCount)
            throws IOException;

}
