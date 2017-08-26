package com.nucleus.geometry;

import static com.nucleus.geometry.VertexBuffer.XYZUV_COMPONENTS;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.texturing.Texture2D;
import com.nucleus.vecmath.Rectangle;

/**
 * Use this builder to create rectangles in a mesh.
 * Will use indexbuffer and vertexbuffer, to use resulting mesh draw using elements.
 * 
 *
 */
public class RectangleShapeBuilder extends ShapeBuilder {

    /**
     * Default UV coordinates for a ful texture frame
     */
    public final static float[] UV_COORDINATES = new float[] { 0, 0, 1, 0, 1, 1, 0, 1 };

    /**
     * Number of vertices for an quad, works with indexed, triangle strip and triangle fan.
     */
    public final static int QUAD_VERTICES = 4;

    /**
     * Number of indices used for a quad when using indexbuffer.
     */
    public final static int QUAD_ELEMENTS = 6;

    public static class Configuration {

        /**
         * Inits the builder to create rectangles with specified width and height, each rectangle will have UV covering
         * the whole quad.
         * 
         * @param width
         * @param height
         * @param z
         * @param count
         * @param startVertex
         */
        public Configuration(float width, float height, float z, int count, int startVertex) {
            this.rectangle = new Rectangle(-width / 2, height / 2, width, height);
            this.count = count;
            this.startVertex = startVertex;
            this.z = z;
        }

        /**
         * Inits the builder to create rectangles with the specified Rectangle size
         * 
         * @param rectangle
         * @param z
         * @param count
         * @param startVertex
         */
        public Configuration(Rectangle rectangle, float z, int count, int startVertex) {
            this.rectangle = rectangle;
            this.count = count;
            this.startVertex = startVertex;
            this.z = z;
        }

        /**
         * Inits the builder to only create indexbuffer, use this for meshes where the rectangle sizes are created later
         * 
         * @param count
         * @param startVertex
         */
        public Configuration(int count, int startVertex) {
            this.count = count;
            this.startVertex = startVertex;
        }

        protected Rectangle rectangle;
        protected int count = 1;
        protected float z;
        /**
         * Destination vertex offset
         */
        protected int startVertex = 0;
    }


    private Configuration configuration;

    public RectangleShapeBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void build(Mesh mesh) {
        float[] data = null;
        VertexBuffer attributes = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        int stride = attributes.getFloatStride();
        if (configuration.rectangle != null) {
            data = mesh.getTexture(Texture2D.TEXTURE_0).createQuadArray(configuration.rectangle,
                    stride, configuration.z);
        }
        int startIndex = configuration.startVertex * stride;
        if (data != null) {
            stride = stride * QUAD_VERTICES;
            for (int i = 0; i < configuration.count; i++) {
                attributes.setPositionUV(data, 0, startIndex, QUAD_VERTICES);
                startIndex += stride;
            }
        }
        // Check if indicebuffer shall be built
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices != null) {
            ElementBuilder.buildQuadBuffer(indices, configuration.count, configuration.startVertex);
        }
    }

    /**
     * Builds an array for 4 vertices containing xyz and uv components, the array can be drawn
     * using GL_TRIANGLE_FAN
     * The vertices will be centered using translate, a value of 0 will be left/top aligned.
     * A value of -width/2 will be centered horizontally.
     * Vertices are numbered clockwise from upper left, ie upper left, upper right, lower right, lower left.
     * 1 2
     * 4 3
     * 
     * @param width width of quad in world coordinates
     * @param height height of quad in world coordinates
     * @param x X position of first corner
     * @param y Y position of first corner
     * @param z position
     * @param uv UV coordinates, or null to use default
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] buildQuadPositionsUV(float width, float height, float x, float y, float z, float[] uv) {
        if (uv == null) {
            uv = UV_COORDINATES;
        }
        float[] quadPositions = new float[XYZUV_COMPONENTS * 4];
        com.nucleus.geometry.MeshBuilder.setPositionUV(x, y, z, uv[0], uv[1], quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPositionUV(width + x, y, z, uv[2], uv[3], quadPositions,
                XYZUV_COMPONENTS);
        com.nucleus.geometry.MeshBuilder.setPositionUV(width + x, y - height, z, uv[4], uv[5], quadPositions,
                XYZUV_COMPONENTS * 2);
        com.nucleus.geometry.MeshBuilder.setPositionUV(x, y - height, z, uv[6], uv[7], quadPositions,
                XYZUV_COMPONENTS * 3);

        return quadPositions;
    }

}
