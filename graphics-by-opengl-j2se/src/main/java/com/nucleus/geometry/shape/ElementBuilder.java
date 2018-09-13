package com.nucleus.geometry.shape;

import java.nio.ShortBuffer;

import com.nucleus.geometry.Mesh.Mode;

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
     * @param mesh buffer
     * @param mode
     * @param count Number of shapes, this could differ from number of vertices
     * @param startVertex First vertex index
     */
    public abstract void buildElements(ShortBuffer buffer, Mode mode, int count, int startVertex);

    /**
     * Builds an element buffer for quads, ie 4 separate vertices are used to create one quad (2 triangles)
     * There is no sharing of vertices between the quads, ie each quad is separated.
     * Only supports ElementBuffer of type SHORT
     * Triangles are built this order
     * 1-2-3
     * 1-4-3
     * 
     * @param buffer
     * @param count Number of quads to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the quad indices
     */
    public void buildQuadBuffer(ShortBuffer buffer, int count, int index) {
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
    }

    /**
     * Builds an element buffer for a line quad, ie 4 lines
     * There is no sharing of vertices between the lines, each line is separated.
     * Only supports ElementBuffer of type SHORT
     * Lines are built using this order
     * 0-1,1-2,2-3,3-0
     * 
     * @param buffer
     * @param count Number of quads to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the quad indices
     */
    public void buildQuadLineBuffer(ShortBuffer buffer, int count, int index) {
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
    }

    /**
     * Builds an element buffer for separate lines
     * There is no sharing of vertices between the lines, each line is separated.
     * Only supports ElementBuffer of type SHORT
     * 
     * @param buffer
     * @param count Number of lines to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the line indices
     */
    public void buildLinesBuffer(ShortBuffer buffer, int count, int index) {
        buffer.position(0);
        short[] quadIndices = new short[2];
        for (int i = 0; i < count; i++) {
            quadIndices[0] = (short) index;
            quadIndices[1] = (short) (index + 1);
            buffer.put(quadIndices, 0, 2);
            index += 2;
        }
    }

}
