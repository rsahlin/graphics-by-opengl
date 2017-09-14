package com.nucleus.geometry;

/**
 * Defines methods for building a shape (surface) using a mesh, ie connecting vertices so that a shape is drawn.
 * 
 */
public abstract class ShapeBuilder {

    /**
     * Builds the specified shape(s) on the mesh.
     * 
     * @param mesh The mesh where the shape(s) will be built, the mesh must have all buffers created prior to calling
     * this method.
     */
    public abstract void build(Mesh mesh);

}
