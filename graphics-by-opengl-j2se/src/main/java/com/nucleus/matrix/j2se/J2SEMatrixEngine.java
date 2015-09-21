package com.nucleus.matrix.j2se;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.NucleusRenderer.MatrixEngine;
import com.nucleus.vecmath.Matrix;

public class J2SEMatrixEngine implements MatrixEngine {

    public J2SEMatrixEngine() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setProjectionMatrix(ViewFrustum viewFrustum) {

        float[] projection = viewFrustum.getProjection();
        switch (viewFrustum.getProjectionType()) {
        case ViewFrustum.PROJECTION_ORTHOGONAL:
            Matrix.orthoM(viewFrustum.getProjectionMatrix(), 0, projection[ViewFrustum.LEFT_INDEX],
                    projection[ViewFrustum.RIGHT_INDEX], projection[ViewFrustum.BOTTOM_INDEX],
                    projection[ViewFrustum.TOP_INDEX], projection[ViewFrustum.NEAR_INDEX],
                    projection[ViewFrustum.FAR_INDEX]);
            break;
        default:
            System.err.println("Illegal projection: " + viewFrustum.getProjectionType());
        }

    }

}
