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
public class RectangleShapeBuilder extends ShapeBuilder {

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
         * @param count Number of rectangles
         * @param startVertex Start vertex for the builder
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
        AttributeBuffer attributes = mesh.getVerticeBuffer(BufferIndex.VERTICES);
        int stride = attributes.getFloatStride();
        float[] data = new float[stride * QUAD_VERTICES];
        if (configuration.rectangle != null) {
            createQuadArray(configuration.rectangle, mesh.getTexture(Texture2D.TEXTURE_0), stride,
                    configuration.z, data);
        }
        int startIndex = configuration.startVertex * stride;
        if (data != null) {
            int components = data.length / QUAD_VERTICES;
            stride = stride * QUAD_VERTICES;
            for (int i = 0; i < configuration.count; i++) {
                attributes.setComponents(data, components, 0, startIndex, QUAD_VERTICES);
                startIndex += stride;
            }
        }
        // Check if indicebuffer shall be built
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices != null) {
            switch (mesh.getMode()) {
                case LINES:
                ElementBuilder.buildQuadLineBuffer(indices, configuration.count, configuration.startVertex);
                break;
                case TRIANGLES:
                ElementBuilder.buildQuadBuffer(indices, configuration.count, configuration.startVertex);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + mesh.getMode());
            }
        }
    }

    /**
     * Creates an array of values that define the quad attribute values using the texture.
     * If vertex stride > 4 and texture type is not untextured then UV array is created.
     * 
     * @param rectangle Size of quad
     * @param texture Texture or null
     * @param vertexStride Number of values between vertices
     * @param z Z axis value for quad.
     * @destination Values where quad array positions, and optional uv, are written.
     */
    public static void createQuadArray(Rectangle rectangle, Texture2D texture, int vertexStride, float z,
            float[] destination) {
        float[] values = rectangle.getValues();
        // TODO Should it be possible to pass UV to this method?
        if (vertexStride > 4 && texture != null && texture.textureType != TextureType.Untextured) {
            createQuadArray(values, z, vertexStride, createUVCoordinates(texture), destination);
        } else {
            createQuadArray(values, z, vertexStride, null, destination);
        }
    }

    /**
     * Creates an array of values that define the quad attribute values using the texture.
     * If vertex stride > 4 and texture type is not untextured then UV array is created.
     * 
     * @param rectangle Size of quad
     * @param texture Texture or null
     * @param vertexStride Number of values between vertices
     * @param z Z axis value for quad.
     * @param destination The created array with quad positions, must contain 4 * vertexStride values
     */
    public static void createQuadArray(float[] values, Texture2D texture, int vertexStride, float z,
            float[] destination) {
        if (vertexStride > 4 && texture != null && texture.textureType != TextureType.Untextured) {
            createQuadArray(values, z, vertexStride, createUVCoordinates(texture), destination);
        } else {
            createQuadArray(values, z, vertexStride, null, destination);
        }
    }

    /**
     * Creates an array of vertex and optional UV coordinates.
     * 
     * @param values x, y, width, height of quad. X, Y is upper left corner.
     * @param z
     * @param vertexStride
     * @param uv Optional UV or null if not used
     * @param destination Result is written here, must contain 4 * vertexStride values
     */
    protected static void createQuadArray(float[] values, float z, int vertexStride, float[] uv,
            float[] destination) {
        if (uv != null) {
            com.nucleus.geometry.MeshBuilder.setPositionUV(values[X], values[Y], z, uv[0], uv[1], destination, 0);
            com.nucleus.geometry.MeshBuilder.setPositionUV(values[X] + values[WIDTH], values[Y], z, uv[2], uv[3],
                    destination,
                    vertexStride);
            com.nucleus.geometry.MeshBuilder.setPositionUV(values[X] + values[WIDTH], values[Y] - values[HEIGHT],
                    z, uv[4], uv[5], destination, vertexStride * 2);
            com.nucleus.geometry.MeshBuilder.setPositionUV(values[X], values[Y] - values[HEIGHT], z, uv[6], uv[7],
                    destination,
                    vertexStride * 3);
        } else {
            com.nucleus.geometry.MeshBuilder.setPosition(values[X], values[Y], z, destination, 0);
            com.nucleus.geometry.MeshBuilder.setPosition(values[X] + values[WIDTH], values[Y], z,
                    destination, vertexStride);
            com.nucleus.geometry.MeshBuilder.setPosition(values[X] + values[WIDTH], values[Y] - values[HEIGHT], z,
                    destination, vertexStride * 2);
            com.nucleus.geometry.MeshBuilder.setPosition(values[X], values[Y] - values[HEIGHT], z,
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

}
