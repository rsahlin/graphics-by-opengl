package com.nucleus.bounds;

import com.google.gson.annotations.SerializedName;
import com.nucleus.ErrorMessage;
import com.nucleus.vecmath.Rectangle;

/**
 * Base class for bounds.
 * The bounds can reference a source position to make it possible to track an object that is moving.
 * If source position changes, call {@link #updated()} to flag to implementations that data may need to be recalculated,
 * for instance in case of rectangular bounds.
 * 
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public abstract class Bounds {

    public enum SerializeNames {
        /**
         * Must be aligned with the type SerializedName
         */
        type(),
        /**
         * Must be aligned with the bounds SerializedName
         */
        bounds();
    }

    /**
     * The type of bounds
     * 
     * @author Richard Sahlin
     *
     */
    public enum Type {
        CIRCULAR(1), RECTANGULAR(2);

        private final int value;

        private Type(int value) {
            this.value = value;
        }
    }

    /**
     * Set to true when the bounds have been updated/moved, for some bounds this means that
     * internal data needs to be recalculated.
     */
    transient protected boolean updated = true;

    /**
     * The type of bounds object
     */
    @SerializedName("type")
    protected Type type;
    /**
     * The bounds data, this is implementation specific.
     * For a circular bounds this is one value.
     * For rectangle bounds it is 8 values, this is the bounds without position.
     */
    @SerializedName("bounds")
    protected float[] bounds;

    /**
     * Checks if a single point is within the bounds.
     * 
     * @param position Position array, must contain 3 values in case of 3 dimensional bound.
     * @param index Index into position array where position is
     * @return True if the point is within the bounds
     */
    public abstract boolean isPointInside(float[] position, int index);

    /**
     * Checks if a circular bounds is within the bounds.
     * 
     * @param bounds The bounds to check against this bound.
     * @return True if the bounds are within this bound, ie some parts are touching. Returns
     * false if there is no contact.
     */
    public abstract boolean isCircularInside(CircularBounds bounds);

    /**
     * Checks if a rectangle bounds is inside the bounds.
     * 
     * @param bounds The bounds to check against this bound.
     * @return True if the bounds are within this bound, ie some parts are touching. Returns
     * false if there is no contact.
     */
    public abstract boolean isRectangleInside(RectangularBounds bounds);

    /**
     * Returns the type of bounds.
     * 
     * @return The type of bounds for this class.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the bound data for this bounds.
     * 
     * @return Array containing the bounds values, this is implementation specific.
     */
    public float[] getBounds() {
        return bounds;
    }

    /**
     * Sets the rotation of the bounds to the specified angle, calling this method several times
     * with the same angle will produce the same value, it it will rotate the original bound to the
     * specified angle.
     * For some bounds this will have no meaning, eg rotating a circular bounds along z axis.
     * 
     * @param axis The axis, X, Y or Z (0,1 or 2)
     * @param angle
     */
    public abstract void rotate(int axis, float angle);

    /**
     * Transforms the bounds using the matrix, this will transform from the original bounds.
     * Calling this method multiple times with the same matrix will yield the same ouput, eg the original
     * bounds are preserved.
     * This method can be called with the node model or mvp matrix to produce the transformed bounds.
     * It is up to the caller to make sure that it is the expected matrix that is used, ie what space the bounds
     * shall be in (model?, view?, projection?)
     * 
     * @param matrix The matrix used to transform the boundingbox
     * @param index Index into array where matrix is
     */
    public abstract void transform(float[] matrix, int index);

    /**
     * Flag that this bounds may need updating, this is for instance when the source position has changed.
     * 
     */
    public void updated() {
        updated = true;
    }

    /**
     * Creates the specified bounds, this is used when deserializing.
     * 
     * @param type the type of bounds to create
     * @param bounds The bounds values or null, x,y, width and height
     * @return The implementation of the bounds
     * @throws IllegalArgumentException If type is null
     */
    public static Bounds create(Type type, float[] bounds) {
        if (type == null) {
            throw new IllegalAccessError("Type is null");
        }
        switch (type) {
        case CIRCULAR:
            return new CircularBounds(bounds);
        case RECTANGULAR:
            return new RectangularBounds(bounds);
        default:
            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + type);
        }
    }

    /**
     * Performs a quick check if the object is outside the bounds, use this for quick on screen checks or similar.
     * 
     * @param bounds Array with x, y, width height
     * @param radius The radius of the object to check.
     * @return True if this object is touches (is inside) the bounds, false if it is fully outside.
     */
    public static boolean isCulled(float[] position, int[] bounds, float radius) {

        if (position[0] + radius < bounds[0] || position[0] - radius > bounds[0] + bounds[2]) {
            return true;
        }
        if (position[1] + radius < bounds[1] || position[1] - radius > bounds[1] + bounds[3]) {
            return true;
        }
        return false;
    }

    /**
     * Sets the bounds from the values, what the values means is implementation specific.
     * 
     * @param values Values that match the data needed for the implementing bounds class.
     */
    public abstract void setBounds(float[] values);

    /**
     * Sets the implementing bounds to match the rectangle
     * 
     * @param rectangle
     */
    public abstract void setBounds(Rectangle rectangle);

}
