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
public class VertexBuffer extends BufferObject {

    private final static String ILLEGAL_DATATYPE_STR = "Illegal datatype: ";

    /**
     * Number of components for X,Y,Z
     */
    public final static int XYZ_COMPONENTS = 3;
    /**
     * Number of components for UV
     */
    public final static int UV_COMPONENTS = 2;
    /**
     * XYZ and UV
     */
    public final static int XYZUV_COMPONENTS = 5;
    /**
     * Number of indexes for a quad drawn using drawElements (3 * 2)
     */
    public final static int QUAD_INDICES = 6;

    /**
     * Number of floats to next set of attribute data
     */
    private int attribFloatStride;
    /**
     * Number of bytes to next attrib variable.
     */
    private int attribByteStride;
    private FloatBuffer vertices;
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
    public VertexBuffer(int verticeCount, int sizePerVertex, int type) {
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
        vertices = ByteBuffer.allocateDirect(sizeInBytes)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        attribByteStride = sizePerVertex * dataSize;
        attribFloatStride = sizePerVertex;
        System.out
                .println("Allocated atrribute buffer with " + sizeInBytes + " bytes, sizePerVertices " + sizePerVertex
                        + " dataSize " + dataSize + ", capacity() "
                        + vertices.capacity());
    }

    /**
     * Sets position data from the source array.
     * After each vertice copied, the vertice stride is used to step in the destination buffer.
     * This method is not efficient for a large number of triangles.
     * 
     * @param triangleData The source data to copy, must hold data for the specified number of triangles.
     * Data is read in the format X,Y,Z
     * @param sourceOffset Offset in source where data is read.
     * @param vertexIndex Index of first vertex to set.
     * @param verticeCount Number of vertices to store.
     */
    @Deprecated
    public void setPosition(float[] verticeData, int sourceOffset, int vertexIndex, int verticeCount) {

        vertexIndex = vertexIndex * attribFloatStride;
        for (int i = 0; i < verticeCount; i++) {
            vertices.position(vertexIndex);
            vertices.put(verticeData, sourceOffset, XYZ_COMPONENTS);
            sourceOffset += XYZ_COMPONENTS;
            vertexIndex += attribFloatStride;
        }
    }

    /**
     * Sets position and UV data from the source array, the format will be XYZUV
     * After each vertice copied, the attribute stride is used to step in the destination buffer.
     * This method is not efficient for a large number of triangles.
     * 
     * @param triangleData The source data to copy, must hold data for the specified number of triangles.
     * Data is read in the format X,Y,Z,U,V
     * @param sourceOffset Offset in source where data is read.
     * @param destOffset Offset in destination vertex buffer, in floats, where data is stored - normally 0.
     * @param verticeCount Number of vertices to store.
     */
    @Deprecated
    public void setPositionUV(float[] verticeData, int sourceOffset, int destOffset, int verticeCount) {
        for (int i = 0; i < verticeCount; i++) {
            vertices.position(destOffset);
            vertices.put(verticeData, sourceOffset, XYZUV_COMPONENTS);
            sourceOffset += XYZUV_COMPONENTS;
            destOffset += attribFloatStride;
        }
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
            vertices.position(destOffset);
            vertices.put(data, sourceOffset, componentCount);
            sourceOffset += componentCount;
            destOffset += attribFloatStride;
        }
        dirty = true;
    }

    /**
     * Sets the UV data from the uv array, after each UV has been set the attribute stride in this buffer
     * will be used to step to the next vertice.
     * 
     * @param uv Packed UV coordinates to set.
     * @param sourceOffset
     * @param destOffset
     * @param verticeCount
     */
    public void setUV(float[] uv, int sourceOffset, int destOffset, int verticeCount) {
        for (int i = 0; i < verticeCount; i++) {
            vertices.position(destOffset);
            vertices.put(uv, sourceOffset, UV_COMPONENTS);
            sourceOffset += UV_COMPONENTS;
            destOffset += attribFloatStride;
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
        vertices.position(sourcePos);
        vertices.put(array, sourcePos, length);
    }


}
