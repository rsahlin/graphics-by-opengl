package com.nucleus.geometry;

import java.nio.ShortBuffer;

import com.nucleus.geometry.ElementBuffer.Type;

/**
 * Utility to create different type of element (index) buffers.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class ElementBuilder extends ShapeBuilder {

    /**
     * Builds the element buffer if present in the mesh
     * 
     * @param mesh The mesh to build the element buffer in
     * @param count Number of shapes, this could differ from number of vertices
     * @param startVertex First vertex index
     */
    public abstract void buildElements(Mesh mesh, int count, int startVertex);

    /**
     * Builds an element buffer for quads, ie 4 separate vertices are used to create one quad (2 triangles)
     * There is no sharing of vertices between the quads, ie each quad is separated.
     * Only supports ElementBuffer of type SHORT
     * Triangles are built this order
     * 1-2-3
     * 1-4-3
     * 
     * @param quadStorage
     * @param count Number of quads to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the quad indices
     */
    public ElementBuffer buildQuadBuffer(ElementBuffer quadStorage, int count, int index) {
        if (quadStorage.type == Type.BYTE) {
            throw new IllegalArgumentException("Invalid type " + quadStorage.type);
        }

        ShortBuffer buffer = quadStorage.indices.asShortBuffer();
        buffer.position(0);
        short[] quadIndices = new short[6];
        for (int i = 0; i < count; i++) {
            quadIndices[0] = (short) index;
            quadIndices[1] = (short) (index + 1);
            quadIndices[2] = (short) (index + 2);
            quadIndices[3] = (short) (index);
            quadIndices[4] = (short) (index + 2);
            quadIndices[5] = (short) (index + 3);
            buffer.put(quadIndices, 0, 6);
            index += 4;
        }

        return quadStorage;
    }

    /**
     * Builds an element buffer for a line quad, ie 4 lines
     * There is no sharing of vertices between the lines, each line is separated.
     * Only supports ElementBuffer of type SHORT
     * Lines are built using this order
     * 0-1,1-2,2-3,3-0
     * 
     * @param quadStorage
     * @param count Number of quads to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the quad indices
     */
    public ElementBuffer buildQuadLineBuffer(ElementBuffer quadStorage, int count, int index) {
        if (quadStorage.type == Type.BYTE) {
            throw new IllegalArgumentException("Invalid type " + quadStorage.type);
        }

        ShortBuffer buffer = quadStorage.indices.asShortBuffer();
        buffer.position(0);
        short[] quadIndices = new short[8];
        for (int i = 0; i < count; i++) {
            quadIndices[0] = (short) index;
            quadIndices[1] = (short) (index + 1);
            quadIndices[2] = (short) (index + 1);
            quadIndices[3] = (short) (index + 2);
            quadIndices[4] = (short) (index + 2);
            quadIndices[5] = (short) (index + 3);
            quadIndices[6] = (short) (index + 3);
            quadIndices[7] = (short) index;
            buffer.put(quadIndices, 0, 8);
            index += 4;
        }

        return quadStorage;
    }

    /**
     * Builds an element buffer for separate lines
     * There is no sharing of vertices between the lines, each line is separated.
     * Only supports ElementBuffer of type SHORT
     * 
     * @param lineStorage
     * @param count Number of lines to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the line indices
     */
    public ElementBuffer buildLinesBuffer(ElementBuffer lineStorage, int count, int index) {
        if (lineStorage.type != Type.SHORT) {
            throw new IllegalArgumentException("Invalid type " + lineStorage.type);
        }

        ShortBuffer buffer = lineStorage.indices.asShortBuffer();
        buffer.position(0);
        short[] quadIndices = new short[2];
        for (int i = 0; i < count; i++) {
            quadIndices[0] = (short) index;
            quadIndices[1] = (short) (index + 1);
            buffer.put(quadIndices, 0, 2);
            index += 2;
        }

        return lineStorage;
    }

}
