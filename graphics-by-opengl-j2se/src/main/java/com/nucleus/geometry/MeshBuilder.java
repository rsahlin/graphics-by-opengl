package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.bounds.Bounds;
import com.nucleus.component.ComponentException;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.shader.ShaderProgram;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderBackendException;
import com.nucleus.scene.RenderableNode;
import com.nucleus.texturing.Texture2D;

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
        public MeshBuilder<T> createMeshBuilder(NucleusRenderer renderer, ShapeBuilder<T> shapeBuilder)
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
     * @throws RenderBackendException If there is a problem calling GL, for instance when setting VBO data
     */
    public void create(RenderableNode<T> parent) throws IOException, RenderBackendException;

    /**
     * Creates one mesh using the builder and returns it.
     * If a shapebuilder is specified it is called to build (populate) the mesh.
     * Creates UBOs and VBOs as configured.
     * 
     * @return The mesh
     * @throws IllegalArgumentException If the needed arguments has not been set
     * @throws IOException If there is an error loading data, for instance texture
     * @throws RenderBackendException If there is a problem calling GL, for instance when setting VBO data
     */
    public T create() throws IOException, RenderBackendException;

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

    /**
     * Sets the shapebuilder to be used when building mesh shape(s)
     * 
     * @param shapeBuilder The shape builder, or null
     * @return
     */
    public MeshBuilder<T> setShapeBuilder(ShapeBuilder shapeBuilder);

    /**
     * Set mode and vertex count for array based drawing - this will not use element (indice) buffer.
     * ie glDrawArrays() will be used to draw the mesh.
     * 
     * @param mode The drawmode for vertices
     * @param vertexCount Number of vertices
     * @param vertexStride Extra attributes to allocate per vertex, if a value larger than 0 is specified then this
     * number of attributes will be added to the attributes allocated for the mesh (for each vertex)
     * @return
     */
    public MeshBuilder<T> setArrayMode(DrawMode mode, int vertexCount, int vertexStride);

    /**
     * Fetches the texture and stores as texture to be used when creating mesh
     * 
     * @param textureRef
     * @throws IOException If the texture could not be loaded
     */
    public MeshBuilder<T> setTexture(ExternalReference textureRef) throws IOException;

    /**
     * Set mode, vertexcount and element (indice) count. The created mesh will have vertexbuffer and indice buffer.
     * When drawn glDrawElements will be used.
     * 
     * @param mode
     * @param vertexCount
     * @param vertexStride Extra attributes to allocate per vertex, if a value larger than 0 is specified then this
     * number of attributes will be added to the attributes allocated for the mesh (for each vertex)
     * @param indiceCount
     */
    public MeshBuilder<T> setElementMode(DrawMode mode, int vertexCount, int vertexStride, int indiceCount);

    /**
     * Sets the material to be used in the mesh
     * 
     * @param material
     */
    public MeshBuilder<T> setMaterial(Material material);

    /**
     * Sets the number of objects the builder shall create mesh for, used for instance when mesh uses
     * batching/instancing, or is a geometryshader
     * 
     * @param objectCount Number of objects to create when building the mesh
     * @return
     */
    public MeshBuilder<T> setObjectCount(int objectCount);

    /**
     * Sets the drawmode for the mesh
     * 
     * @param mode
     * @return Meshbuilder
     */
    public MeshBuilder<T> setMode(DrawMode mode);

    /**
     * Sets the number of attributes (floats) per vertex to create for each buffer
     * 
     * @param sizePerVertex
     * @return
     */
    public MeshBuilder<T> setAttributesPerVertex(int[] sizePerVertex);

    /**
     * Sets the texture to use for the created mesh
     * 
     * @param texture
     * @return
     */
    public MeshBuilder<T> setTexture(Texture2D texture);

    /**
     * Returns the ShapeBuilder to use for creating vertices or null
     * 
     * @return
     */
    public ShapeBuilder getShapeBuilder();

    public Texture2D getTexture();

    public Material getMaterial();

}
