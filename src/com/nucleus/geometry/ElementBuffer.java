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
public class ElementBuffer {

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

    public enum Mode {
        /**
         * From GL_POINTS
         */
        POINTS(0),
        /**
         * From GL_LINE_STRIP
         */
        LINE_STRIP(3),
        /**
         * From GL_LINE_LOOP
         */
        LINE_LOOP(2),
        /**
         * From GL_TRIANGLE_STRIP
         */
        TRIANGLE_STRIP(5),
        /**
         * From GL_TRIANGLE_FAN
         */
        TRIANGLE_FAN(6),
        /**
         * From GL_TRIANGLES
         */
        TRIANGLES(4);

        public final int mode;

        private Mode(int mode) {
            this.mode = mode;
        }
    }

    /**
     * The vertex index buffer, contains indexes to vertices to be drawn.
     */
    ByteBuffer indices;

    Type type;

    /**
     * Drawmode, see openGL definition of glDrawElements()
     */
    Mode mode;
    int count;

    /**
     * Creates an element buffer with the specified number of indexes, if type is BYTE and count is 10 then a buffer
     * with 10 bytes will be allocated.
     * 
     * @param mode
     * @param count
     * @param type
     * @throws IllegalArgumentException if type or mode is null
     */
    public ElementBuffer(Mode mode, int count, Type type) {
        if (type == null || mode == null) {
            throw new IllegalArgumentException(NULL_TYPE_STR);
        }
        this.mode = mode;
        this.count = count;
        this.type = type;
        int size = 1;
        switch (type) {
        case BYTE:
            break;
        case SHORT:
            size = 2;
            break;
        }
        indices = ByteBuffer.allocateDirect(count * size).order(ByteOrder.nativeOrder());
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
     * Returns the elementbuffer draw mode, this is what primitives are drawn (triangles, triangle fan, lines etc)
     * 
     * @return
     */
    public Mode getMode() {
        return mode;
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
