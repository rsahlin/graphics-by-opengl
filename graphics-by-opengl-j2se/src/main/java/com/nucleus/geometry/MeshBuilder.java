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
     * The vertices will be centered using anchorX and anchorY, a value of 0 will be left/top aligned.
     * A value of 1/width will be centered horizontally.
     * 
     * @param width
     * @param height
     * @param z The Z position
     * @param anchorX X axis anchor offet, 0 will be left centered (assuming x axis is increasing to the right)
     * @param anchorY Y axis anchor offet, 0 will be top centered, (assuming y axis is increasing downwards)
     * @param vertexStride, number of floats to add from one vertex to the next. Usually 3 to allow XYZ storage,
     * increase if padding (eg for UV) is needed.
     * @return array containing 4 vertices for a quad with the specified size, the size of the array will be
     * vertexStride * 4
     */
    public static float[] buildQuadPositionsIndexed(float width, float height, float z, float anchorX, float anchorY,
            int vertexStride) {

        float[] quadPositions = new float[vertexStride * 4];

        com.nucleus.geometry.MeshBuilder.setPosition(-anchorX, -anchorY, z, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPosition(width - anchorX, -anchorY, z, quadPositions,
                vertexStride);
        com.nucleus.geometry.MeshBuilder.setPosition(width - anchorX, height - anchorY, z, quadPositions,
                vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPosition(-anchorX, height - anchorY, z, quadPositions,
                vertexStride * 3);
        return quadPositions;
    }

    /**
     * Builds a mesh with a specified number of indexed quads of GL_FLOAT type, the mesh will have an elementbuffer to
     * index the vertices.
     * Vertex buffer will have storage for XYZ + UV.
     * 
     * @param mesh The mesh to build the buffers in, this is the mesh that can be rendered.
     * @param The program to use when rendering the mesh
     * @param spriteCount Number of sprites to build, this is NOT the vertex count.
     * @param quadPositions Array with x,y,z - this is set for each tile. Must contain data for 4 vertices.
     * @param attribute2Size Size per vertex for attribute buffer 2, this may be 0
     * 
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public static void buildQuadMeshIndexed(Mesh mesh, ShaderProgram program, int quadCount, float[] quadPositions,
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
        mesh.setupIndexed(indices, attributes, material, null);
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
