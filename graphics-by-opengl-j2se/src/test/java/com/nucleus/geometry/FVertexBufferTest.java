package com.nucleus.geometry;

import java.nio.FloatBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.opengl.GLESWrapper.GLES20;

public class FVertexBufferTest {

    final static int TRIANGLE_COUNT = 3;
    final static int COMPONENT_COUNT = 3;
    final static int SIZE_PER_VERTEX = 10;
    final static int DATA_SIZE32 = 4;

    @Test
    public void testCreateVertexBuffer() {
        AttributeBuffer vb = new AttributeBuffer(TRIANGLE_COUNT * 3, SIZE_PER_VERTEX, GLES20.GL_FLOAT);
        Assert.assertEquals(TRIANGLE_COUNT * 3, vb.getVerticeCount());
        Assert.assertEquals(GLES20.GL_FLOAT, vb.getDataType());
        Assert.assertEquals(SIZE_PER_VERTEX * DATA_SIZE32, vb.getByteStride());
        Assert.assertEquals(SIZE_PER_VERTEX * TRIANGLE_COUNT * COMPONENT_COUNT, vb.getBuffer().capacity());
    }

}
