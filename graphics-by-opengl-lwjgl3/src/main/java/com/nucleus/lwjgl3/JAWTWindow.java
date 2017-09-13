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

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;

public class JAWTWindow extends J2SEWindow implements RenderContextListener, MouseMotionListener, MouseListener {

    LWJGLCanvas canvas;

    public JAWTWindow(CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        super(coreAppStarter, width, height);
        init(coreAppStarter, width, height);
    }

    private void init(CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        canvas = new LWJGLCanvas(this, width, height);
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
        wrapper = new LWJGL3GLES20Wrapper();
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
        handleMouseEvent(e.getX(), e.getY(), 0, e.getWhen(), PointerAction.DOWN);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        handleMouseEvent(e.getX(), e.getY(), 0, e.getWhen(), PointerAction.UP);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        handleMouseEvent(e.getX(), e.getY(), 0, e.getWhen(), PointerAction.MOVE);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
    }

}
