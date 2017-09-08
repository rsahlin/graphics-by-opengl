package com.nucleus.lwjgl3;

import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.nucleus.CoreApp;
import com.nucleus.J2SEWindow;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;

public class JAWTWindow extends J2SEWindow implements RenderContextListener {

    LWJGLCanvas canvas;

    public JAWTWindow(CoreApp.CoreAppStarter coreAppStarter, int width, int height) {
        super(coreAppStarter, width, height);
        init(width, height, coreAppStarter);
    }

    private void init(int width, int height, CoreApp.CoreAppStarter coreAppStarter) {
        canvas = new LWJGLCanvas(this, width, height);
        final JFrame frame = new JFrame("JAWT Demo");
        this.coreAppStarter = coreAppStarter;
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
        wrapper = new LWJGL3GLES20Wrapper();
    }

    @Override
    public void contextCreated(int width, int height) {
        coreAppStarter.createCoreApp(width, height);
        coreApp.contextCreated(width, height);
    }

}
