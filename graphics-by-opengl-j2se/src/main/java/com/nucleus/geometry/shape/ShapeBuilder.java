package com.nucleus.geometry.shape;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.Mode;
import com.nucleus.texturing.Texture2D;

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
         * Set to true to add vertex index for each vertex in the quad, ie the first vertex will have index 0, the next
         * 1 and so on.
         * The index is stored after vertex xyz.
         */
        protected boolean enableVertexIndex = false;

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

        /**
         * Enable or disables the vertex index, of set to true then each vertex has the vertex index in the quad. 0 for
         * the first vertex, 1 for the next - up to 3.
         * 
         * @param enable
         */
        public void enableVertexIndex(boolean enable) {
            this.enableVertexIndex = enable;
        }

    }

    /**
     * Builds the specified shape(s) on the mesh.
     * 
     * @param attributes
     * @param texture
     * @param indices
     * @param mode
     */
    public abstract void build(AttributeBuffer attributes, Texture2D texture, ElementBuffer indices, GLESWrapper.Mode mode);
 
}
