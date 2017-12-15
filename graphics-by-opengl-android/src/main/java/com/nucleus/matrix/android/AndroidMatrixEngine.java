package com.nucleus.matrix.android;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.NucleusRenderer.MatrixEngine;

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
        ViewFrustum.setProjectionMatrix(viewFrustum.getMatrix(), viewFrustum.getProjection(),
                viewFrustum.getValues());
    }

}
