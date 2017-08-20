package com.nucleus.geometry;

import java.nio.ByteBuffer;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderProgram;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public class MeshBuilder<T> {

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
     * Builds a mesh with a specified number of indexed quads of GL_FLOAT type, the mesh must have an elementbuffer to
     * index the vertices. The index buffer will be built with indexes, this mesh shall be drawn using indexed mode.
     * Vertex buffer shall have storage for XYZ and texture UV if used.
     * 
     * @param mesh The mesh to build the buffers in, this is the mesh that can be rendered.
     * @param program The program to use when rendering the mesh, it is stored in the material
     * @param quadCount Number of quads to put in element (index) buffer, 1 means 6 indexes for 1 quad
     * This is the max number of quads that can be drawn.
     * @param quadPositions Array with x,y,z - this is set for each tile. Must contain data for 4 vertices.
     */
    public static void buildQuadMeshIndexed(Mesh mesh, ShaderProgram program, int quadCount,
            float[] quadPositions) {
        // // Create the indexes
        ElementBuilder.buildQuadBuffer(mesh.indices, quadCount, 0);
        buildQuads(mesh, program, quadCount, 0, quadPositions);
    }

    /**
     * Builds the position data for one or more quads at the specified index in the mesh.
     * The indices must already be created
     * Vertex buffer shall have storage for XYZ and texture UV if used.
     * 
     * @param mesh The mesh to build the position data in
     * @param program The program to use when rendering the mesh, it is stored in the material
     * @param quadCount Number of quads build positions for.
     * @param index Index to the quad to build, 0 means the first, 1 the second etc.
     * @param quadPositions Array with x,y,z and optional uv - this is set for each tile. Must contain data for 4
     * vertices.
     */
    public static void buildQuads(Mesh mesh, ShaderProgram program, int quadCount, int index,
            float[] quadPositions) {
        VertexBuffer buffer = mesh.attributes[BufferIndex.VERTICES.index];
        // TODO do not fetch vertices, call buffer.setPosition()
        float[] vertices = new float[buffer.getFloatStride() * quadCount * 4];
        int destPos = 0;
        for (int i = 0; i < quadCount; i++) {
            System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
            destPos += quadPositions.length;
        }
        int components = quadPositions.length / RectangleShapeBuilder.QUAD_VERTICES;
        VertexBuffer vb = mesh.attributes[BufferIndex.VERTICES.index];
        vb.setComponents(vertices,
                components, 0, index * components * RectangleShapeBuilder.QUAD_VERTICES,
                quadCount * RectangleShapeBuilder.QUAD_VERTICES);
        vb.setDirty(true);
    }

}
