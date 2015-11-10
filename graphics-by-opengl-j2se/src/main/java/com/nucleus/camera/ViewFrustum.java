package com.nucleus.camera;

/**
 * The setup of the render viewfrustum.
 * This includes the viewport transform, the dimension of the view frustum and support for view matrix.
 * Use this to control the render area on screen, the projection (frustum or orthogonal) and view matrixes.
 * 
 * @author Richard Sahlin
 *
 */
public class ViewFrustum {

    public final static int VIEWPORT_X = 0;
    public final static int VIEWPORT_Y = 1;
    public final static int VIEWPORT_WIDTH = 2;
    public final static int VIEWPORT_HEIGHT = 3;
    /**
     * Number of values for the viewport.
     */
    public final static int VIEWPORT_LENGTH = 4;
    /**
     * Number of values for the projection
     */
    public final static int PROJECTION_LENGTH = 6;
    /**
     * Number of values for the matrix
     */
    public final static int MATRIX_LENGTH = 16;

    public final static int LEFT_INDEX = 0;
    public final static int RIGHT_INDEX = 1;
    public final static int BOTTOM_INDEX = 2;
    public final static int TOP_INDEX = 3;
    public final static int NEAR_INDEX = 4;
    public final static int FAR_INDEX = 5;

    public final static int PROJECTION_ORTHOGONAL = 1;
    public final static int PROJECTION_PERSPECTIVE = 2;

    /**
     * The viewport setting, same as glViewPort, units are in pixels. This is the transform from normalized device
     * coordinates to window/screen coordinates. Normally set to 0,0,width,height
     * x,y,width,height
     */
    private int[] viewPort = new int[VIEWPORT_LENGTH];

    /**
     * The projection, frustum or orthogonal
     */
    private float[] projectionMatrix = new float[MATRIX_LENGTH];
    /**
     * The projection values, left,right,bottom,top,near,far
     */
    private float[] projection = new float[] { -0.5f, 0.5f, -0.5f, 0.5f, 0, 1 };
    private int projectionType = PROJECTION_PERSPECTIVE;

    /**
     * Sets the dimension, in pixels, for the screen viewport.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setViewPort(int x, int y, int width, int height) {
        viewPort[VIEWPORT_X] = x;
        viewPort[VIEWPORT_Y] = y;
        viewPort[VIEWPORT_WIDTH] = width;
        viewPort[VIEWPORT_HEIGHT] = height;
    }

    /**
     * Fetches the viewport, this is a reference to the array holding the values. Do not modify these values,
     * use setViewPort to change.
     * 
     * @return
     */
    public int[] getViewPort() {
        return viewPort;
    }

    /**
     * Sets the projection to be orthogonal (2D)
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     */
    public void setOrthoProjection(float left, float right, float bottom, float top, float near, float far) {
        projection[LEFT_INDEX] = left;
        projection[RIGHT_INDEX] = right;
        projection[BOTTOM_INDEX] = bottom;
        projection[TOP_INDEX] = top;
        projection[NEAR_INDEX] = near;
        projection[FAR_INDEX] = far;
        projectionType = PROJECTION_ORTHOGONAL;
    }

    public void setPerspectiveProjection(float left, float right, float bottom, float top, float near, float far) {
        projection[LEFT_INDEX] = left;
        projection[RIGHT_INDEX] = right;
        projection[BOTTOM_INDEX] = bottom;
        projection[TOP_INDEX] = top;
        projection[NEAR_INDEX] = near;
        projection[FAR_INDEX] = far;
        projectionType = PROJECTION_PERSPECTIVE;
    }

    /**
     * Returns the projection type, PROJECTION_ORTHOGONAL or PROJECTION_PERSPECTIVE
     * 
     * @return PROJECTION_ORTHOGONAL or PROJECTION_PERSPECTIVE
     */
    public int getProjectionType() {
        return projectionType;
    }

    /**
     * Returns a reference to the projection values, left,right,bottom,top,near,far - do not modify.
     * 
     * @return
     */
    public float[] getProjection() {
        return projection;
    }

    /**
     * Returns a reference to the projection matrix.
     * This is currently NOT set by this class since we have not implemented Matrix classes.
     * For now, it is up to the user of this class to set the projection matrix before using it.
     * 
     * @return
     */
    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

}
