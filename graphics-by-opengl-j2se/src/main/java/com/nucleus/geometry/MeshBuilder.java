package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.bounds.Bounds;
import com.nucleus.component.ComponentException;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RenderableNode;

/**
 * Interface for Mesh builders
 * 
 * @author Richard Sahlin
 *
 */
public interface MeshBuilder<T> {

    /**
     * Interface for creating mesh builders - use this to create a mesh that can be used on the specified node, for
     * instance when loading nodes or when creating meshes to be used in a component.
     * 
     * @param <T>
     */
    public interface MeshBuilderFactory<T> {
        /**
         * Creates a mesh builder that can be used to create a mesh for the node or component.
         * Subclasses may need to override this to create the necessary MeshBuilder
         * 
         * @param renderer
         * @param shapeBuilder
         * @return MeshBuilder that can be used to create Meshes
         * @throws ComponentException
         */
        public MeshBuilder<T> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder shapeBuilder)
                throws IOException;

    }

    /**
     * Create a new empty instance of a mesh.
     * 
     * @return
     */
    public T createInstance();

    /**
     * Creates the mesh for the arguments supplied to this builder - vertexcount, texture, material and drawmode.
     * If parent is supplied the mesh will be added to it.
     * If a shapebuilder is specified it is called to build (populate) the mesh.
     * Creates UBOs and VBOs as configured.
     * 
     * @param parent The parent node where the Mesh will be added.
     * @return The mesh
     * @throws IllegalArgumentException If the needed arguments has not been set
     * @throws IOException If there is an error loading data, for instance texture
     * @throws GLException If there is a problem calling GL, for instance when setting VBO data
     */
    public T create(RenderableNode<T> parent) throws IOException, GLException;

    /**
     * Calculates the bounds covering this mesh - this may return null.
     * 
     * @return
     */
    public Bounds createBounds();

}
