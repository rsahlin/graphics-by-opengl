package com.nucleus.geometry;

/**
 * Base class for a buffer that can be handled as a buffer object, via binding a buffer then calling the apropriate
 * method to use the buffer.
 * For instance when calling glVertexAttribPointer
 * 
 * @author Richard Sahlin
 *
 */
public class BufferObject {

    /**
     * The buffer object name
     */
    private int name;

    /**
     * The size of the buffer in bytes.
     */
    protected int sizeInBytes;

    /**
     * Sets the buffer object to use, this must be allocated by GL, or 0 to disable buffer objects.
     * 
     * @param name Buffer name or 0 to disable
     */
    public void setBufferName(int name) {
        this.name = name;
    }

    /**
     * Returns the buffer object name, if not 0 then use the buffer object when sending data to GL.
     * 
     * @return
     */
    public int getBufferName() {
        return name;
    }

    /**
     * Returns the size in bytes of this buffer.
     * 
     * @return
     */
    public int getSizeInBytes() {
        return sizeInBytes;
    }

}
