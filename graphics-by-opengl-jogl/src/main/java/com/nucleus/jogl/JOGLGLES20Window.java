package com.nucleus.jogl;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;

/**
 * Window for a GLES2 renderer, this class must create the correct {@link GLESWrapper}
 * 
 * @author Richard Sahlin
 *
 */
public class JOGLGLES20Window extends JOGLGLEWindow {

    public JOGLGLES20Window(int width, int height, RenderContextListener listener) {
        super(width, height, GLProfile.get(GLProfile.GL2ES2), listener);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (glesWrapper != null) {
            ((JOGLGLES20Wrapper) glesWrapper).freeNames();
        }
        super.display(drawable);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose()");
        System.exit(0);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        if (glesWrapper == null) {
            glesWrapper = new JOGLGLES20Wrapper(drawable.getGL().getGL2ES2());
        }
        super.init(drawable);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        super.reshape(drawable, x, y, width, height);
    }

    @Override
    public GLESWrapper getGLESWrapper() {
        return glesWrapper;
    }

}
