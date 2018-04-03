package com.nucleus.lwjgl3;

import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.lwjgl.system.Platform;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.SimpleLogger;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;

public class JAWTWindow extends J2SEWindow implements RenderContextListener, MouseMotionListener, MouseListener {

    LWJGLCanvas canvas;

    public JAWTWindow(Renderers version, CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        super(coreAppStarter, width, height);
        init(version, coreAppStarter, width, height);
    }

    private void init(Renderers version, CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        Platform platform = Platform.get();
        SimpleLogger.d(getClass(), "Init windows for platform " + platform);
        switch (platform) {
            case WINDOWS:
                canvas = new LWJGLWindowsCanvas(version, this, width, height);
                break;
            case LINUX:
                canvas = new LWJGLLinuxCanvas(version, this, width, height);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + Platform.get());
        }

        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        final JFrame frame = new JFrame("JAWT Demo");
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
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        // TODO How to find GL capabilities so that wrapper can select highest level of GLES
        wrapper = LWJGLWrapperFactory.createWrapper(null, version);
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

}
