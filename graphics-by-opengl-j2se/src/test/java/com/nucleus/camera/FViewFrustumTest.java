package com.nucleus.camera;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.camera.ViewFrustum.Projection;

public class FViewFrustumTest {

    @Test
    public void testSetProjection() {
        ViewFrustum vf = new ViewFrustum();
        float[] values = new float[] { 1, 2, 3, 4, 5, 6 };
        vf.setOrthoProjection(values[0], values[1], values[2], values[3], values[4], values[5]);
        Assert.assertEquals(Projection.ORTHOGONAL, vf.getProjection());
        Assert.assertArrayEquals(values, vf.getValues(), 0f);
    }
}
