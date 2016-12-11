package com.nucleus.jogl;

import com.nucleus.CoreApp;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.J2SELogger;
import com.nucleus.SimpleLogger;
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
public class NucleusApplication implements CoreAppStarter, RenderContextListener, WindowListener {

    public final static String WINDOW_WIDTH_KEY = "WINDOW-WIDTH";
    public final static String WINDOW_HEIGHT_KEY = "WINDOW-HEIGHT";

    protected JOGLGLES20Window window;
    protected CoreApp coreApp;
    protected int swapInterval = 1;
    protected int windowWidth = 480;
    protected int windowHeight = 800;

    public NucleusApplication(String[] args, Renderers version) {
        SimpleLogger.setLogger(new J2SELogger());
        setProperties(args);
        createCore(version);
    }

    /**
     * Reads arguments from the VM and sets
     * 
     * @param args
     */
    protected void setProperties(String[] args) {
        if (args == null) {
            return;
        }
        for (String str : args) {

            if (str.toUpperCase().startsWith(WINDOW_WIDTH_KEY)) {
                windowWidth = Integer.parseInt(str.substring(WINDOW_WIDTH_KEY.length() + 1));
                System.out.println(WINDOW_WIDTH_KEY + " set to " + windowWidth);
            }
            if (str.toUpperCase().startsWith(WINDOW_HEIGHT_KEY)) {
                windowHeight = Integer.parseInt(str.substring(WINDOW_HEIGHT_KEY.length() + 1));
                System.out.println(WINDOW_HEIGHT_KEY + " set to " + windowHeight);
            }
        }
    }

    private void createGLES20Window() {
        window = new JOGLGLES20Window(windowWidth, windowHeight, this, swapInterval);
        window.setGLEVentListener();
        window.setWindowListener(this);
        // Setting window to visible will trigger the GLEventListener, on the same or another thread.
        window.setVisible(true);
    }

    @Override
    public void createCore(Renderers version) {
        switch (version) {
        case GLES20:
            createGLES20Window();
            break;
        default:
            throw new IllegalArgumentException("Not implemented for version " + version);
        }
    }

    @Override
    public void contextCreated(int width, int height) {
        if (coreApp == null) {
            coreApp = new CoreApp(RendererFactory.getRenderer(window.getGLESWrapper(), new J2SEImageFactory(),
                    new J2SEMatrixEngine()));
            window.setCoreApp(coreApp);
        }
        coreApp.contextCreated(width, height);
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

    @Override
    public void resize(int x, int y, int width, int height) {
        if (coreApp != null) {
            coreApp.getRenderer().resizeWindow(x, y, width, height);
        }
    }

    @Override
    public void windowClosed() {
    }
}
