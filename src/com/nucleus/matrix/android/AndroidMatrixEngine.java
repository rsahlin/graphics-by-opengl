package com.nucleus.matrix.android;

import android.opengl.Matrix;
import android.util.Log;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.BaseRenderer.MatrixEngine;

/**
 * Android implementation of Matrix functions.
 * 
 * @author Richard Sahlin
 *
 */
public class AndroidMatrixEngine implements MatrixEngine {

    public final static String ANDROID_MATRIXENGINE_TAG = "AndroidMatrixEngine";

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
            Log.e(ANDROID_MATRIXENGINE_TAG, "Illegal projection: " + viewFrustum.getProjectionType());
        }

    }

}
