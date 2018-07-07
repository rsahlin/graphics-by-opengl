package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.bounds.Bounds;
import com.nucleus.component.ComponentException;
import com.nucleus.geometry.Mesh.Builder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class MeshBuilder<T> {

    /**
     * Interface for creating mesh builders - use this to create a mesh that can be used on the specified node, for
     * instance when loading nodes or when creating meshes to be used in a component.
     * 
     * @param <T>
     */
    public interface MeshBuilderFactory<T> {
        /**
         * Creates a mesh builder that can be used to create a mesh for the node or component.
         * Subclasses may need to override this to create the necessary Mesh.Builder
         * 
         * @param renderer
         * @param parent
         * @param count Number of objects to set the builder to create
         * @param shapeBuilder
         * @return
         * @throws ComponentException
         */
        public Builder<?> createMeshBuilder(NucleusRenderer renderer, Node parent, int count, ShapeBuilder shapeBuilder)
                throws IOException;

    }

    /**
     * Create a new empty instance of a mesh.
     * 
     * @return
     */
    protected abstract T createInstance();

    /**
     * Creates the mesh for the arguments supplied to this builder - vertexcount, texture, material and drawmode.
     * If a shapebuilder is specified it is called to build (populate) the mesh.
     * Creates UBOs and VBOs as configured.
     * 
     * @return The mesh
     * @throws IllegalArgumentException If the needed arguments has not been set
     * @throws IOException If there is an error loading data, for instance texture
     * @throws GLException If there is a problem calling GL, for instance when setting VBO data
     */
    public abstract T create() throws IOException, GLException;

    /**
     * Calculates the bounds covering this mesh - this may return null.
     * 
     * @return
     */
    public abstract Bounds createBounds();

}
