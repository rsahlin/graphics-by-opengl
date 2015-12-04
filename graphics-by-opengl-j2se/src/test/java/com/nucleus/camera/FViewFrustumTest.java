package com.nucleus.camera;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.camera.ViewFrustum.Projection;
import com.nucleus.vecmath.Matrix;

public class FViewFrustumTest {

    @Test
    public void testCreate() {
        // Tests that the frustum is created and holds the proper data.
        ViewFrustum vf = new ViewFrustum();
        Assert.assertEquals(Matrix.MATRIX_ELEMENTS, vf.getMatrix().length);
        Assert.assertEquals(ViewFrustum.PROJECTION_SIZE, vf.getValues().length);
    }

    @Test
    public void testSetProjection() {
        ViewFrustum vf = new ViewFrustum();
        float[] values = new float[] { 1, 2, 3, 4, 5, 6 };
        vf.setOrthoProjection(values[0], values[1], values[2], values[3], values[4], values[5]);
        Assert.assertEquals(Projection.ORTHOGONAL, vf.getProjection());
        Assert.assertArrayEquals(values, vf.getValues(), 0f);
    }
}
