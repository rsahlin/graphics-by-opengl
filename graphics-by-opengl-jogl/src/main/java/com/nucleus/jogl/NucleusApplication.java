package com.nucleus.jogl;

import com.nucleus.CoreApp;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.matrix.j2se.J2SEMatrixEngine;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.RendererFactory;
import com.nucleus.texturing.J2SEImageFactory;

/**
 * Base class for an application using {@link NucleusRenderer} through JOGL
 * The purpose of this class is to separate JOGL specific init and startup from shared code.
 * 
 * @author Richard Sahlin
 *
 */
public class NucleusApplication implements CoreAppStarter, RenderContextListener {

    protected JOGLGLES20Window window;
    protected CoreApp coreApp;
    protected int swapInterval = 1;
    protected int windowWidth = 480;
    protected int windowHeight = 800;

    @Override
    public void createCore(Renderers version) {
        window = new JOGLGLES20Window(windowWidth, windowHeight, this, swapInterval);
        window.setGLEVentListener();
        // Setting window to visible will trigger the GLEventListener, on the same or another thread.
        window.setVisible(true);
    }

    @Override
    public void contextCreated(int width, int height) {
        coreApp = new CoreApp(RendererFactory.getRenderer(window.getGLESWrapper(), new J2SEImageFactory(),
                new J2SEMatrixEngine()));
        coreApp.getRenderer().init();
        coreApp.contextCreated(window.getWidth(), window.getHeight());
        window.setCoreApp(coreApp);
    }

    /**
     * Returns the {@link NucleusRenderer} renderer - do NOT call this method before {@link #contextCreated(int, int)}
     * has been called by the renderer.
     * 
     * 
     * @return The renderer, or null if {@link #contextCreated(int, int)} has not been called by the renderer.
     */
    public NucleusRenderer getRenderer() {
        if (coreApp == null) {
            return null;
        }
        return coreApp.getRenderer();
    }

}
