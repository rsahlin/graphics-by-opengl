package com.nucleus.camera;

import com.google.gson.annotations.SerializedName;
import com.nucleus.vecmath.Axis;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Transform;

/**
 * The setup of the viewfrustum.
 * Use this to control projection to screen coordinates.
 * This class may be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class ViewFrustum {

    public static final String VIEWFRUSTUM = "viewFrustum";
    public static final String VALUES = "values";
    public static final String PROJECTION = "projection";

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
    @SerializedName(VALUES)
    private float[] values = new float[PROJECTION_SIZE];
    @SerializedName(PROJECTION)
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
     * Sets the projection to the matrix
     * 
     * @param matrix The matrix where the projection will be set
     * @return The matrix containing the projection of this ViewFrustum
     */
    public float[] getMatrix(float[] matrix) {
        setProjectionMatrix(matrix, projection, values);
        return matrix;
    }

    /**
     * Sets the projection in the matrix
     * 
     * @param matrix The matrix to set the view projection in
     * @param projection The projection type, PERSPECTIVE, ORTHOGONAL
     * @param values The projection (frustum) values.
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
                float[] perspective = Matrix.createProjectionMatrix(values[0], values[1], values[2], values[3]);
                Matrix.copy(perspective, 0, matrix, 0);
                break;
            default:
                System.err.println("Illegal projection: " + projection);
        }
    }

    /**
     * Returns the width of the view frustum, delta between right and left.
     * 
     * @return
     */
    public float getWidth() {
        return Math.abs(values[RIGHT_INDEX] - values[LEFT_INDEX]);
    }

    /**
     * Returns the height of the view frustum, delta between top and bottom
     * 
     * @return
     */
    public float getHeight() {
        return Math.abs(values[TOP_INDEX] - values[BOTTOM_INDEX]);
    }

    /**
     * Returns the depth of the viewfrustum
     * 
     * @return
     */
    public float getDepth() {
        return Math.abs(values[FAR_INDEX] - values[BOTTOM_INDEX]);
    }

    /**
     * Sets the left/right values of the viewfrustum
     * 
     * @param left
     * @param right
     */
    public void setLeftRight(float left, float right) {
        values[LEFT_INDEX] = left;
        values[RIGHT_INDEX] = right;
    }

    /**
     * Sets the bottom/top values of the viewfrustum
     * 
     * @param bottom
     * @param top
     */
    public void setBottomTop(float bottom, float top) {
        values[BOTTOM_INDEX] = bottom;
        values[TOP_INDEX] = top;
    }

    /**
     * Transform the viewfrustum and store in result
     * Currently only works for scaling
     * 
     * @param result Must have place for 6 values
     * @param transform
     * @throws NullPointerException If result or transform is null
     * @throws IndexOutOfBoundsException If result does not have room for values
     */
    public void getTransformedValues(float[] result, Transform transform) {
        float[] scale = transform.getScale();
        result[LEFT_INDEX] = scale[Axis.X.index] * values[LEFT_INDEX];
        result[RIGHT_INDEX] = scale[Axis.X.index] * values[RIGHT_INDEX];
        result[BOTTOM_INDEX] = scale[Axis.Y.index] * values[BOTTOM_INDEX];
        result[TOP_INDEX] = scale[Axis.Y.index] * values[TOP_INDEX];
        result[NEAR_INDEX] = scale[Axis.Z.index] * values[NEAR_INDEX];
        result[FAR_INDEX] = scale[Axis.Z.index] * values[FAR_INDEX];
    }

    /**
     * Copies the values in this viewfrustum to destination
     * 
     * @param result
     */
    public void getValues(float[] result) {
        System.arraycopy(values, 0, result, 0, PROJECTION_SIZE);
    }

}
