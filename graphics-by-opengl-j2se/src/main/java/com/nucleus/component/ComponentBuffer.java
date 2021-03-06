package com.nucleus.component;

import com.nucleus.geometry.BufferObject;

/**
 * Storage for component data, this data is read and written to by clients.
 * Buffer can be optimized for usage by CPU or native (GPU)
 * The buffer holds storage for a number of entities - what they are and how they are processed is not known to the
 * buffer.
 * Currently sets datatype size to 4, if any other size is used this must be updated.
 * 
 *
 */
public abstract class ComponentBuffer extends BufferObject {

    public final static int DATATYPE_SIZE = 4;

    /**
     * Number of entities that this buffer has storage for.
     */
    protected final int entityCount;
    /**
     * Storage size per entity.
     */
    protected final int sizePerEntity;

    public ComponentBuffer(int entityCount, int sizePerEntity) {
        super((entityCount * sizePerEntity) * DATATYPE_SIZE);
        this.entityCount = entityCount;
        this.sizePerEntity = sizePerEntity;
    }

    /**
     * Stores float values for the specified entity, with offset, use this sparingly when data for the entity shall be
     * initialized.
     * Avoid calling this often since it is not optimized.
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

    /**
     * Returns the number of entities
     * 
     * @return
     */
    public int getEntityCount() {
        return entityCount;
    }

}
