package com.nucleus.component;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.BufferObject;

/**
 * Storage for component data, this data is read and written to by clients.
 * Buffer can be optimized for usage by CPU or native (GPU)
 * The buffer holds storage for a number of entities - what they are and how they are processed is not known to the
 * buffer.
 * 
 *
 */
public abstract class ComponentBuffer extends BufferObject {

    /**
     * Number of entities that this buffer has storage for.
     */
    protected int entityCount;
    /**
     * Storage size per entity.
     */
    protected int sizePerEntity;

    public ComponentBuffer(int entityCount, int sizePerEntity) {
        this.entityCount = entityCount;
        this.sizePerEntity = sizePerEntity;
        sizeInBytes = (entityCount * sizePerEntity) << 2;
    }

    /**
     * Reads (copies) the data for the specified entity.
     * The destination array must have enough space to store {@link #getSizePerEntity()} values
     * 
     * @param entity The entity to fetch, 0 to entityCount - 1
     * @param destination Data is put here
     */
    public abstract void get(int entity, float[] destination);

    /**
     * Reads (copies) the data for the specified entity.
     * The destination buffer must have enough space, at the current position, to store {@link #getSizePerEntity()}
     * values
     * 
     * @param entity The entity to fetch, 0 to entityCount - 1
     * @param destination The destination buffer
     */
    public abstract void get(int entity, AttributeBuffer destination);

    /**
     * Stores float values for the specified entity, with offset.
     * 
     * @param entity
     * @param offset
     * @param source
     * @param srcOffset offset into data where first value is read
     * @param count Number of values to copy
     */
    public abstract void put(int entity, int offset, float[] source, int srcOffset, int count);

    /**
     * Returns the size (floats) of an entity
     * 
     * @return
     */
    public int getSizePerEntity() {
        return sizePerEntity;
    }

}
