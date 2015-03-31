package com.nucleus.geometry;

import java.nio.FloatBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.opengl.GLES20Wrapper.GLES20;

public class FVertexBufferTest {

    final static int TRIANGLE_COUNT = 3;
    final static int COMPONENT_COUNT = 3;
    final static int SIZE_PER_VERTEX = 10;
    final static int DATA_SIZE32 = 4;

    @Test
    public void testCreateVertexBuffer() {
        VertexBuffer vb = new VertexBuffer(TRIANGLE_COUNT, SIZE_PER_VERTEX, GLES20.GL_FLOAT,
                DATA_SIZE32);
        Assert.assertEquals(TRIANGLE_COUNT * COMPONENT_COUNT, vb.getVerticeCount());
        Assert.assertEquals(COMPONENT_COUNT, vb.getComponentCount());
        Assert.assertEquals(GLES20.GL_FLOAT, vb.getDataType());
        Assert.assertEquals(SIZE_PER_VERTEX * DATA_SIZE32, vb.getByteStride());
        Assert.assertEquals(SIZE_PER_VERTEX * TRIANGLE_COUNT * COMPONENT_COUNT, vb.getBuffer().capacity());
    }

    @Test
    public void testPutPositionUV() {

        // Allocate data for xyz + uv
        float[] data = new float[TRIANGLE_COUNT * 3 * 5];
        for (int i = 0; i < data.length; i++) {
            data[i] = i;
        }
        VertexBuffer vb = new VertexBuffer(TRIANGLE_COUNT, SIZE_PER_VERTEX, GLES20.GL_FLOAT,
                DATA_SIZE32);
        vb.setPosition(data, 0, 0, TRIANGLE_COUNT * 3);
        // First 5 values should be followed by empty data (SIZE_PER_VERTEX - 5)
        // For now assume FloatBuffer since this is the only type currently supported.0
        float[] getBuffer = new float[5];
        int position = 0;
        for (int i = 0; i < TRIANGLE_COUNT * COMPONENT_COUNT; i++) {
            FloatBuffer buffer = (FloatBuffer) vb.getBuffer();
            buffer.position(position);
            buffer.get(getBuffer);
            position += SIZE_PER_VERTEX;
            Assert.assertArrayEquals(new float[] {
                    i * 5, i * 5 + 1, i * 5 + 2, i * 5 + 3, i * 5 + 4 }, getBuffer, 0);
        }

    }
}
