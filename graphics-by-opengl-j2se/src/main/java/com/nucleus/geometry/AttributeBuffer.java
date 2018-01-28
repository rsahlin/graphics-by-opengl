package com.nucleus.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.nucleus.SimpleLogger;
import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * Create and hold data for OpenGL vertex arrays, this can for instance be the vertice position, texture coordinates,
 * normal and material data or any other attribute data.
 * The data is interleaved, meaning that data for one vertex is stored together - as opposed to having separated
 * buffers.
 * 
 * @author Richard Sahlin
 *
 */
public class AttributeBuffer extends BufferObject {

    private final static String ILLEGAL_DATATYPE_STR = "Illegal datatype: ";

    /**
     * Number of floats to next set of attribute data
     */
    private int attribFloatStride;
    /**
     * Number of bytes to next attrib variable.
     */
    private int attribByteStride;
    private FloatBuffer attributes;
    private int verticeCount;

    /**
     * Datatype
     */
    private int type;

    /**
     * Creates the buffer storage for specified number of vertices, this can be used to draw different types, for
     * instance with an element (vertex index) buffer or with drawArrays.
     * 
     * @param verticeCount Number of vertices to allocate storage for
     * @param sizePerVertex Size in floats to allocate for each vertex, eg 3 if xyz is specified
     * @param type The datatype GLES20.GL_FLOAT
     * @throws IllegalArgumentException If type is not GLES20.GL_FLOAT
     */
    public AttributeBuffer(int verticeCount, int sizePerVertex, int type) {
        init(verticeCount, sizePerVertex, type);
    }

    /**
     * Creates the buffer to hold vertice and attribute data.
     * 
     * @param verticeCount Number of vertices to allocate storage for
     * @param sizePerVertex Size in floats to allocate for each vertex, normal usecase for x,y,z + texture uv is 5
     * @param type The datatype GLES20.GL_FLOAT
     * @throws IllegalArgumentException If type is not GLES20.GL_FLOAT
     */
    private void init(int verticeCount, int sizePerVertex, int type) {
        if (type != GLES20.GL_FLOAT) {
            throw new IllegalArgumentException(ILLEGAL_DATATYPE_STR + type);
        }
        int dataSize = 4;
        this.type = type;
        this.verticeCount = verticeCount;
        sizeInBytes = verticeCount * sizePerVertex * dataSize;
        attributes = ByteBuffer.allocateDirect(sizeInBytes)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        attribByteStride = sizePerVertex * dataSize;
        attribFloatStride = sizePerVertex;
        SimpleLogger.d(getClass(),
                "Allocated atrribute buffer with " + sizeInBytes + " bytes, sizePerVertices " + sizePerVertex
                        + " dataSize " + dataSize + ", capacity() "
                        + attributes.capacity());
    }

    /**
     * Sets a number of components into this buffer from the data array, uses the stride in this buffer
     * to step from one vertice to the next
     * 
     * @param data
     * @param componentCount Number of components (values) to set, 1 to set one float
     * @param sourceOffset
     * @param destOffset
     * @param verticeCount Number of vertices to set the components to
     */
    public void setComponents(float[] data, int componentCount, int sourceOffset, int destOffset, int verticeCount) {
        for (int i = 0; i < verticeCount; i++) {
            attributes.position(destOffset);
            attributes.put(data, sourceOffset, componentCount);
            sourceOffset += componentCount;
            destOffset += attribFloatStride;
        }
        dirty = true;
    }

    /**
     * Returns the underlying Buffer holding vertex buffer array data.
     * NOTE!
     * Take care when writing to the buffer as it may clash with copy to gl.
     * 
     * @return
     */
    public FloatBuffer getBuffer() {
        return attributes;
    }

    /**
     * returns the capacity of the underlying buffer
     * 
     * @return
     */
    public int getCapacity() {
        return attributes.capacity();
    }

    /**
     * Returns the number of vertices stored in this buffer, ie the max number of vertices that can be rendered.
     * 
     * @return Number of vertices in this buffer
     */
    public int getVerticeCount() {
        return verticeCount;
    }

    /**
     * Returns the float stride value
     * 
     * @return
     */
    public int getFloatStride() {
        return attribFloatStride;
    }

    /**
     * Returns the datatype for this buffer, this is the type of data contained within, ie is the data byte, short float
     * Use for vertex attrib pointer.
     * 
     * @return
     */
    public int getDataType() {
        return type;
    }

    /**
     * The byte offset between consecutive variables
     * 
     * @return Number of bytes between consecutive variables
     */
    public int getByteStride() {
        return attribByteStride;
    }

    /**
     * Sets the byte stride value used when attrib pointer is set.
     * Call this method if you want to override the default value which is set to ame as vertex byte size when buffer is
     * created.
     * 
     * @param byteStride
     */
    public void setByteStride(int byteStride) {
        attribByteStride = byteStride;
    }

    /**
     * Copies float values from the source array into the buffer.
     * Use this method when many values shall be written.
     * 
     * @param array
     * @param sourcePos
     * @param destPos Position into floatbuffer where values are put
     * @param length
     */
    public void setArray(float[] array, int sourcePos, int destPos, int length) {
        attributes.position(destPos);
        attributes.put(array, sourcePos, length);
    }

    /**
     * Same as calling {@link #calculateBounds2D(int)} with the vertice count and first
     * rewinding the buffer.
     * 
     * @param vertices
     * @return
     */
    public float[] calculateBounds2D() {
        attributes.rewind();
        return calculateBounds2D(verticeCount);
    }

    /**
     * Calculates the axis aligned 2D bounds for vertices in this buffer, starting at the current position.
     * 
     * @param count Number of vertices to include in calculation
     * @return Array with the smallest and largest corner (x1y1x2y2)
     */
    public float[] calculateBounds2D(int count) {
        float[] result = null;
        int stride = (attribByteStride / 4);
        float[] values = new float[stride];
        FloatBuffer buffer = attributes;
        for (int i = 0; i < count; i++) {
            buffer.get(values);
            if (result == null) {
                result = new float[4];
                System.arraycopy(values, 0, result, 0, 2);
                System.arraycopy(values, 0, result, 2, 2);
            } else {
                result[0] = Math.min(values[0], result[0]);
                result[1] = Math.min(values[1], result[1]);
                result[2] = Math.max(values[2], result[2]);
                result[3] = Math.max(values[3], result[3]);
            }
        }
        return result;
    }

}
