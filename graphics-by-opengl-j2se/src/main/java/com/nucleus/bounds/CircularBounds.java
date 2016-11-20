package com.nucleus.bounds;


/**
 * 2 Dimensional circular bounds, the bounds is 3 values, x,y and radius.
 * 
 * @author Richard Sahlin
 *
 */
public class CircularBounds extends Bounds {

    /**
     * Index into bounds data where the radius is
     */
    public final static int X_INDEX = 0;
    public final static int Y_INDEX = 1;
    public final static int RADIUS_INDEX = 2;


    /**
     * Creates a new circular bounds, with the specified 2D position and radius.
     * 
     * @param values The bounds values. X,y and radius
     */
    public CircularBounds(float[] values) {
        create(values);
    }

    /**
     * Sets the bounds data with the radius, first creating the array and sets the type to CIRCULAR
     * 
     * @param values The bounds values. X,y and radius.
     * @throws NullPointerException If values is null
     */
    private void create(float[] values) {
        type = Type.CIRCULAR;
        bounds = new float[3];
        bounds[X_INDEX] = values[X_INDEX];
        bounds[Y_INDEX] = values[Y_INDEX];
        bounds[RADIUS_INDEX] = values[RADIUS_INDEX];
    }


    @Override
    public boolean isPointInside(float[] position, int index) {
        float xdist = bounds[X_INDEX] - position[index++];
        float ydist = bounds[Y_INDEX] - position[index];
        float dist = (float) Math.sqrt(xdist * xdist + ydist * ydist);
        if (dist < bounds[RADIUS_INDEX]) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isCircularInside(CircularBounds bounds) {

        int distX = (int) (bounds.bounds[X_INDEX] - this.bounds[X_INDEX]);
        int distY = (int) (bounds.bounds[Y_INDEX] - this.bounds[Y_INDEX]);
        if (Math.abs(distX) > this.bounds[RADIUS_INDEX] + bounds.bounds[RADIUS_INDEX]) {
            return false;
        }
        if (Math.abs(distY) > this.bounds[RADIUS_INDEX] + bounds.bounds[RADIUS_INDEX]) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRectangleInside(RectangularBounds bounds) {
        return bounds.isCircularInside(this);
    }

    @Override
    public void rotate(int axis, float degrees) {
        //Rotation of the circular bounds have no meaning.
    }

    @Override
    public void transform(float[] matrix, int index) {
        // TODO Implement scale and translate

    }

}
