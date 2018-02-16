package com.nucleus.geometry;

import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderProgram;

/**
 * Utility class to help build different type of Meshes.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class MeshBuilder<T> {

    /**
     * Create a new empty instance of a mesh.
     * 
     * @return
     */
    protected abstract T createMesh();

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
        AttributeBuffer buffer = mesh.attributes[BufferIndex.VERTICES.index];
        // TODO do not fetch vertices, call buffer.setPosition()
        float[] vertices = new float[buffer.getFloatStride() * quadCount * 4];
        int destPos = 0;
        for (int i = 0; i < quadCount; i++) {
            System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
            destPos += quadPositions.length;
        }
        int components = quadPositions.length / RectangleShapeBuilder.QUAD_VERTICES;
        AttributeBuffer vb = mesh.attributes[BufferIndex.VERTICES.index];
        vb.setComponents(vertices,
                components, 0, index * components * RectangleShapeBuilder.QUAD_VERTICES,
                quadCount * RectangleShapeBuilder.QUAD_VERTICES);
        vb.setDirty(true);
    }

}
