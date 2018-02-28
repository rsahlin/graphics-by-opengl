package com.nucleus.component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeBuffer;

/**
 * ComponentBuffer with a native underlying buffer - this is for native usecases such as for Compute shaders or OpenCL.
 *
 */
public class NativeComponentBuffer extends ComponentBuffer {

    FloatBuffer data;
    final float[] tempData;

    public NativeComponentBuffer(int entityCount, int sizePerEntity) {
        super(entityCount, sizePerEntity);
        data = ByteBuffer.allocateDirect(entityCount * sizePerEntity * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        tempData = new float[sizePerEntity];
    }

    @Override
    public void get(int entity, float[] destination) {
        data.position(entity * sizePerEntity);
        data.get(destination);
    }

    @Override
    public void get(int entity, AttributeBuffer destination) {
        get(entity, tempData);
        destination.put(tempData);
    }

    @Override
    public void put(int entity, int offset, float[] source, int srcOffset, int count) {
        data.position(entity * sizePerEntity + offset);
        data.put(source, srcOffset, count);
    }

}
