package com.nucleus.geometry;

import static com.nucleus.geometry.VertexBuffer.INDEXED_QUAD_VERTICES;
import static com.nucleus.geometry.VertexBuffer.QUAD_INDICES;
import static com.nucleus.geometry.VertexBuffer.STRIP_QUAD_VERTICES;
import static com.nucleus.geometry.VertexBuffer.XYZUV_COMPONENTS;
import static com.nucleus.geometry.VertexBuffer.XYZ_COMPONENTS;

import java.nio.ByteBuffer;

import com.nucleus.data.Anchor;
import com.nucleus.geometry.ElementBuffer.Mode;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.vecmath.Axis;

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
     * @param size Width and height of quad in world coordinates
     * @param anchor Anchor values for x,y and z
     * @param vertexStride, number of floats to add from one vertex to the next. Usually 3 to allow XYZ storage,
     * increase if padding (eg for UV) is needed.
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] buildQuadPositionsIndexed(float[] size, Anchor anchor, int vertexStride) {

        float[] translate = anchor.calcOffsets(size);
        float[] quadPositions = new float[vertexStride * 4];
        com.nucleus.geometry.MeshBuilder.setPosition(translate[Axis.X.index], translate[Axis.Y.index],
                translate[Axis.Z.index], quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPosition(size[Axis.WIDTH.index] + translate[Axis.X.index],
                translate[Axis.Y.index], translate[Axis.Z.index], quadPositions, vertexStride);
        com.nucleus.geometry.MeshBuilder.setPosition(size[Axis.WIDTH.index] + translate[Axis.X.index],
                size[Axis.HEIGHT.index] + translate[Axis.Y.index],
                translate[Axis.Z.index], quadPositions, vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPosition(translate[Axis.X.index],
                size[Axis.HEIGHT.index] + translate[Axis.Y.index],
                translate[Axis.Z.index], quadPositions, vertexStride * 3);
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
    public static float[] buildQuadPositionsUV(float width, float height, float z, float x, float y) {

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
     * Builds a mesh with a specified number of indexed quads of GL_FLOAT type, the mesh will have an elementbuffer to
     * index the vertices.
     * Vertex buffer will have storage for XYZ + UV.
     * 
     * @param mesh The mesh to build the buffers in, this is the mesh that can be rendered.
     * @param program The program to use when rendering the mesh, it is stored in the material
     * @param quadCount Number of quads to build, this is NOT the vertex count.
     * @param quadPositions Array with x,y,z - this is set for each tile. Must contain data for 4 vertices.
     * @param attribute2Size Size per vertex for attribute buffer 2, this may be 0
     * 
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public static void buildQuadMeshIndexed(Mesh mesh, ShaderProgram program, int quadCount, float[] quadPositions) {
        VertexBuffer[] attributes = new VertexBuffer[2];
        attributes[BufferIndex.VERTICES.index] = new VertexBuffer(quadCount * INDEXED_QUAD_VERTICES, XYZ_COMPONENTS,
                XYZ_COMPONENTS,
                GLES20.GL_FLOAT);
        attributes[1] = program.createAttributeBuffer(quadCount * INDEXED_QUAD_VERTICES);
        ElementBuffer indices = new ElementBuffer(Mode.TRIANGLES, QUAD_INDICES * quadCount, Type.SHORT);
        ElementBuilder.buildQuadBuffer(indices, indices.getCount() / QUAD_INDICES, 0);

        float[] vertices = new float[quadCount * INDEXED_QUAD_VERTICES * XYZ_COMPONENTS];
        int destPos = 0;
        for (int i = 0; i < quadCount; i++) {
            System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
            destPos += quadPositions.length;
        }

        attributes[BufferIndex.VERTICES.index].setPosition(vertices, 0, 0, quadCount * INDEXED_QUAD_VERTICES);
        Material material = new Material(program);
        mesh.setupIndexed(indices, attributes, material, null);
    }

    /**
     * Builds a quad mesh using a fan, the mesh can be rendered using glDrawArrays
     * 
     * @param mesh
     * @param program The program to use for the material in the mesh
     * @param quadPositions
     * @param attribute2Size
     */
    public static void buildQuadMeshFan(Mesh mesh, ShaderProgram program, float[] quadPositions, int attribute2Size) {
        int attributeBuffers = 1;
        if (attribute2Size > 0) {
            attributeBuffers = 2;
        }
        VertexBuffer[] attributes = new VertexBuffer[attributeBuffers];
        attributes[BufferIndex.VERTICES.index] = new VertexBuffer(STRIP_QUAD_VERTICES, XYZ_COMPONENTS,
                XYZUV_COMPONENTS, GLES20.GL_FLOAT);
        if (attributeBuffers > 1) {
            attributes[1] = new VertexBuffer(STRIP_QUAD_VERTICES, XYZ_COMPONENTS, attribute2Size, GLES20.GL_FLOAT);
        }
        attributes[BufferIndex.VERTICES.index].setPositionUV(quadPositions, 0, 0, STRIP_QUAD_VERTICES);
        Material material = new Material(program);
        mesh.setupVertices(attributes, material, null);
        mesh.setMode(GLES20.GL_TRIANGLE_FAN);
    }

    /**
     * Used by tiled objects to set the UV position to 1 or 0 so it can be multiplied by a fraction size to get
     * correct UV for a specific frame.
     * This method is chosen to move as much processing as possible to the GPU - the UV of each sprite could be
     * calculated at runtime but that would give a higher CPU impact when a large number of sprites are animated.
     * 
     * @param attributeData Array with attribute data where UV is stored.
     * @param offset Offset into attribute array
     * @param uIndex Index to U in attribute data
     * @param vIndex Index to V in attribute data
     * @param stride Added to get to next vertex.
     */
    public static void prepareTiledUV(float[] attributeData, int offset, int uIndex, int vIndex, int stride) {
        int index = offset;
        attributeData[index + uIndex] = 0;
        attributeData[index + vIndex] = 0;
        index += stride;
        attributeData[index + uIndex] = 1;
        attributeData[index + vIndex] = 0;
        index += stride;
        attributeData[index + uIndex] = 1;
        attributeData[index + vIndex] = 1;
        index += stride;
        attributeData[index + uIndex] = 0;
        attributeData[index + vIndex] = 1;
    }

}
