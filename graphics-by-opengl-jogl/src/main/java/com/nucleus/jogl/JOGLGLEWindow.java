package com.nucleus.jogl;

import java.awt.Frame;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.nucleus.CoreApp;
import com.nucleus.mmi.PointerData;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;

/**
 * 
 * @author Richard Sahlin
 * The JOGL abstract native window, use by subclasses to render GL content
 * The window shall drive rendering by calling {@link CoreApp#drawFrame()} on a thread that
 * has GL access. This is normally done in the {@link #display(GLAutoDrawable)} method.
 *
 */
public abstract class JOGLGLEWindow implements GLEventListener, MouseListener {

    /**
     * A zoom on the wheel equals 1 / 100 screen height
     */
    private final static float ZOOM_FACTOR = 0.01f;

    private Dimension windowSize;
    private boolean undecorated = false;
    private boolean alwaysOnTop = false;
    private boolean fullscreen = false;
    private boolean mouseVisible = true;
    private boolean mouseConfined = false;
    private int swapInterval = 1;
    protected volatile boolean contextCreated = false;
    protected RenderContextListener listener;
    protected GLCanvas canvas;
    protected Frame frame;
    protected GLWindow glWindow;

    protected GLESWrapper glesWrapper;
    protected CoreApp coreApp;

    public JOGLGLEWindow(int width, int height, GLProfile glProfile, RenderContextListener listener) {
        this.listener = listener;
        windowSize = new Dimension(width, height);
        createNEWTWindow(width, height, glProfile);
    }

    public JOGLGLEWindow(int width, int height, GLProfile glProfile, RenderContextListener listener, int swapInterval) {
        this.listener = listener;
        this.swapInterval = swapInterval;
        windowSize = new Dimension(width, height);
        createNEWTWindow(width, height, glProfile);

    }

    /**
     * Creates the JOGL display and OpenGLES
     * 
     * @param width
     * @param height
     */
    private void createNEWTWindow(int width, int height, GLProfile glProfile) {
        // Display display = NewtFactory.createDisplay(null);
        // Screen screen = NewtFactory.createScreen(display, SCREEN_ID);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        // Window window = NewtFactory.createWindow(glCapabilities);
        glWindow = GLWindow.create(glCapabilities);

        glWindow.setSize(windowSize.getWidth(), windowSize.getHeight());
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.addMouseListener(this);
        GLProfile.initSingleton();
        Animator animator = new Animator();
        animator.add(glWindow);
        animator.start();
    }

    private void createAWTWindow(int width, int height, GLProfile glProfile) {

        GLCapabilities caps = new GLCapabilities(glProfile);
        caps.setBackgroundOpaque(false);
        glWindow = GLWindow.create(caps);

        frame = new java.awt.Frame("Nucleus");
        frame.setSize(width, height);
        frame.setLayout(new java.awt.BorderLayout());
        canvas = new GLCanvas();
        frame.add(canvas, java.awt.BorderLayout.CENTER);
        frame.validate();

        // GLProfile glp = GLProfile.getDefault();
        // GLCapabilities caps = new GLCapabilities(glp);
        // canvas = new GLCanvas(caps);
        // frame = new Frame("JOGL GLESWindow");
        // frame.setSize(width, height);
        // frame.add(canvas);
        // frame.validate();
        // GLProfile.initSingleton();

    }

    public void setTitle(String title) {
        if (frame != null) {
            frame.setTitle(title);
        }
        if (glWindow != null) {
            glWindow.setTitle(title);
        }
    }

    /**
     * Sets the CoreApp in this window.
     * This is used to drive rendering in the {@link #display(GLAutoDrawable)} method.
     * 
     * @param coreApp
     */
    public void setCoreApp(CoreApp coreApp) {
        this.coreApp = coreApp;
    }

    public void setGLEVentListener() {
        if (glWindow != null) {
            glWindow.addGLEventListener(this);
        }
        if (canvas != null) {
            canvas.addGLEventListener(this);
        }
    }

    public void setVisible(boolean visible) {
        if (glWindow != null) {
            glWindow.setVisible(visible);
        }
        if (frame != null) {
            frame.setVisible(visible);
        }

    }

    /**
     * Returns the GLESWrapper for this window - must be created by subclasses in the
     * {@link #init(com.jogamp.opengl.GLAutoDrawable)} method.
     * 
     * @return The GLESWrapper for this window, or null if {@link #init(com.jogamp.opengl.GLAutoDrawable)} has not been
     * called by the system.
     * This normally means that the window has not been made visible.
     */
    public abstract GLESWrapper getGLESWrapper();

    @Override
    public void init(GLAutoDrawable drawable) {
        contextCreated = true;
        listener.contextCreated(getWidth(), getHeight());
        drawable.getGL().setSwapInterval(swapInterval);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        System.out.println("reshape: x,y= " + x + ", " + y + " width,height= " + width + ", " + height);
        windowSize.setWidth(width);
        windowSize.setHeight(height);
    }

    /**
     * Returns the width as reported by the {@link #reshape(GLAutoDrawable, int, int, int, int)} method.
     * 
     * @return
     */
    public int getWidth() {
        return windowSize.getWidth();
    }

    /**
     * Returns the height as reported by the {@link #reshape(GLAutoDrawable, int, int, int, int)} method
     * 
     * @return
     */
    public int getHeight() {
        return windowSize.getHeight();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        coreApp.drawFrame();
    }

    protected void handleMouseEvent(MouseEvent e, PointerAction action) {
        int[] xpos = e.getAllX();
        int[] ypos = e.getAllY();
        int count = e.getPointerCount();
        for (int i = 0; i < count; i++) {
            short id = e.getPointerId(i);
            switch (action) {
            case DOWN:
                // Recording down for multi touch - all pointers will be re-sent when a new finger goes down.
                coreApp.getInputProcessor().pointerEvent(PointerAction.DOWN, e.getWhen(), id,
                        new float[] { xpos[i], ypos[i] });
                break;
            case UP:
                coreApp.getInputProcessor().pointerEvent(PointerAction.UP, e.getWhen(), id, new float[] {
                        xpos[i], ypos[i] });
                break;
            case MOVE:
                coreApp.getInputProcessor().pointerEvent(PointerAction.MOVE, e.getWhen(), id, new float[] {
                        xpos[i], ypos[i] });
            default:
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Pressed");
        handleMouseEvent(e, PointerAction.DOWN);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        handleMouseEvent(e, PointerAction.UP);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        handleMouseEvent(e, PointerAction.MOVE);
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        float factor = Window.getInstance().getHeight() * ZOOM_FACTOR;
        coreApp.getInputProcessor().pointerEvent(PointerAction.ZOOM, e.getWhen(), PointerData.POINTER_1, new float[] {
                e.getRotation()[1] * factor, e.getRotation()[1] * factor });
    }
}
