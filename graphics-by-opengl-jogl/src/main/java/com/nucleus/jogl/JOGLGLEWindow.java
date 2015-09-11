package com.nucleus.jogl;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

/**
 * 
 * @author Richard Sahlin
 * The JOGL abstract native window, use by subclasses to render GL content
 *
 */
public abstract class JOGLGLEWindow implements GLEventListener {

    private int SCREEN_ID = 0;
    private Dimension windowSize;
    private boolean undecorated = false;
    private boolean alwaysOnTop = false;
    private boolean fullscreen = false;
    private boolean mouseVisible = true;
    private boolean mouseConfined = false;
    protected GLWindow glWindow;

    /**
     * Creates the JOGL display and OpenGLES
     * 
     * @param width
     * @param height
     */
    public void createWindow(int width, int height, GLProfile glProfile) {
        windowSize = new Dimension(width, height);
        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, SCREEN_ID);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(windowSize.getWidth(), windowSize.getHeight());
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.addGLEventListener(this);
        glWindow.setVisible(true);

    }

}
