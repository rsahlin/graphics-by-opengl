package com.nucleus.camera;

import com.google.gson.annotations.SerializedName;
import com.nucleus.vecmath.Matrix;

/**
 * The setup of the viewfrustum.
 * Use this to control projection to screen coordinates.
 * This class may be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class ViewFrustum {

    public enum Projection {
        PERSPECTIVE(),
        ORTHOGONAL();
    }

    /**
     * Number of values for the projection
     */
    public final static int PROJECTION_SIZE = 6;
    public final static int LEFT_INDEX = 0;
    public final static int RIGHT_INDEX = 1;
    public final static int BOTTOM_INDEX = 2;
    public final static int TOP_INDEX = 3;
    public final static int NEAR_INDEX = 4;
    public final static int FAR_INDEX = 5;

    /**
     * The projection values, left,right,bottom,top,near,far
     */
    @SerializedName("values")
    private float[] values = new float[] { -0.5f, 0.5f, -0.5f, 0.5f, 0, 1 };
    @SerializedName("projection")
    private Projection projection = Projection.PERSPECTIVE;

    /**
     * Default constructor
     */
    public ViewFrustum() {
    }

    /**
     * Creates a copy of the specified view frustum
     * 
     * @param source
     */
    public ViewFrustum(ViewFrustum source) {
        set(source);
    }

    /**
     * Copies the values from the source viewfrustum to this
     * 
     * @param source
     */
    public void set(ViewFrustum source) {
        setProjection(source.projection, source.values);
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
        values[LEFT_INDEX] = left;
        values[RIGHT_INDEX] = right;
        values[BOTTOM_INDEX] = bottom;
        values[TOP_INDEX] = top;
        values[NEAR_INDEX] = near;
        values[FAR_INDEX] = far;
        projection = Projection.ORTHOGONAL;
    }

    /**
     * Sets the projection to perspective
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     */
    public void setPerspectiveProjection(float left, float right, float bottom, float top, float near, float far) {
        values[LEFT_INDEX] = left;
        values[RIGHT_INDEX] = right;
        values[BOTTOM_INDEX] = bottom;
        values[TOP_INDEX] = top;
        values[NEAR_INDEX] = near;
        values[FAR_INDEX] = far;
        projection = Projection.PERSPECTIVE;
    }

    /**
     * Sets the projection to PERSPECTIVE or ORTHOGONAL and sets left,right,bottom,top,near and far values.
     * 
     * @param projection
     * @param values
     */
    public void setProjection(Projection projection, float[] values) {
        setValues(values);
        this.projection = projection;

    }

    /**
     * Sets the values for the left, right, bottom, top, near and far.
     * This does not change the projection type
     * 
     * @param values
     */
    public void setValues(float[] values) {
        this.values[LEFT_INDEX] = values[LEFT_INDEX];
        this.values[RIGHT_INDEX] = values[RIGHT_INDEX];
        this.values[BOTTOM_INDEX] = values[BOTTOM_INDEX];
        this.values[TOP_INDEX] = values[TOP_INDEX];
        this.values[NEAR_INDEX] = values[NEAR_INDEX];
        this.values[FAR_INDEX] = values[FAR_INDEX];
    }

    /**
     * Returns the projection type
     * 
     * @return ORTHOGONAL or PERSPECTIVE
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * Returns a reference to the projection values, left,right,bottom,top,near,far - do not modify.
     * 
     * @return
     */
    public float[] getValues() {
        return values;
    }

    /**
     * Returns this view projection as a matrix, the matrix will be calculated.
     * 
     * @return The view projection as a matrix
     */
    public float[] getMatrix() {
        float[] matrix = Matrix.createMatrix();
        setProjectionMatrix(matrix, projection, values);
        return matrix;
    }

    /**
     * Sets the projection in the matrix
     * 
     * @param matrix The matrix to set the view projection in
     * @param projection The projection type, PERSPECTIVE, ORTHOGONAL
     * @param values The projection (frustum) values. left,right, bottom,top, near and far
     */
    public static void setProjectionMatrix(float[] matrix, Projection projection, float[] values) {
        switch (projection) {
        case ORTHOGONAL:
            Matrix.orthoM(matrix, 0, values[ViewFrustum.LEFT_INDEX],
                    values[ViewFrustum.RIGHT_INDEX], values[ViewFrustum.BOTTOM_INDEX],
                    values[ViewFrustum.TOP_INDEX], values[ViewFrustum.NEAR_INDEX],
                    values[ViewFrustum.FAR_INDEX]);
            break;
        case PERSPECTIVE:
            Matrix.frustumM(matrix, 0, values[ViewFrustum.LEFT_INDEX],
                    values[ViewFrustum.RIGHT_INDEX], values[ViewFrustum.BOTTOM_INDEX],
                    values[ViewFrustum.TOP_INDEX], values[ViewFrustum.NEAR_INDEX],
                    values[ViewFrustum.FAR_INDEX]);
            break;
        default:
            System.err.println("Illegal projection: " + projection);
        }
    }

}
