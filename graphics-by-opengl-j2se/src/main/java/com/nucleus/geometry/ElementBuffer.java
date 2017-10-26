package com.nucleus.geometry;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Container for geometry element (index) data, this is used when the draw mode needs to index vertices.
 * 
 * @author Richard Sahlin
 *
 */
public class ElementBuffer extends BufferObject {

    private final static String NULL_TYPE_STR = "Type is null";

    /**
     * Data type for indices in the buffer
     * 
     * @author Richard Sahlin
     *
     */
    public enum Type {
        /**
         * Unsigned byte storage, same as GL_UNSIGNED_BYTE
         */
        BYTE(5121),
        /**
         * Unsigned short storage, same as GL_UNSIGNED_SHORT
         */
        SHORT(5123);

        public final int type;

        private Type(int type) {
            this.type = type;
        }

    }

    /**
     * The vertex index buffer, contains indexes to vertices to be drawn.
     */
    ByteBuffer indices;

    Type type;

    /**
     * Number of indices in this buffer
     */
    int count;

    /**
     * Number of elements to draw
     */
    int drawCount;

    /**
     * Offset to first element
     */
    int offset;

    /**
     * Creates an element buffer with the specified number of indexes, if type is BYTE and count is 10 then a buffer
     * with 10 bytes will be allocated.
     * 
     * @param count
     * @param type
     * @throws IllegalArgumentException if type is null
     */
    public ElementBuffer(int count, Type type) {
        if (type == null) {
            throw new IllegalArgumentException(NULL_TYPE_STR);
        }
        this.count = count;
        drawCount = count;
        this.type = type;
        int size = 1;
        switch (type) {
        case BYTE:
            break;
        case SHORT:
            size = 2;
            break;
        }
        sizeInBytes = count * size;
        indices = ByteBuffer.allocateDirect(sizeInBytes).order(ByteOrder.nativeOrder());
        System.out
                .println("Allocated element buffer with " + sizeInBytes);
    }

    /**
     * Returns the number of indices in this buffer, ie the number of drawable vertices.
     * 
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the number of elements to draw - this is set to the same as the element count when the buffer
     * is created
     * 
     * @return
     */
    public int getDrawCount() {
        return drawCount;
    }

    /**
     * Returns the offset to the first element (indice) to draw
     * 
     * @return
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the number of elements to draw and the offset to first element.
     * offset + drawCount must be less or equal to count.
     * 
     * @param drawCount Number of elements to draw (indices)
     * @param offset First element to draw
     */
    public void setDrawCount(int drawCount, int offset) {
        if ((drawCount + offset) <= count) {
            this.drawCount = drawCount;
            this.offset = offset;
        }
    }


    /**
     * Returns the type of data stored in this buffer
     * 
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the buffer containing the element index data, this can be used to draw with drawElements()
     * 
     * @return
     */
    public Buffer getBuffer() {
        return indices;
    }

}
