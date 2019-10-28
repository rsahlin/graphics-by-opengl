package com.nucleus.jogl;

import java.awt.Frame;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;
import java.lang.reflect.Field;
import java.util.Hashtable;

import com.jogamp.common.os.Platform;
import com.jogamp.nativewindow.util.InsetsImmutable;
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.nucleus.Backend.BackendFactory;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.CoreAppStarter;
import com.nucleus.J2SEWindow;
import com.nucleus.J2SEWindowApplication.PropertySettings;
import com.nucleus.SimpleLogger;
import com.nucleus.mmi.Key.Action;
import com.nucleus.mmi.Pointer;
import com.nucleus.mmi.Pointer.PointerAction;
import com.nucleus.mmi.Pointer.Type;
import com.nucleus.renderer.NucleusRenderer.Renderers;
import com.nucleus.renderer.SurfaceConfiguration;

/**
 * 
 * @author Richard Sahlin
 * The JOGL abstract native window, use by subclasses to render GL content
 * The window shall drive rendering by calling {@link CoreApp#drawFrame()} on a thread that
 * has GL access. This is normally done in the {@link #display(GLAutoDrawable)} method.
 *
 */
public abstract class JOGLGLWindow extends J2SEWindow
        implements GLEventListener, MouseListener, MouseMotionListener, java.awt.event.MouseListener,
        com.jogamp.newt.event.WindowListener,
        KeyListener, WindowListener {

    private boolean alwaysOnTop = false;
    private boolean mouseVisible = true;
    private boolean mouseConfined = false;
    private boolean autoSwapBuffer = false;
    protected volatile boolean contextCreated = false;
    protected GLCanvas canvas;
    protected Frame frame;
    protected GLWindow glWindow;
    Animator animator;
    private Hashtable<Integer, Integer> AWTKeycodes;

    /**
     * Creates a new JOGL window with the specified {@link CoreAppStarter} and swapinterval
     * 
     * @throws IllegalArgumentException If coreAppStarter is null
     */
    public JOGLGLWindow(BackendFactory factory, CoreAppStarter coreAppStarter, PropertySettings appSettings) {
        super(factory, coreAppStarter, appSettings);
    }

    @Override
    public VideoMode init(PropertySettings appSettings) {
        version = appSettings.version;
        if (coreAppStarter == null) {
            throw new IllegalArgumentException("CoreAppStarter is null");
        }
        switch (appSettings.windowType) {
            case NEWT:
                createNEWTWindow(appSettings);
                break;
            case JAWT:
                createAWTWindow(appSettings);
                break;
            default:
                throw new IllegalArgumentException("Invalid windowtype for JOGL: " + appSettings.windowType);
        }

        /**
         * Fetch jogamp.newt fields that start with VK_ and store keycodes in array to convert to AWT values.
         */
        AWTKeycodes = getAWTFields();
        return new VideoMode(appSettings.width, appSettings.height, appSettings.fullscreen, appSettings.swapInterval);
    }

    private Hashtable<Integer, Integer> getAWTFields() {
        Hashtable<Integer, Integer> awtFields = new Hashtable<>();
        for (Field newtField : com.jogamp.newt.event.KeyEvent.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(newtField.getModifiers())) {
                String fieldName = newtField.getName();
                if (fieldName.startsWith("VK_")) {
                    try {
                        Field awtField = java.awt.event.KeyEvent.class.getField(fieldName);
                        int newtKeyCode = newtField.getShort(null) & 0xffff;
                        int awtKeyCode = awtField.getInt(null);
                        awtFields.put(newtKeyCode, awtKeyCode);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        SimpleLogger.d(getClass(), e.toString());
                    }
                }
            }
        }
        return awtFields;
    }

    protected GLProfile getProfile(Renderers version) {
        SimpleLogger.d(getClass(), "os.and.arch: " + Platform.os_and_arch);
        GLProfile defaultProfile = null;
        try {
            defaultProfile = GLProfile.getDefault();
            if (defaultProfile != null) {
                SimpleLogger.d(getClass(), "Default profile implName: " + defaultProfile.getImplName() + ", name: "
                        + defaultProfile.getName());
            } else {
                SimpleLogger.d(getClass(), "Default profile is NULL");
            }
        } catch (Throwable t) {
            // Not much to do
            SimpleLogger.d(getClass(), "Internal error when fetching default profile");
        }
        GLProfile profile = null;
        switch (version) {
            case GLES20:
                if (defaultProfile != null && (defaultProfile.isGLES2() || defaultProfile.isGL2ES2())) {
                    profile = defaultProfile;
                } else {
                    profile = GLProfile.get(GLProfile.GL2ES2);
                }
                break;
            case GLES30:
            case GLES31:
            case GLES32:
                if (defaultProfile != null && (defaultProfile.isGLES3() || defaultProfile.isGL4ES3())) {
                    profile = defaultProfile;
                } else {
                    profile = GLProfile.get(GLProfile.GL4ES3);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid version " + version);
        }
        return profile;
    }

    /**
     * Creates the JOGL display and OpenGLES
     * 
     * @param width
     * @param height
     * @param profile
     * @version
     */
    private void createNEWTWindow(PropertySettings appSettings) {
        GLProfile profile = getProfile(appSettings.version);
        SurfaceConfiguration config = appSettings.getConfiguration();
        GLProfile.initSingleton();
        GLCapabilities glCapabilities = new GLCapabilities(profile);
        glCapabilities.setSampleBuffers(config.getSamples() > 0);
        glCapabilities.setNumSamples(config.getSamples());
        glCapabilities.setBackgroundOpaque(true);
        glCapabilities.setAlphaBits(0);
        glWindow = GLWindow.create(glCapabilities);
        glWindow.setUndecorated(appSettings.windowUndecorated);
        InsetsImmutable insets = glWindow.getInsets();
        glWindow.setSize(
                appSettings.windowUndecorated ? appSettings.width
                        : appSettings.width + insets.getTotalWidth(),
                appSettings.windowUndecorated ? appSettings.height
                        : appSettings.height + insets.getTotalHeight());
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.addMouseListener(this);
        glWindow.addWindowListener(this);
        glWindow.addKeyListener(this);
        glWindow.addWindowListener(this);
        glWindow.addGLEventListener(this);
        animator = new Animator();
        animator.add(glWindow);
        animator.start();
        glWindow.setAutoSwapBufferMode(autoSwapBuffer);
    }

    private void createAWTWindow(PropertySettings appSettings) {
        GLProfile profile = getProfile(appSettings.version);
        GLProfile.initSingleton();
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setBackgroundOpaque(true);
        caps.setAlphaBits(0);
        // glWindow = GLWindow.create(caps);
        frame = new java.awt.Frame("Nucleus");
        frame.setSize(appSettings.width, appSettings.height);
        frame.setLayout(new java.awt.BorderLayout());
        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        frame.add(canvas, java.awt.BorderLayout.CENTER);
        frame.validate();
        frame.addWindowListener(this);
        animator = new Animator();
        animator.add(canvas);
        animator.start();
        canvas.setAutoSwapBufferMode(autoSwapBuffer);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
    }

    @Override
    public void setWindowTitle(String title) {
        if (frame != null) {
            frame.setTitle(title);
        }
        if (glWindow != null) {
            glWindow.setTitle(title);
        }
    }

    public void setGLEVentListener() {
        if (glWindow != null) {
            glWindow.addGLEventListener(this);
        }
        if (canvas != null) {
            canvas.addGLEventListener(this);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (glWindow != null) {
            glWindow.setVisible(visible);
        }
        if (frame != null) {
            frame.setVisible(visible);
        }

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        internalCreateCoreApp(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        drawable.swapBuffers();
        drawable.getGL().setSwapInterval(videoMode.getSwapInterval());
        internalContextCreated(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        SimpleLogger.d(getClass(), "reshape: x,y= " + x + ", " + y + " width,height= " + width + ", " + height);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (!autoSwapBuffer) {
            coreApp.renderFrame();
            if (glWindow != null) {
                glWindow.swapBuffers();
            } else if (canvas != null) {
                canvas.swapBuffers();
            }
        }
    }

    protected void handleMouseEvent(MouseEvent e, PointerAction action) {
        int[] xpos = e.getAllX();
        int[] ypos = e.getAllY();
        int count = e.getPointerCount();
        Type type = Type.STYLUS;
        for (int i = 0; i < count; i++) {
            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    type = Type.MOUSE;
                    break;
                case MouseEvent.BUTTON2:
                    type = Type.MOUSE;
                    break;
                case MouseEvent.BUTTON3:
                    type = Type.MOUSE;
                    break;
                case MouseEvent.BUTTON4:
                    type = Type.MOUSE;
                    break;
                case MouseEvent.BUTTON5:
                    type = Type.MOUSE;
                    break;
                case MouseEvent.BUTTON6:
                    type = Type.MOUSE;
                    break;
            }
            handleMouseEvent(action, type, xpos[i], ypos[i], e.getPointerId(i), e.getWhen());
        }
    }

    protected void handleKeyEvent(KeyEvent event) {
        /**
         * com.jogamp.newt.event.KeyEvent keycodes are the same as the AWT KeyEvent keycodes.
         */
        SimpleLogger.d(getClass(), "KeyEvent " + event.getEventType() + " : " + event.getKeyCode());
        switch (event.getEventType()) {
            case KeyEvent.EVENT_KEY_PRESSED:
                super.handleKeyEvent(new com.nucleus.mmi.Key(Action.PRESSED,
                        AWTKeycodes.get((int) event.getKeyCode())));
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        onBackPressed();
                }
                break;
            case KeyEvent.EVENT_KEY_RELEASED:
                super.handleKeyEvent(new com.nucleus.mmi.Key(Action.RELEASED,
                        AWTKeycodes.get((int) event.getKeyCode())));
                break;
            default:
                // Do nothing
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
        mouseWheelMoved(e.getRotation()[1], e.getWhen());
    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        handleMouseEvent(PointerAction.MOVE, Type.MOUSE, e.getX(), e.getX(), Pointer.POINTER_1, e.getWhen());
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        handleMouseEvent(PointerAction.DOWN, Type.MOUSE, e.getX(), e.getX(), Pointer.POINTER_1, e.getWhen());
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        handleMouseEvent(PointerAction.UP, Type.MOUSE, e.getX(), e.getX(), Pointer.POINTER_1, e.getWhen());
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    @Override
    public void windowResized(WindowEvent e) {
    }

    @Override
    public void windowMoved(WindowEvent e) {
    }

    @Override
    public void windowDestroyNotify(WindowEvent e) {
        windowClosed();
    }

    @Override
    public void windowDestroyed(WindowEvent e) {
        windowClosed();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
    }

    @Override
    public void windowRepaint(WindowUpdateEvent e) {
    }

    @Override
    public void windowOpened(java.awt.event.WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        SimpleLogger.d(getClass(), "Window closing");
        windowClosed();
    }

    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
        SimpleLogger.d(getClass(), "Window closed");
    }

    @Override
    public void windowIconified(java.awt.event.WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(java.awt.event.WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowActivated(java.awt.event.WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeactivated(java.awt.event.WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getModifiers() & InputEvent.AUTOREPEAT_MASK) == 0) {
            handleKeyEvent(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if ((e.getModifiers() & InputEvent.AUTOREPEAT_MASK) == 0) {
            handleKeyEvent(e);
        }
    }

    @Override
    public VideoMode setVideoMode(VideoMode videoMode, int monitorIndex) {
        switch (windowType) {
            case JAWT:
                break;
            case NEWT:
                glWindow.setFullscreen(videoMode.isFullScreen());
                break;
            default:
                throw new IllegalArgumentException("Invalid windowtype: " + windowType);
        }
        return videoMode;
    }

    @Override
    public void destroy() {
        if (animator != null) {
            animator.stop();
        }
    }

}
