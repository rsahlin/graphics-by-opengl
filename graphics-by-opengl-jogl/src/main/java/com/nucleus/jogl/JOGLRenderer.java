package com.nucleus.jogl;

import com.nucleus.matrix.j2se.J2SEMatrixEngine;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.RendererFactory.Renderers;
import com.nucleus.texture.j2se.J2SEImageFactory;

public class JOGLRenderer {

    private NucleusRenderer renderer;
    private JOGLGLES20Wrapper gles;

    public JOGLRenderer() {
        renderer = RendererFactory.getRenderer(Renderers.GLES20, gles, new J2SEImageFactory(), new J2SEMatrixEngine());
    }
}
