package com.nucleus.geometry;

import java.nio.ByteBuffer;

import com.nucleus.geometry.ElementBuffer.Mode;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.shader.ShaderProgram;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public class MeshBuilder {

    /**
     * Number of vertices for an indexed quad
     */
    public final static int INDEXED_QUAD_VERTICES = 4;
    /**
     * Number of components for X,Y,Z
     */
    public final static int XYZ_COMPONENTS = 3;
    /**
     * Number of indexes for a quad drawn using drawElements (3 * 2)
     */
    public final static int QUAD_INDICES = 6;

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
     * Builds a buffer containing the 4 vertices for XYZ components, to be indexed when drawing, ie drawn using
     * drawElements().
     * The vertices will be centered in the middle of width / height, x will go from -(width / 2) to (width / 2)
     * y will go from -(height / 2) to (height / 2)
     * 
     * @param width
     * @param height
     * @param z The Z position
     * @param vertexStride, number of floats to add from one vertex to the next. Usually 3 to allow XYZ storage,
     * increase if padding (eg for UV) is needed.
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] buildQuadPositionsIndexed(float width, float height, float z, int vertexStride) {

        float[] quadPositions = new float[vertexStride * 4];

        float halfWidth = width / 2;
        float halfHeight = height / 2;
        com.nucleus.geometry.MeshBuilder.setPosition(-halfWidth, -halfHeight, z, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPosition(halfWidth, -halfHeight, z, quadPositions,
                vertexStride);
        com.nucleus.geometry.MeshBuilder.setPosition(halfWidth, halfHeight, z, quadPositions,
                vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPosition(-halfWidth, halfHeight, z, quadPositions,
                vertexStride * 3);
        return quadPositions;
    }

    /**
     * Builds a mesh with a specified number of indexed quads of GL_FLOAT type, the mesh will have an elementbuffer to
     * index the vertices.
     * Vertex buffer will have storage for XYZ + UV.
     * 
     * @program The program to use when rendering the mesh
     * @param spriteCount Number of sprites to build, this is NOT the vertex count.
     * @param quadPositions Array with x,y,z - this is set for each tile. Must contain data for 4 vertices.
     * @param attribute2Size Size per vertex for attribute buffer 2, this may be 0
     * 
     * @return The mesh that can be rendered.
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public static Mesh buildQuadMeshIndexed(ShaderProgram program, int quadCount, float[] quadPositions,
            int attribute2Size) {
        int attributeBuffers = 1;
        if (attribute2Size > 0) {
            attributeBuffers = 2;
        }
        VertexBuffer[] attributes = new VertexBuffer[attributeBuffers];
        attributes[0] = new VertexBuffer(quadCount * INDEXED_QUAD_VERTICES, XYZ_COMPONENTS, XYZ_COMPONENTS,
                GLES20.GL_FLOAT);
        if (attributeBuffers > 1) {
            attributes[1] = new VertexBuffer(quadCount * INDEXED_QUAD_VERTICES, 4, attribute2Size, GLES20.GL_FLOAT);
        }

        ElementBuffer indices = new ElementBuffer(Mode.TRIANGLES, QUAD_INDICES * quadCount, Type.SHORT);
        ElementBuilder.buildQuadBuffer(indices, indices.getCount() / QUAD_INDICES, 0);

        float[] vertices = new float[quadCount * INDEXED_QUAD_VERTICES * XYZ_COMPONENTS];
        int destPos = 0;
        for (int i = 0; i < quadCount; i++) {
            System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
            destPos += quadPositions.length;
        }

        attributes[0].setPosition(vertices, 0, 0, quadCount * INDEXED_QUAD_VERTICES);
        Material material = new Material(program);
        Mesh mesh = new Mesh(indices, attributes, material, null);
        return mesh;

    }

}
