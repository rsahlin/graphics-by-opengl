package com.nucleus.bounds;


/**
 * 2 Dimensional circular bounds, the bounds is one value containing the radius.
 * 
 * @author Richard Sahlin
 *
 */
public class CircularBounds extends Bounds {

    /**
     * Index into bounds data where the radius is
     */
    public final static int RADIUS_INDEX = 0;


    /**
     * Creates a new circular bounds, with the specified radius.
     * 
     * @param radius The radius of the bounds
     */
    public CircularBounds(float radius) {
        create(radius);
    }

    /**
     * Sets the bounds data with the radius, first creating the array and sets the type to CIRCULAR
     * 
     * @param radius
     */
    private void create(float radius) {
        bounds = new float[1];
        bounds[RADIUS_INDEX] = radius;
        type = Type.CIRCULAR;
    }


    @Override
    public boolean isPointInside(float[] position, int index) {
        float xdist = this.position[0] - position[index++];
        float ydist = this.position[1] - position[index];
        float dist = (float) Math.sqrt(xdist * xdist + ydist * ydist);
        if (dist < bounds[RADIUS_INDEX]) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isCircularInside(CircularBounds bounds) {

        int distX = (int) (bounds.position[0] - position[0]);
        int distY = (int) (bounds.position[1] - position[1]);
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


}
