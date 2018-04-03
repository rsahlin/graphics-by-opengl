package com.nucleus.geometry;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.nucleus.SimpleLogger;

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
        BYTE(5121, 1),
        /**
         * Unsigned short storage, same as GL_UNSIGNED_SHORT
         */
        SHORT(5123, 2);

        public final int type;
        public final int sizeInBytes;

        private Type(int type, int sizeInBytes) {
            this.type = type;
            this.sizeInBytes = sizeInBytes;
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
     * Offset to first element
     */
    int offset;

    /**
     * Creates an element buffer with the specified number of indexes, if type is BYTE and count is 10 then a buffer
     * with 10 bytes will be allocated.
     * 
     * @param count
     * @param type
     * @throws NullPointerException if type is null
     */
    public ElementBuffer(int count, Type type) {
        super(count * type.sizeInBytes);
        this.count = count;
        this.type = type;
        indices = ByteBuffer.allocateDirect(sizeInBytes).order(ByteOrder.nativeOrder());
        SimpleLogger.d(getClass(), "Allocated element buffer with " + sizeInBytes);
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
