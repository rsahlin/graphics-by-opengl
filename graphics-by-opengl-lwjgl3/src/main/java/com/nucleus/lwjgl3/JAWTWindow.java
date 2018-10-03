package com.nucleus.lwjgl3;

import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.lwjgl.opengles.GLES;
import org.lwjgl.system.Platform;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.SurfaceConfiguration;

public class JAWTWindow extends J2SEWindow
        implements RenderContextListener, MouseMotionListener, MouseListener, MouseWheelListener {

    LWJGLCanvas canvas;
    JFrame frame;

    public JAWTWindow(Renderers version, CoreApp.CoreAppStarter coreAppStarter, SurfaceConfiguration config, int width,
            int height) {
        super(coreAppStarter, width, height, config);
        init(version, coreAppStarter, width, height);
    }

    private void init(Renderers version, CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        Platform platform = Platform.get();
        SimpleLogger.d(getClass(), "Init windows for platform " + platform);
        switch (platform) {
            case WINDOWS:
                canvas = new LWJGLWindowsCanvas(this, width, height);
                break;
            case LINUX:
                canvas = new LWJGLLinuxCanvas(this, width, height);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + Platform.get());
        }

        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseWheelListener(this);
        frame = new JFrame("JAWT Demo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                canvas.destroy();
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                frame.dispose();
                return true;
            }

            return false;
        });

        frame.setLayout(new BorderLayout());
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        // Do not make since callback may happen before this window is created in the j2sewindow
    }

    @Override
    public void internalCreateCoreApp(int width, int height) {
        wrapper = LWJGLWrapperFactory.createWrapper(GLES.createCapabilities(), null);
        super.internalCreateCoreApp(width, height);
    }

    @Override
    public void contextCreated(int width, int height) {
        coreAppStarter.createCoreApp(width, height);
        internalContextCreated(width, height);
    }

    @Override
    public void setCoreApp(CoreApp coreApp) {
        super.setCoreApp(coreApp);
        canvas.setCoreApp(coreApp);
    }

    protected void handleMouseEvent(MouseEvent e, PointerAction action) {
        int xpos = e.getX();
        int ypos = e.getY();
        Type type = Type.STYLUS;
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                type = Type.MOUSE;
                break;
            case MouseEvent.BUTTON2:
                type = Type.ERASER;
                break;
            case MouseEvent.BUTTON3:
                type = Type.FINGER;
                break;

        }
        handleMouseEvent(action, type, xpos, ypos, 0, e.getWhen());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
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
    public void mouseDragged(MouseEvent e) {
        handleMouseEvent(e, PointerAction.MOVE);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceLost() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseWheelMoved(-e.getWheelRotation(), e.getWhen());
    }

}
