package com.nucleus.geometry;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class MeshBuilder<T> {

    /**
     * Create a new empty instance of a mesh.
     * 
     * @return
     */
    protected abstract T createMesh();

}
