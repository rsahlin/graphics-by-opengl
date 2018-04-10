package com.nucleus.geometry;

import static com.nucleus.vecmath.Rectangle.HEIGHT;
import static com.nucleus.vecmath.Rectangle.WIDTH;
import static com.nucleus.vecmath.Rectangle.X;
import static com.nucleus.vecmath.Rectangle.Y;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.renderer.Window;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureParameter.Parameter;
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
     * Do not access directly, use {@link #getUV(Texture2D)} to get correct uv
     */
    private final static float[] UV_COORDINATES = new float[] { 0, 0, 1, 0, 1, 1, 0, 1 };
    /**
     * Default UV coordinates for a full texture frame, V flipped for inverted Y axis.
     * Do not access directly, use {@link #getUV(Texture2D)} to get correct uv
     */
    private final static float[] UVFLIPPED_COORDINATES = new float[] { 0, 1, 1, 1, 1, 0, 0, 0 };

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
    /**
     * Quad data stored here after created.
     */
    private float[] quadStoreage;

    public RectangleShapeBuilder(RectangleConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets the quad to build
     * 
     * @param quad The quad, 0 for first quad, 1 for second etc
     * @return
     */
    public RectangleShapeBuilder setStartQuad(int quad) {
        configuration.startVertex = quad * 4;
        return this;
    }

    /**
     * Sets the rectangle to use when creating positions
     * 
     * @param rectangle
     * @return
     */
    public RectangleShapeBuilder setRectangle(Rectangle rectangle) {
        configuration.rectangle = rectangle;
        return this;
    }

    /**
     * Sets the state of the vertex index flag, if enabled then the vertex index in the Quad (0 - 3) is stored
     * after position. Use this for texture types that calculate UV based on vertex index in quad, for instance tiled
     * texture.
     * 
     * @param enable
     * @return
     */
    public RectangleShapeBuilder setEnableVertexIndex(boolean enable) {
        configuration.enableVertexIndex = enable;
        return this;
    }

    /**
     * Sets 3 component position plus uv in the destination array.
     * This method is not speed efficient, only use when very few positions shall be set.
     * For instance when creating one quad.
     * 
     * @param vertexIndex The vertex index in the quad - 0 to 3, this is stored AFTER xyz
     * @param x
     * @param y
     * @param optional uv
     * @param dest Destination array
     * @param pos The index where data is written.
     */
    public void setPositionUV(int vertexIndex, float x, float y, float[] uv, float[] dest, int pos) {
        dest[pos++] = x;
        dest[pos++] = y;
        dest[pos++] = configuration.z;
        if (configuration.enableVertexIndex) {
            dest[pos++] = vertexIndex;
        }
        if (uv != null) {
            dest[pos++] = uv[vertexIndex * 2];
            dest[pos++] = uv[vertexIndex * 2 + 1];
        }
    }

    @Override
    public void build(Mesh mesh) {
        AttributeBuffer attributes = mesh.getAttributeBuffer(BufferIndex.VERTICES);
        int stride = attributes.getFloatStride();
        if (quadStoreage == null) {
            quadStoreage = new float[stride * QUAD_VERTICES];
        }
        if (configuration.rectangle != null) {
            createQuadArray(mesh.getTexture(Texture2D.TEXTURE_0), mesh.getMode(), stride, quadStoreage);
        }
        int startIndex = configuration.startVertex * stride;
        int count = configuration.getRectangleCount();
        for (int i = 0; i < count; i++) {
            attributes.setComponents(quadStoreage, stride, 0, startIndex, QUAD_VERTICES);
            startIndex += stride * QUAD_VERTICES;
        }
        attributes.setDirty(true);
        buildElements(mesh, count, configuration.startVertex);
    }

    /**
     * Creates an array of values that define the quad attribute values using the texture.
     * If vertex stride > 4 and texture type is not untextured then UV array is created.
     * 
     * @param texture Texture or null
     * @param mode The drawmode used for the quad
     * @param vertexStride Number of values between vertices
     * @destination Values where quad array positions, and optional uv, are written.
     */
    protected void createQuadArray(Texture2D texture, Mesh.Mode mode, int vertexStride, float[] destination) {
        float[] values = configuration.rectangle.getValues();
        float[] uvCoordinates = null;
        if (vertexStride > 4 && texture != null && texture.textureType != TextureType.Untextured) {
            uvCoordinates = new float[8];
            createUVCoordinates(texture, uvCoordinates);
        }
        createQuadArray(mode, values, vertexStride, uvCoordinates, destination);
    }

    /**
     * Creates an array of vertex positions from a rectangle, including the index of the vertex in the quad, and
     * optional UV coordinates.
     * The index in the vertex can be used to calculate the UV positions from a UV frame, so that only the frame number
     * needs to be specified. This is used by some texture types.
     * 
     * @param values x, y, width, height of quad. X, Y is upper left corner of created quad.
     * @param vertexStride
     * @param uv Optional UV or null if not used
     * @param useVertexIndex
     * @param destination Result is written here, must contain 4 * vertexStride values
     */
    protected void createQuadArray(Mesh.Mode mode, float[] values, int vertexStride, float[] uv, float[] destination) {
        switch (mode) {
            case TRIANGLES:
            case TRIANGLE_FAN:
                setPositionUV(0, values[X], values[Y], uv, destination, 0);
                setPositionUV(1, values[X] + values[WIDTH], values[Y], uv, destination, vertexStride);
                setPositionUV(2, values[X] + values[WIDTH], values[Y] - values[HEIGHT], uv, destination,
                        vertexStride * 2);
                setPositionUV(3, values[X], values[Y] - values[HEIGHT], uv, destination,
                        vertexStride * 3);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for mode " + mode);
        }
    }

    /**
     * creates the uv coordinates for the texture, if texture does not use UV, for instance for type
     * {@link TextureType#Untextured} then null is returned.
     * If texture type is Texture2D the texture parameters are checked and UV adopted to REPEAT if specified.
     * 
     * @param texture
     * @param destination
     */
    protected static void createUVCoordinates(Texture2D texture, float[] destination) {
        switch (texture.textureType) {
            case Texture2D:
            case DynamicTexture2D:
                Parameter[] params = texture.getTexParams().getParameters();
                createUVCoordinates(texture, params[TextureParameter.WRAP_S_INDEX],
                        params[TextureParameter.WRAP_T_INDEX], destination);
                break;
            case TiledTexture2D:
                TiledTexture2D t = (TiledTexture2D) texture;
                float maxU = (1f / (t.getTileWidth()));
                float maxV = (1f / (t.getTileHeight()));
                destination[0] = 0;
                destination[1] = 0;
                destination[2] = maxU;
                destination[3] = 0;
                destination[4] = maxU;
                destination[5] = maxV;
                destination[6] = 0;
                destination[7] = maxV;
                break;
            case UVTexture2D:
            case Untextured:
            default:
        }
    }

    protected static void createUVCoordinates(Texture2D texture, Parameter wrapS, Parameter wrapT,
            float[] destination) {
        float[] uv = getUV(texture);
        if (wrapS == Parameter.REPEAT || wrapT == Parameter.REPEAT) {
            float x = wrapS == Parameter.REPEAT ? (float) Window.getInstance().getWidth() / texture.getWidth() : 1f;
            float y = wrapT == Parameter.REPEAT ? (float) Window.getInstance().getHeight() / texture.getHeight() : 1f;
            int index = 0;
            while (index < uv.length) {
                destination[index] = uv[index++] * x;
                destination[index] = uv[index++] * y;
            }
        } else {
            System.arraycopy(uv, 0, destination, 0, uv.length);
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

    /**
     * Returns the coordinates for a rectangle texture based on the flipV flag in the texture.
     * Y axis will be inverted if flipV flag is set in the texture.
     * 
     * @param texture
     * @return
     */
    public static final float[] getUV(Texture2D texture) {
        if (texture.isFlipV()) {
            return UVFLIPPED_COORDINATES;
        }
        return UV_COORDINATES;
    }

}
