package com.nucleus.geometry;

import static com.nucleus.vecmath.Rectangle.HEIGHT;
import static com.nucleus.vecmath.Rectangle.WIDTH;
import static com.nucleus.vecmath.Rectangle.X;
import static com.nucleus.vecmath.Rectangle.Y;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.Rectangle;

/**
 * Use this builder to create rectangles in a mesh.
 * Will use indexbuffer and vertexbuffer, to use resulting mesh draw using elements.
 * 
 *
 */
public class RectangleShapeBuilder extends ElementBuilder {

    public static final float DEFAULT_Z = 0;
    /**
     * Default UV coordinates for a full texture frame
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

    public static class RectangleConfiguration extends Configuration {

        /**
         * Inits the builder to create rectangles with specified width and height, each rectangle will have UV covering
         * the whole quad.
         * 
         * @param width
         * @param height
         * @param z
         * @param count Number of rectangles
         * @param startVertex
         */
        public RectangleConfiguration(float width, float height, float z, int count, int startVertex) {
            super(count * QUAD_VERTICES, startVertex);
            this.rectangle = new Rectangle(-width / 2, height / 2, width, height);
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
        public RectangleConfiguration(Rectangle rectangle, float z, int count, int startVertex) {
            super(count * QUAD_VERTICES, startVertex);
            this.rectangle = rectangle;
            this.z = z;
        }

        /**
         * Inits the builder to only create indexbuffer, use this for meshes where the rectangle sizes are created
         * later.
         * 
         * @param count Number of rectangles
         * @param startVertex Start vertex for the builder
         */
        public RectangleConfiguration(int count, int startVertex) {
            super(count * QUAD_VERTICES, startVertex);
        }

        /**
         * Returns the number of rectangles
         * 
         * @return
         */
        public int getRectangleCount() {
            return vertexCount >>> 2;
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

        protected Rectangle rectangle;
        protected float z;
        /**
         * Set to true to add vertex index for each vertex in the quad, ie the first vertex will have index 0, the next
         * 1 and so on.
         * The index is stored after vertex xyz.
         */
        protected boolean enableVertexIndex = false;
    }

    private RectangleConfiguration configuration;

    public RectangleShapeBuilder(RectangleConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets 3 component position in the destination array and the quad vertex index.
     * The vertexIndex is used to keep track of which of the 4 vertices is processed, for instance when
     * setting up uv coordinates for a UV frame.
     * This method is not speed efficient, only use when very few positions shall be set.
     * 
     * @param vertexIndex The vertex index in the quad - 0 to 3, this is stored AFTER xyz
     * @param x
     * @param y
     * @param z
     * @param dest position will be set here, must contain at least pos + 3 values.
     * @param pos The index where data is written.
     */
    public static void setPosition(int vertexIndex, float x, float y, float z, float[] dest, int pos) {
        dest[pos++] = x;
        dest[pos++] = y;
        dest[pos++] = z;
        dest[pos++] = vertexIndex;
    }

    /**
     * Sets 3 component position in the destination array.
     * This method is not speed efficient, only use when very few positions shall be set.
     * 
     * @param x
     * @param y
     * @param z
     * @param dest position will be set here, must contain at least pos + 3 values.
     * @param pos The index where data is written.
     */
    public static void setPosition(float x, float y, float z, float[] dest, int pos) {
        dest[pos++] = x;
        dest[pos++] = y;
        dest[pos++] = z;
    }

    /**
     * Sets 3 component position plus uv in the destination array.
     * This method is not speed efficient, only use when very few positions shall be set.
     * For instance when creating one quad.
     * 
     * @param vertexIndex The vertex index in the quad - 0 to 3, this is stored AFTER xyz
     * @param x
     * @param y
     * @param z
     * @param u
     * @param v
     * @param dest position will be set here, must contain at least pos + 5 values.
     * @param pos The index where data is written.
     */
    public static void setPositionUV(int vertexIndex, float x, float y, float z, float u, float v, float[] dest,
            int pos) {
        dest[pos++] = x;
        dest[pos++] = y;
        dest[pos++] = z;
        dest[pos++] = vertexIndex;
        dest[pos++] = u;
        dest[pos++] = v;
    }

    /**
     * Sets 3 component position plus uv in the destination array.
     * This method is not speed efficient, only use when very few positions shall be set.
     * For instance when creating one quad.
     * 
     * @param x
     * @param y
     * @param z
     * @param u
     * @param v
     * @param dest position will be set here, must contain at least pos + 5 values.
     * @param pos The index where data is written.
     */
    public static void setPositionUV(float x, float y, float z, float u, float v, float[] dest,
            int pos) {
        dest[pos++] = x;
        dest[pos++] = y;
        dest[pos++] = z;
        dest[pos++] = u;
        dest[pos++] = v;
    }

    @Override
    public void build(Mesh mesh) {
        AttributeBuffer attributes = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        int stride = attributes.getFloatStride();
        float[] data = new float[stride * QUAD_VERTICES];
        if (configuration.rectangle != null) {
            createQuadArray(configuration.rectangle, mesh.getTexture(Texture2D.TEXTURE_0), stride,
                    configuration.z, configuration.enableVertexIndex, data);
        }
        int startIndex = configuration.startVertex * stride;
        int count = configuration.getRectangleCount();
        if (data != null) {
            int components = data.length / QUAD_VERTICES;
            stride = stride * QUAD_VERTICES;
            for (int i = 0; i < count; i++) {
                attributes.setComponents(data, components, 0, startIndex, QUAD_VERTICES);
                startIndex += stride;
            }
        }
        buildElements(mesh, count, configuration.startVertex);
    }

    /**
     * Creates an array of values that define the quad attribute values using the texture.
     * If vertex stride > 4 and texture type is not untextured then UV array is created.
     * 
     * @param rectangle Size of quad
     * @param texture Texture or null
     * @param vertexStride Number of values between vertices
     * @param z Z axis value for quad.
     * @param useVertexIndex If true then the index into the quad (0 - 3) is added after xyz.
     * @destination Values where quad array positions, and optional uv, are written.
     */
    public static void createQuadArray(Rectangle rectangle, Texture2D texture, int vertexStride, float z,
            boolean useVertexIndex, float[] destination) {
        float[] values = rectangle.getValues();
        // TODO Should it be possible to pass UV to this method?
        float[] uvCoordinates = null;
        if (vertexStride > 4 && texture != null && texture.textureType != TextureType.Untextured) {
            uvCoordinates = createUVCoordinates(texture);
        }
        if (useVertexIndex) {
            createQuadArrayVertexIndex(values, z, vertexStride, uvCoordinates, destination);
        } else {
            createQuadArray(values, z, vertexStride, uvCoordinates, destination);
        }
    }

    /**
     * Creates an array of vertex position including the index of the vertex in the quad, and optional UV coordinates.
     * The index in the vertex can be used to calculate the UV positions from a UV frame, so that only the frame number
     * needs to be specified.
     * 
     * @param values x, y, width, height of quad. X, Y is upper left corner.
     * @param z
     * @param vertexStride
     * @param uv Optional UV or null if not used
     * @param destination Result is written here, must contain 4 * vertexStride values
     */
    protected static void createQuadArrayVertexIndex(float[] values, float z, int vertexStride, float[] uv,
            float[] destination) {
        if (uv != null) {
            setPositionUV(0, values[X], values[Y], z, uv[0], uv[1], destination, 0);
            setPositionUV(1, values[X] + values[WIDTH], values[Y], z, uv[2], uv[3],
                    destination,
                    vertexStride);
            setPositionUV(2, values[X] + values[WIDTH], values[Y] - values[HEIGHT],
                    z, uv[4], uv[5], destination, vertexStride * 2);
            setPositionUV(3, values[X], values[Y] - values[HEIGHT], z, uv[6], uv[7],
                    destination,
                    vertexStride * 3);
        } else {
            setPosition(0, values[X], values[Y], z, destination, 0);
            setPosition(1, values[X] + values[WIDTH], values[Y], z,
                    destination, vertexStride);
            setPosition(2, values[X] + values[WIDTH], values[Y] - values[HEIGHT], z,
                    destination, vertexStride * 2);
            setPosition(3, values[X], values[Y] - values[HEIGHT], z,
                    destination, vertexStride * 3);
        }
    }

    /**
     * Creates an array of vertex position and optional UV coordinates.
     * 
     * @param values x, y, width, height of quad. X, Y is upper left corner.
     * @param z
     * @param vertexStride
     * @param uv Optional UV or null if not used
     * @param destination Result is written here, must contain 4 * vertexStride values
     */
    protected static void createQuadArray(float[] values, float z, int vertexStride, float[] uv, float[] destination) {
        if (uv != null) {
            setPositionUV(values[X], values[Y], z, uv[0], uv[1], destination, 0);
            setPositionUV(values[X] + values[WIDTH], values[Y], z, uv[2], uv[3],
                    destination,
                    vertexStride);
            setPositionUV(values[X] + values[WIDTH], values[Y] - values[HEIGHT],
                    z, uv[4], uv[5], destination, vertexStride * 2);
            setPositionUV(values[X], values[Y] - values[HEIGHT], z, uv[6], uv[7],
                    destination,
                    vertexStride * 3);
        } else {
            setPosition(values[X], values[Y], z, destination, 0);
            setPosition(values[X] + values[WIDTH], values[Y], z,
                    destination, vertexStride);
            setPosition(values[X] + values[WIDTH], values[Y] - values[HEIGHT], z,
                    destination, vertexStride * 2);
            setPosition(values[X], values[Y] - values[HEIGHT], z,
                    destination, vertexStride * 3);
        }
    }

    /**
     * creates the uv coordinates for the texture, if texture does not use UV, for instance for type
     * {@link TextureType#Untextured} then null is returned.
     * 
     * @param texture
     * @return Array with UV values or null if not supported
     */
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
            case Untextured:
            default:
                return null;
        }
    }

    @Override
    public void buildElements(Mesh mesh, int count, int startVertex) {
        // Check if indicebuffer shall be built
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices != null) {
            switch (mesh.getMode()) {
                case LINES:
                    buildQuadLineBuffer(indices, count, startVertex);
                    break;
                case TRIANGLES:
                    buildQuadBuffer(indices, count, startVertex);
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented for " + mesh.getMode());
            }
        }
    }

}
