package com.nucleus.component;

import java.nio.FloatBuffer;

import com.nucleus.common.BufferUtils;

/**
 * ComponentBuffer with a native underlying buffer - this is for native usecases such as for Compute shaders or OpenCL.
 *
 */
public class NativeComponentBuffer extends ComponentBuffer {

    FloatBuffer data;
    final float[] tempData;

    public NativeComponentBuffer(int entityCount, int sizePerEntity) {
        super(entityCount, sizePerEntity);
        data = BufferUtils.createFloatBuffer(entityCount * sizePerEntity);
        tempData = new float[sizePerEntity];
    }

    @Override
    public void put(int entity, int offset, float[] source, int srcOffset, int count) {
        data.position(entity * sizePerEntity + offset);
        data.put(source, srcOffset, count);
    }

}
