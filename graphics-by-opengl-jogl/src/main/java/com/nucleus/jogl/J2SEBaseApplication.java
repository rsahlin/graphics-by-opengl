package com.nucleus.jogl;

import com.nucleus.matrix.j2se.J2SEMatrixEngine;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.renderer.RendererFactory.Renderers;
import com.nucleus.texture.j2se.J2SEImageFactory;

public class J2SEBaseApplication {

    private JOGLGLES20Window window;
    private GLES20Wrapper gles;
    private NucleusRenderer renderer;

    public static void main(String[] args) {
        J2SEBaseApplication main = new J2SEBaseApplication();
        main.create();
    }

    public void create() {
        window = new JOGLGLES20Window(1920, 1080);
        gles = new JOGLGLES20Wrapper(window.getGL2ES2());
        renderer = RendererFactory.getRenderer(Renderers.GLES20, gles, new J2SEImageFactory(),
                new J2SEMatrixEngine());

    }

}
