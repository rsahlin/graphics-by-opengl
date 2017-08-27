package com.nucleus.geometry;

import static com.nucleus.geometry.VertexBuffer.XYZUV_COMPONENTS;
import static com.nucleus.vecmath.Rectangle.HEIGHT;
import static com.nucleus.vecmath.Rectangle.WIDTH;
import static com.nucleus.vecmath.Rectangle.X;
import static com.nucleus.vecmath.Rectangle.Y;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
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
            data = createQuadArray(configuration.rectangle, mesh.getTexture(Texture2D.TEXTURE_0), stride,
                    configuration.z);
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

    /**
     * Creates an array of values that define the quad attribute values using the texture.
     * 
     * @param rectangle Size of quad
     * @param texture
     * @param vertexStride Number of values between vertices
     * @param z Z axis value for quad.
     * @return Array of values needed to render a quad.
     */

    public static float[] createQuadArray(Rectangle rectangle, Texture2D texture, int vertexStride, float z) {
        float[] values = rectangle.getValues();
        // TODO Should it be possible to pass UV to this method?
        if (vertexStride > 4) {
            return createQuadArrayUV(values, z, vertexStride, createUVCoordinates(texture));
        } else {
            return createQuadArray(values, z, vertexStride);
        }
    }

    /**
     * Creates an array of vertex and UV coordinates.
     * 
     * @param values x, y, width, height of quad. X, Y is upper left corner.
     * @param z
     * @param vertexStride
     * @param uv
     * @return Array with vertices and uv for a quad
     */
    protected static float[] createQuadArrayUV(float[] values, float z, int vertexStride, float[] uv) {
        float[] quadPositions = new float[vertexStride * 4];
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X], values[Y], z, uv[0], uv[1], quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X] + values[WIDTH], values[Y], z, uv[2], uv[3],
                quadPositions,
                vertexStride);
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X] + values[WIDTH], values[Y] - values[HEIGHT],
                z, uv[4], uv[5], quadPositions, vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPositionUV(values[X], values[Y] - values[HEIGHT], z, uv[6], uv[7],
                quadPositions,
                vertexStride * 3);
        return quadPositions;
    }

    /**
     * Creates an array of vertex coordinates.
     * 
     * @param values x, y, width, height of quad. X, Y is upper left corner.
     * @param z
     * @param vertexStride
     * @return
     */
    protected static float[] createQuadArray(float[] values, float z, int vertexStride) {
        float[] quadPositions = new float[vertexStride * 4];
        com.nucleus.geometry.MeshBuilder.setPosition(values[X], values[Y], z, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPosition(values[X] + values[WIDTH], values[Y], z,
                quadPositions, vertexStride);
        com.nucleus.geometry.MeshBuilder.setPosition(values[X] + values[WIDTH], values[Y] - values[HEIGHT], z,
                quadPositions, vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPosition(values[X], values[Y] - values[HEIGHT], z,
                quadPositions, vertexStride * 3);
        return quadPositions;
    }

    protected static float[] createUVCoordinates(Texture2D texture) {
        switch (texture.textureType) {
        case Texture2D:
            return UV_COORDINATES;
        case TiledTexture2D:
            TiledTexture2D t = (TiledTexture2D) texture;
            float maxU = (1f / (t.getTileWidth()));
            float maxV = (1f / (t.getTileHeight()));
            return new float[] { 0, 0, maxU, 0, maxU, maxV, 0, maxV };
        case UVTexture2D:
            // Should normally not allocate UV coordinates.
        case Untextured:
            // Should normally not allocate UV coordinates.
        default:
            throw new IllegalArgumentException("Should not use UV coordinates for texture: " + texture.textureType);
        }
    }

}
