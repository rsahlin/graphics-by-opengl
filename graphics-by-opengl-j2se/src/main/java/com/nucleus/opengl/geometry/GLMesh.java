package com.nucleus.opengl.geometry;

import java.io.IOException;

import com.nucleus.Backend.DrawMode;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.bounds.Bounds;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.MeshBuilder;
import com.nucleus.geometry.shape.ShapeBuilder;
import com.nucleus.io.ExternalReference;
import com.nucleus.opengl.GLPipeline;
import com.nucleus.opengl.shader.GLShaderProgram;
import com.nucleus.opengl.shader.TranslateProgram;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.RenderableNode;
import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.Texture2D;

/**
 * Mesh using the GLES/GL buffers
 *
 */
public class GLMesh extends Mesh {

    /**
     * Builder for meshes
     *
     * @param <T>
     */
    public static class Builder<T extends Mesh> implements MeshBuilder<Mesh> {

        protected NucleusRenderer renderer;
        protected Texture2D texture;
        protected Material material;
        protected int[] attributesPerVertex;
        protected int vertexCount = -1;
        protected int indiceCount = 0;
        /**
         * Extra attributes to allocate for each vertex
         */
        protected int vertexStride;
        /**
         * Optional builder parameter that can be used to determine number of vertices.
         */
        protected int objectCount = 1;
        protected ElementBuffer.Type indiceBufferType = Type.SHORT;
        protected DrawMode mode;
        protected ShapeBuilder shapeBuilder;

        /**
         * Creates a new builder
         * 
         * @param renderer
         * @throws IllegalArgumentException If gles is null
         */
        public Builder(NucleusRenderer renderer) {
            if (renderer == null) {
                throw new IllegalArgumentException("Renderer may not be null");
            }
            this.renderer = renderer;
        }

        @Override
        public Builder<T> setObjectCount(int objectCount) {
            this.objectCount = objectCount;
            return this;
        }

        @Override
        public Builder<T> setMode(DrawMode mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public Builder<T> setAttributesPerVertex(int[] sizePerVertex) {
            this.attributesPerVertex = sizePerVertex;
            return this;
        }

        @Override
        public Builder<T> setTexture(Texture2D texture) {
            this.texture = texture;
            return this;
        }

        /**
         * 
         * @param vertexStride Extra attributes to allocate per vertex, if a value larger than 0 is specified then this
         * number of attributes will be added to the attributes allocated for the mesh (for each vertex)
         * @return
         */
        public Builder<T> setVertexStride(int vertexStride) {
            this.vertexStride = vertexStride;
            return this;
        }

        @Override
        public void create(RenderableNode<Mesh> parent) throws IOException, BackendException {
            if (parent == null) {
                throw new IllegalArgumentException("Parent node may not be null when creating mesh");
            }
            parent.addMesh(create());
        }

        @Override
        public T create() throws IOException, BackendException {
            validate();
            T mesh = (T) createInstance();
            mesh.createMesh(texture, attributesPerVertex, material, vertexCount, indiceCount, mode);
            if (shapeBuilder != null) {
                shapeBuilder.build(mesh);
            }
            if (com.nucleus.renderer.Configuration.getInstance().isUseVBO()) {
                renderer.getBufferFactory().createVBOs(mesh);
            }
            return mesh;
        }

        @Override
        public GraphicsPipeline createPipeline() {
            // Default is to create a translate program, this will use an indexer so that creating 2D objects is
            // possible.
            // this is used mainly for ui elements
            GLShaderProgram shader = renderer.getAssets().getProgram(renderer, new TranslateProgram(texture));
            return new GLPipeline(renderer, shader, material);
        }

        @Override
        public Mesh createInstance() {
            return new GLMesh();
        }

        @Override
        public Builder<T> setTexture(ExternalReference textureRef) throws IOException {
            this.texture = renderer.getAssets().getTexture(renderer, BaseImageFactory.getInstance(),
                    textureRef);
            return this;
        }

        @Override
        public Builder<T> setArrayMode(DrawMode mode, int vertexCount, int vertexStride) {
            this.vertexCount = vertexCount;
            this.mode = mode;
            this.vertexStride = vertexStride;
            return this;
        }

        @Override
        public Builder<T> setElementMode(DrawMode mode, int vertexCount, int vertexStride, int indiceCount) {
            this.indiceCount = indiceCount;
            this.vertexCount = vertexCount;
            this.mode = mode;
            this.vertexStride = vertexStride;
            return this;
        }

        @Override
        public Builder<T> setMaterial(Material material) {
            this.material = material;
            return this;
        }

        @Override
        public Builder<T> setShapeBuilder(ShapeBuilder shapeBuilder) {
            this.shapeBuilder = shapeBuilder;
            return this;
        }

        /**
         * Checks that the needed arguments has been set
         */
        protected void validate() {
            if ((attributesPerVertex == null) || texture == null || vertexCount <= 0 || mode == null
                    || material == null) {
                throw new IllegalArgumentException(
                        "Missing argument when creating mesh: texture=" + texture + ", vertexcount="
                                + vertexCount + ", mode=" + mode + ", material=" + material + ", attributesPerVertex="
                                + attributesPerVertex);
            }
        }

        /**
         * Calculates the bounds covering this mesh.
         * 
         * @return
         */
        @Override
        public Bounds createBounds() {
            return null;
        }

        @Override
        public Texture2D getTexture() {
            return texture;
        }

        @Override
        public Material getMaterial() {
            return material;
        }

        @Override
        public ShapeBuilder getShapeBuilder() {
            return shapeBuilder;
        }

    }

    /**
     * Creates a new empty mesh, the attribute/index buffers must be prepared before rendering can take place.
     */
    protected GLMesh() {
        super();
    }

    /**
     * Creates a shallow copy of the source mesh, only the serialized values are copied, id and textureRef.
     * 
     * @param source
     */
    protected GLMesh(Mesh source) {
        setId(source.getId());
        textureRef = source.getTextureRef();
    }

    /**
     * Creates a Builder to create a mesh that can be rendered in a Node
     * 
     * @param renderer
     * @param maxVerticeCount
     * @param material
     * @param program
     * @param texture
     * @param shapeBuilder
     * @param mode
     * @return
     */
    public static Builder<Mesh> createBuilder(NucleusRenderer renderer, int maxVerticeCount, Material material,
            GLShaderProgram program, Texture2D texture, ShapeBuilder shapeBuilder, DrawMode mode) {
        GLMesh.Builder<Mesh> builder = new GLMesh.Builder<Mesh>(renderer);
        builder.setTexture(texture);
        builder.setMaterial(material);
        builder.setArrayMode(mode, maxVerticeCount, 0);
        builder.setShapeBuilder(shapeBuilder).setAttributesPerVertex(program.getAttributeSizes());
        return builder;
    }

}
