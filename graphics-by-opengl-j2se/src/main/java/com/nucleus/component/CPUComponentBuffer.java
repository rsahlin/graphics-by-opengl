package com.nucleus.component;

import com.nucleus.geometry.AttributeBuffer;

/**
 * Component buffer with a float array store - use this for CPU usecases.
 *
 */
public class CPUComponentBuffer extends ComponentBuffer {

    float[] data;

    public CPUComponentBuffer(int entityCount, int sizePerEntity) {
        super(entityCount, sizePerEntity);
        data = new float[entityCount * sizePerEntity];
    }

    @Override
    public void get(int entity, float[] destination) {
        System.arraycopy(data, entity * sizePerEntity, destination, 0, sizePerEntity);
    }

    @Override
    public void get(int entity, AttributeBuffer destination) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void put(int entity, int offset, float[] source, int srcOffset, int count) {
        System.arraycopy(source, srcOffset, data, entity * sizePerEntity + offset, count);
    }

}
