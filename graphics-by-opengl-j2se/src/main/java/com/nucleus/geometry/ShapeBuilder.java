package com.nucleus.geometry;

/**
 * Defines methods for building a shape (surface) using a mesh, ie connecting vertices so that a shape is drawn.
 * This builder SHALL NOT create any buffer storage - that is done the {@link Mesh#Builder}
 * 
 */
public abstract class ShapeBuilder {

    /**
     * The shape builder configuration superclass, this shall be used by subclasses
     *
     */
    public static class Configuration {
        /**
         * Number of vertices to build shape for - NOTE this does not define the buffer storage that is
         * done in the {@link Mesh.Builder}
         */
        protected int vertexCount = 0;
        protected int startVertex = 0;
        
        /**
         * Inits the builder with the specified number of vertices and a start vertex index.
         * 
         * @param vertexCount Number of vertices to build shapes for
         * @param startVertex
         */
        public Configuration(int vertexCount, int startVertex) {
            this.vertexCount = vertexCount;
            this.startVertex = startVertex;
        }
    }

    /**
     * Builds the specified shape(s) on the mesh.
     * 
     * @param mesh The mesh where the shape(s) will be built, the mesh must have all buffers created prior to calling
     * this method.
     */
    public abstract void build(Mesh mesh);

}
