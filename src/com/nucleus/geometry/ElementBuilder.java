package com.nucleus.geometry;

import java.nio.ShortBuffer;

import com.nucleus.geometry.ElementBuffer.Type;

/**
 * Utility to create different type of element (index) buffers.
 * 
 * @author Richard Sahlin
 *
 */
public class ElementBuilder {

    /**
     * Builds an element buffer for quads, ie 4 separate vertices are used to create one quad (2 triangles)
     * There is no sharing of vertices between the quads, ie each quad is separated.
     * Only supports ElementBuffer of type SHORT
     * 
     * @param quadStorage
     * @param count Number of quads to build
     * @param index First vertex to index
     * @return The ElementBuffer containing the quad indices
     */
    public static ElementBuffer buildQuadBuffer(ElementBuffer quadStorage, int count, int index) {
        if (quadStorage.type == Type.BYTE) {
            throw new IllegalArgumentException("Invalid type " + quadStorage.type);
        }

        ShortBuffer buffer = quadStorage.indices.asShortBuffer();
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

}
