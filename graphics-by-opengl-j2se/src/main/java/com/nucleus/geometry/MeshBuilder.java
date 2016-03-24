package com.nucleus.geometry;

import static com.nucleus.geometry.VertexBuffer.INDEXED_QUAD_VERTICES;
import static com.nucleus.geometry.VertexBuffer.QUAD_INDICES;
import static com.nucleus.geometry.VertexBuffer.STRIP_QUAD_VERTICES;
import static com.nucleus.geometry.VertexBuffer.XYZUV_COMPONENTS;
import static com.nucleus.geometry.VertexBuffer.XYZ_COMPONENTS;
import static com.nucleus.vecmath.Rectangle.HEIGHT;
import static com.nucleus.vecmath.Rectangle.WIDTH;
import static com.nucleus.vecmath.Rectangle.X;
import static com.nucleus.vecmath.Rectangle.Y;

import java.nio.ByteBuffer;

import com.nucleus.Error;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.vecmath.Rectangle;
import com.nucleus.vecmath.Rectangle.Mode;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public class MeshBuilder {

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
     * @param x
     * @param y
     * @param z
     * @param u
     * @param v
     * @param dest position will be set here, must contain at least pos + 5 values.
     * @param pos The index where data is written.
     */
    public static void setPositionUV(float x, float y, float z, float u, float v, float[] dest, int pos) {
        dest[pos++] = x;
        dest[pos++] = y;
        dest[pos++] = z;
        dest[pos++] = u;
        dest[pos++] = v;
    }

    /**
     * Sets 3 component position in the destination buffer.
     * This method is not speed efficient, only use when very few positions shall be set.
     * 
     * @param x
     * @param y
     * @param z
     * @param buffer
     */
    public static void setPosition(float x, float y, float z, ByteBuffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
    }

    /**
     * Builds an array containing the 4 vertices for XYZ components, to be indexed when drawing, ie drawn using
     * drawElements().
     * The vertices will be centered using translate, a value of 0 will be left/top aligned.
     * A value of -1/width will be centered horizontally.
     * Vertices are numbered clockwise from upper left, ie upper left, upper right, lower right, lower left.
     * 1 2
     * 4 3
     * 
     * @param rectangle x1, y1, width, height
     * @param vertexStride, number of floats to add from one vertex to the next. Usually 3 to allow XYZ storage,
     * increase if padding (eg for UV) is needed.
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] createQuadPositionsIndexed(Rectangle rectangle, int vertexStride, float z) {

        float[] values = rectangle.getValues();
        if (rectangle.getMode() != Mode.SIZE) {
            throw new IllegalArgumentException(Error.NOT_IMPLEMENTED.message);
        }

        float[] quadPositions = new float[vertexStride * 4];
        com.nucleus.geometry.MeshBuilder.setPosition(values[X], values[Y],
                z, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPosition(values[X] + values[WIDTH], values[Y], z, quadPositions,
                vertexStride);
        com.nucleus.geometry.MeshBuilder.setPosition(values[X] + values[WIDTH], values[Y] + values[HEIGHT],
                z, quadPositions,
                vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPosition(values[X], values[Y] + values[HEIGHT], z, quadPositions,
                vertexStride * 3);
        return quadPositions;
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
     * @param z The Z position
     * @param x axis offset, 0 will be left centered (assuming x axis is increasing to the right)
     * @param y axis offset, 0 will be top centered, (assuming y axis is increasing downwards)
     * @param vertexStride, number of floats to add from one vertex to the next. 5 for a packed array with xyz and uv
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] createQuadPositionsUV(float width, float height, float z, float x, float y) {

        float[] quadPositions = new float[XYZUV_COMPONENTS * 4];
        com.nucleus.geometry.MeshBuilder.setPositionUV(x, y, z, 0, 0, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPositionUV(width + x, y, z, 1, 0, quadPositions,
                XYZUV_COMPONENTS);
        com.nucleus.geometry.MeshBuilder.setPositionUV(width + x, height + y, z, 1, 1, quadPositions,
                XYZUV_COMPONENTS * 2);
        com.nucleus.geometry.MeshBuilder.setPositionUV(x, height + y, z, 0, 1, quadPositions,
                XYZUV_COMPONENTS * 3);

        return quadPositions;
    }

    /**
     * Builds a mesh with a specified number of indexed quads of GL_FLOAT type, the mesh must have an elementbuffer to
     * index the vertices. The index buffer will be built with indexes, this mesh shall be drawn using indexed mode.
     * Vertex buffer shall have storage for XYZ + UV.
     * 
     * @param mesh The mesh to build the buffers in, this is the mesh that can be rendered.
     * @param program The program to use when rendering the mesh, it is stored in the material
     * @param index The index of the first quad to build
     * @param quadCount Number of quads to build, this is NOT the vertex count.
     * @param quadPositions Array with x,y,z - this is set for each tile. Must contain data for 4 vertices.
     * @param attribute2Size Size per vertex for attribute buffer 2, this may be 0
     * 
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public static void buildQuadMeshIndexed(Mesh mesh, ShaderProgram program, int index, int quadCount,
            float[] quadPositions) {
        ElementBuilder.buildQuadBuffer(mesh.indices, mesh.indices.getCount() / QUAD_INDICES, 0);

        float[] vertices = new float[quadCount * INDEXED_QUAD_VERTICES * XYZ_COMPONENTS];
        int destPos = 0;
        for (int i = 0; i < quadCount; i++) {
            System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
            destPos += quadPositions.length;
        }
        mesh.attributes[BufferIndex.VERTICES.index].setPosition(vertices, 0, index * INDEXED_QUAD_VERTICES,
                quadCount * INDEXED_QUAD_VERTICES);
    }

    /**
     * Builds the position and UV for one Quad, before calling this method the indexbuffer must be built if needed.
     * 
     * @param mesh
     * @param program
     * @param index
     */
    public static void buildQuad(Mesh mesh, ShaderProgram program, int index, float[] quadPositions) {
        float[] vertices = new float[INDEXED_QUAD_VERTICES * XYZ_COMPONENTS];
        int destPos = 0;
        System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
        mesh.attributes[BufferIndex.VERTICES.index].setPosition(vertices, 0, index * INDEXED_QUAD_VERTICES,
                INDEXED_QUAD_VERTICES);

    }

    /**
     * This method is deprecated, do not create attribute buffers in the builder.
     * Builds a quad mesh using a fan, the mesh can be rendered using glDrawArrays
     * 
     * @param mesh
     * @param program The program to use for the material in the mesh
     * @param quadPositions
     * @param attribute2Size
     */
    @Deprecated
    public static void buildQuadMeshFan(Mesh mesh, ShaderProgram program, float[] quadPositions, int attribute2Size) {
        int attributeBuffers = 1;
        if (attribute2Size > 0) {
            attributeBuffers = 2;
        }
        VertexBuffer[] attributes = new VertexBuffer[attributeBuffers];
        attributes[BufferIndex.VERTICES.index] = new VertexBuffer(STRIP_QUAD_VERTICES, XYZ_COMPONENTS,
                XYZUV_COMPONENTS, GLES20.GL_FLOAT);
        if (attributeBuffers > 1) {
            attributes[BufferIndex.ATTRIBUTES.index] = program.createAttributeBuffer(STRIP_QUAD_VERTICES, mesh);
        }
        attributes[BufferIndex.VERTICES.index].setPositionUV(quadPositions, 0, 0, STRIP_QUAD_VERTICES);
        Material material = new Material(program);
        mesh.setupVertices(attributes, material, null);
        mesh.setMode(GLES20.GL_TRIANGLE_FAN);
        program.setupUniforms(mesh);
    }

    /**
     * Prepares the UV coordinates for a Quad sprite.
     * Used by tiled objects to set the UV position to 1 or 0 so it can be multiplied by a fraction size to get
     * correct UV for a specific frame.
     * This method is chosen to move as much processing as possible to the GPU - the UV of each sprite could be
     * calculated at runtime but that would give a higher CPU impact when a large number of sprites are animated.
     * 
     * @param attributeData Array with attribute data where UV is stored.
     * @param index Index of first vertex
     * @param uIndex Index to UV in attribute data
     * @param stride Added to get to next vertex.
     */
    public static void prepareTiledUV(PropertyMapper mapper, float[] attributeData, int index) {
        int uvIndex = mapper.UV_INDEX;
        int stride = mapper.ATTRIBUTES_PER_VERTEX;

        index = index * mapper.ATTRIBUTES_PER_VERTEX * ShaderProgram.VERTICES_PER_SPRITE;
        attributeData[index + uvIndex] = 0;
        attributeData[index + uvIndex + 1] = 0;
        index += stride;
        attributeData[index + uvIndex] = 1;
        attributeData[index + uvIndex + 1] = 0;
        index += stride;
        attributeData[index + uvIndex] = 1;
        attributeData[index + uvIndex + 1] = 1;
        index += stride;
        attributeData[index + uvIndex] = 0;
        attributeData[index + uvIndex + 1] = 1;
    }
}
