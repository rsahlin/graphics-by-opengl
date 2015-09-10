package com.nucleus.camera;

import org.junit.Assert;
import org.junit.Test;

public class FViewFrustumTest {

    @Test
    public void testCreate() {
        // Tests that the frustum is created and holds the proper data.
        ViewFrustum vf = new ViewFrustum();
        Assert.assertEquals(ViewFrustum.MATRIX_LENGTH, vf.getProjectionMatrix().length);
        Assert.assertEquals(ViewFrustum.PROJECTION_LENGTH, vf.getProjection().length);
        Assert.assertEquals(ViewFrustum.VIEWPORT_LENGTH, vf.getViewPort().length);
    }

    @Test
    public void testSetViewPort() {
        ViewFrustum vf = new ViewFrustum();
        int[] values = new int[] { 1, 2, 111, 222 };
        vf.setViewPort(values[0], values[1], values[2], values[3]);
        Assert.assertArrayEquals(values, vf.getViewPort());
    }

    @Test
    public void testSetProjection() {
        ViewFrustum vf = new ViewFrustum();
        float[] values = new float[] { 1, 2, 3, 4, 5, 6 };
        vf.setOrthoProjection(values[0], values[1], values[2], values[3], values[4], values[5]);
        Assert.assertEquals(ViewFrustum.PROJECTION_ORTHOGONAL, vf.getProjectionType());
        Assert.assertArrayEquals((float[]) values, (float[]) vf.getProjection(), 0f);
    }
}
