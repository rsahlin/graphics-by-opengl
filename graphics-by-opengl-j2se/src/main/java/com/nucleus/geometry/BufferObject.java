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
     * The buffer object name (id)
     */
    private int name;
    /**
     * The size of the buffer in bytes.
     */
    protected final int sizeInBytes;
    /**
     * Set to true when data in the buffer changes, means it needs to be uploaded to gl
     */
    protected boolean dirty;

    protected BufferObject(int sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

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

    /**
     * Returns true if the data in this buffer has changed.
     * 
     * @return True if the data in this buffer has changed
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the state of the dirty flag, use this to signal that the data in this buffer has been updated.
     * 
     * @param dirty
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

}
