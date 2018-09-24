package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.bounds.Bounds;
import com.nucleus.component.ComponentException;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.ShaderProgram;

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
         * @param gles
         * @param shapeBuilder
         * @return MeshBuilder that can be used to create Meshes
         * @throws ComponentException
         */
        public MeshBuilder<T> createMeshBuilder(GLES20Wrapper gles, ShapeBuilder shapeBuilder)
                throws IOException;

    }

    /**
     * Create a new empty instance of a mesh.
     * 
     * @return
     */
    public T createInstance();

    /**
     * Creates one mesh for the arguments supplied to this builder - vertexcount, texture, material and drawmode -
     * and adds the meshes to the parent node.
     * If a shapebuilder is specified it is called to build (populate) the mesh.
     * Creates UBOs and VBOs as configured.
     * This is the same as calling {@link #create()} and then adding the mesh to the parent.
     * 
     * @param parent The parent node where the Mesh will be added.
     * @return The mesh
     * @throws IllegalArgumentException If the needed arguments has not been set
     * @throws IOException If there is an error loading data, for instance texture
     * @throws GLException If there is a problem calling GL, for instance when setting VBO data
     */
    public void create(RenderableNode<T> parent) throws IOException, GLException;

    /**
     * Creates one mesh using the builder and returns it.
     * If a shapebuilder is specified it is called to build (populate) the mesh.
     * Creates UBOs and VBOs as configured.
     * 
     * @return The mesh
     * @throws IllegalArgumentException If the needed arguments has not been set
     * @throws IOException If there is an error loading data, for instance texture
     * @throws GLException If there is a problem calling GL, for instance when setting VBO data
     */
    public T create() throws IOException, GLException;

    /**
     * Returns the shader program that can be used to draw the mesh. This is normally only used when program to use
     * is not known.
     * For instance when loading nodes, or other scenarios where mesh type is known (but not program)
     * 
     * @return Shader program to use for drawing mesh.
     */
    public ShaderProgram createProgram();

    /**
     * Calculates the bounds covering this mesh - this may return null.
     * 
     * @return
     */
    public Bounds createBounds();

}
