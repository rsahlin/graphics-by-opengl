package com.nucleus.jogl;

import com.jogamp.opengl.GL4ES3;

public class JOGLGLES30Wrapper extends JOGLGLES20Wrapper {

    /**
     * Creates a new instance of the GLES30 wrapper for JOGL
     * 
     * @param gles The JOGL GLES30 instance
     * @throws IllegalArgumentException If gles is null
     */
    public JOGLGLES30Wrapper(GL4ES3 gles) {
        super(gles);
    }

}
