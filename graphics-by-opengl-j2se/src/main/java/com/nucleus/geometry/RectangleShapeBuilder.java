package com.nucleus.geometry;

import static com.nucleus.geometry.VertexBuffer.XYZUV_COMPONENTS;

import com.nucleus.geometry.Mesh.BufferIndex;

public class RectangleShapeBuilder extends ShapeBuilder {

    /**
     * Number of vertices for an quad, works with indexed, triangle strip and triangle fan.
     */
    public final static int QUAD_VERTICES = 4;

    /**
     * Number of indices used for a quad when using indexbuffer.
     */
    public final static int QUAD_ELEMENTS = 6;

    public static class Configuration {

        public Configuration(float width, float height, float z, int count, int startVertex) {
            this.data = buildQuadPositionsUV(width, height, -width / 2, height / 2, z);
            this.count = count;
            this.startVertex = startVertex;
        }

        protected float[] data;
        protected int count = 1;
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
        VertexBuffer attributes = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        for (int i = 0; i < configuration.count; i++) {
            attributes.setPositionUV(configuration.data, 0, configuration.startVertex * attributes.getFloatStride(),
                    configuration.count * QUAD_VERTICES);

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
     * @param vertexStride, number of floats to add from one vertex to the next. 5 for a packed array with xyz and uv
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] buildQuadPositionsUV(float width, float height, float x, float y, float z) {

        float[] quadPositions = new float[XYZUV_COMPONENTS * 4];
        com.nucleus.geometry.MeshBuilder.setPositionUV(x, y, z, 0, 0, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPositionUV(width + x, y, z, 1, 0, quadPositions,
                XYZUV_COMPONENTS);
        com.nucleus.geometry.MeshBuilder.setPositionUV(width + x, y - height, z, 1, 1, quadPositions,
                XYZUV_COMPONENTS * 2);
        com.nucleus.geometry.MeshBuilder.setPositionUV(x, y - height, z, 0, 1, quadPositions,
                XYZUV_COMPONENTS * 3);

        return quadPositions;
    }

}
