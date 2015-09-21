package com.nucleus.geometry;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * Create and hold data for OpenGL vertex arrays, this can for instance be the vertice position, texture coordinates,
 * normal and material data.
 * The data is interleaved, meaning that data for one vertex is stored together - as opposed to having separated
 * buffers.
 * 
 * @author Richard Sahlin
 *
 */
public class VertexBuffer {

    private final static String ILLEGAL_DATATYPE_STR = "Illegal datatype: ";

    /**
     * Number of floats to next set of attribute data
     */
    private int attribFloatSize;
    /**
     * Number of bytes to next attrib variable.
     */
    private int attribByteStride;
    private FloatBuffer vertices;
    private int verticeCount;
    /**
     * Number of components
     */
    private int components;
    /**
     * Datatype
     */
    private int type;

    /**
     * Creates the buffer storage for specified number of vertices, this can be used to draw different types, for
     * instance with an element (vertex index) buffer or with drawArrays.
     * 
     * @param verticeCount Number of vertices to allocate storage for
     * @param components Number of components for each vertex, 3 for x,y,z
     * @param sizePerVertex Size in floats to allocate for each vertex, eg 3 if xyz is specified
     * @param type The datatype GLES20.GL_FLOAT
     * @throws IllegalArgumentException If type is not GLES20.GL_FLOAT
     */
    public VertexBuffer(int verticeCount, int components, int sizePerVertex, int type) {
        init(verticeCount, components, sizePerVertex, type);
    }

    /**
     * Creates the buffer to hold vertice and attribute data.
     * 
     * @param verticeCount Number of vertices to allocate storage for
     * @param components Number of components for each vertex, 3 for x,y,z
     * @param sizePerVertex Size in floats to allocate for each vertex, normal usecase for x,y,z + texture uv is 5
     * @param type The datatype GLES20.GL_FLOAT
     * @throws IllegalArgumentException If type is not GLES20.GL_FLOAT
     */
    private void init(int verticeCount, int components, int sizePerVertex, int type) {
        if (type != GLES20.GL_FLOAT) {
            throw new IllegalArgumentException(ILLEGAL_DATATYPE_STR + type);
        }
        int dataSize = 4;
        this.components = components;
        this.type = type;
        this.verticeCount = verticeCount;
        vertices = ByteBuffer.allocateDirect(verticeCount * sizePerVertex * dataSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        attribByteStride = sizePerVertex * dataSize;
        attribFloatSize = sizePerVertex;
    }

    /**
     * Sets position data from the source array.
     * After each triangle copied, the vertice stride is used to step in the destination buffer.
     * This method is not efficient for a large number of triangles.
     * 
     * @param triangleData The source data to copy, must hold data for the specified number of triangles.
     * Data is read in the format X,Y,Z,U,V
     * @param sourceOffset Offset in source where data is read.
     * @param destOffset Offset in destination vertex buffer, in floats, where data is stored - normally 0.
     * @param triangleCount Number of triangles to store.
     */
    public void setPosition(float[] verticeData, int sourceOffset, int destOffset, int verticeCount) {

        for (int i = 0; i < verticeCount; i++) {
            vertices.position(destOffset);
            vertices.put(verticeData, sourceOffset, 3);
            sourceOffset += 3;
            destOffset += attribFloatSize;
        }
    }

    /**
     * Returns the underlying Buffer holding vertex buffer array data.
     * 
     * @return
     */
    public Buffer getBuffer() {
        return vertices;
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
     * Returns the number of components in this buffer, normally 3 (X,Y,Z)
     * Used when setting vertex attrib pointer.
     * 
     * @return
     */
    public int getComponentCount() {
        return components;
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
     * The byte offset between consecutive variables.
     * 
     * @return Number of bytes between consecutive variables
     */
    public int getByteStride() {
        return attribByteStride;
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
        vertices.position(sourcePos);
        vertices.put(array, sourcePos, length);
    }

}
