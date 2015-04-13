package com.nucleus.matrix;

import com.nucleus.camera.ViewFrustum;

/**
 * Matrix functions that may be accelerated on target platform.
 * 
 * @author Richard Sahlin
 *
 */
public interface MatrixEngine {

    /**
     * Sets the projection matrix to be used by the renderer based on the setting in the viewFrustum
     * 
     * @param viewFrustum
     * 
     */
    public abstract void setProjectionMatrix(ViewFrustum viewFrustum);

}
