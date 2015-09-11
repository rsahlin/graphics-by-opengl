package com.nucleus.jogl;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;

public class JOGLGLES20Window extends JOGLGLEWindow {

    private static final String INIT_NOT_CALLED = "init() has not been called";
    private GL2ES2 gles;

    public JOGLGLES20Window(int width, int height) {
        super();
        createWindow(width, height, GLProfile.get(GLProfile.GL2ES2));
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose()");

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gles = drawable.getGL().getGL2ES2();
    }

    @Override
    public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
        System.out.println("reshape: x,y= " + x + ", " + y + " width,height= " + width + ", " + height);
    }

    /**
     * Returns the GL2ES instance, the init() method must have been called before calling this method.
     * 
     * @return The GL2ES instance
     * @throws IllegalStateException If {@link #init(GLAutoDrawable)} has not been called before calling this method.
     */
    public GL2ES2 getGL2ES2() {
        if (gles == null) {
            throw new IllegalStateException(INIT_NOT_CALLED);
        }
        return gles;
    }
}
