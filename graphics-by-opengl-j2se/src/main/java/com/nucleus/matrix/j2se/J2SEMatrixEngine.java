package com.nucleus.matrix.j2se;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.NucleusRenderer.MatrixEngine;

/**
 * J2SE implementation of the matrix engine.
 * 
 * @author Richard Sahlin
 *
 */
public class J2SEMatrixEngine implements MatrixEngine {

    public J2SEMatrixEngine() {
    }

    @Override
    public void setProjectionMatrix(ViewFrustum viewFrustum) {
        ViewFrustum.setProjectionMatrix(viewFrustum.getMatrix(), viewFrustum.getProjection(),
                viewFrustum.getValues());
    }

}
