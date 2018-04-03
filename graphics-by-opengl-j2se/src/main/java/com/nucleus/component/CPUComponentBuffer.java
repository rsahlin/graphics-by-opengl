package com.nucleus.component;

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

    public float[] getData() {
        return data;
    }

    @Override
    public void put(int entity, int offset, float[] source, int srcOffset, int count) {
        System.arraycopy(source, srcOffset, data, entity * sizePerEntity + offset, count);
    }

}
