package com.nucleus.component;

import com.nucleus.geometry.AttributeBuffer;

public class CPUComponentBuffer extends ComponentBuffer {

    private float[] data;

    public CPUComponentBuffer(int entityCount, int sizePerEntity) {
        super(entityCount, sizePerEntity);
        data = new float[entityCount * sizePerEntity];
    }

    @Override
    public void get(int entity, float[] destination) {
        int index = entity * sizePerEntity;
        System.arraycopy(data, index, destination, 0, sizePerEntity);
    }

    @Override
    public void put(int entity, int offset, float[] source, int srcOffset, int count) {
        int index = entity * sizePerEntity;
        System.arraycopy(source, srcOffset, this.data, index + offset, count);
    }

    @Override
    public void get(int entity, AttributeBuffer destination) {
        int index = entity * sizePerEntity;
        destination.getBuffer().put(data, index, sizePerEntity);
        destination.setDirty(true);
    }

}
