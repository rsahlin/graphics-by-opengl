package com.nucleus.component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
    public void put(int entity, int offset, float[] source, int srcOffset, int count) {
        data.position(entity * sizePerEntity + offset);
        data.put(source, srcOffset, count);
    }

}
