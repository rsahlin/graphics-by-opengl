package com.nucleus.bounds;

import static com.nucleus.vecmath.VecMath.X;
import static com.nucleus.vecmath.VecMath.Y;
import static com.nucleus.vecmath.VecMath.Z;

import java.util.LinkedList;

import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Vector2D;
import com.nucleus.vecmath.Vector3D;

/**
 * 2 Dimensional rectangle bounds, the bounds can be rotated and can have a position array that it follows.
 * The bounds values are the 4 corners of the rectangle
 * 
 * @author Richard Sahlin
 *
 */
public class RectangularBounds extends Bounds {

    /**
     * Number of data values for this bounds.
     */
    public final static int BOUNDS_LENGTH = 8;

    public final static int X1 = 0;
    public final static int Y1 = 1;
    public final static int X2 = 2;
    public final static int Y2 = 3;
    public final static int X3 = 4;
    public final static int Y3 = 5;
    public final static int X4 = 6;
    public final static int Y4 = 7;

    transient protected float[] collideVector1 = new float[2];
    transient protected float[] collideVector2D = new float[2];
    transient protected float[] vector1 = new float[2];
    transient protected float[] vector2D = new float[2];
    transient protected float[] vector3D = new float[2];
    transient protected float[] vector4 = new float[2];
    /**
     * Store result position + rotated bounding here
     */
    transient protected float[] resultPositions = new float[8];

    transient protected float[] tempPositons = new float[8];
    transient protected float[] rotatedBounds = new float[8];

    /**
     * Creates a new bounds from the specified upper left corner (x, y) and size
     * 
     * @param x Upper left
     * @param y Upper left
     * @param width Width of bounds
     * @param height Height of bounds
     */
    public RectangularBounds(float x, float y, float width, float height) {
        type = Type.RECTANGULAR;
        setBounds(x, y, width, height);
    }

    /**
     * Creates a new bounds from an array of values.
     * If the array contains 4 values, bounds will be created from upper left corner (x1,y1) and width + height
     * If not, the array must contain 8 values, X+Y for each corner in a clockwise manner from upper left.
     */
    public RectangularBounds(float[] values, int index) {
        // TODO How to handle if bounds do not use position reference?
        position = new float[2];
        type = Type.RECTANGULAR;
        if (values == null || values.length == 0) {
            // Take bounds from rectangle
        } else if (values.length == 4) {
            setBounds(values[0], values[1], values[2], values[3]);
        } else {
            setBounds(values, index);
        }
    }

    /**
     * Internal method, calculate the quick bounds radius for culling check.
     * Call this whenever the bounds values are set or changed.
     */
    private final float calculateRadius() {
        float x1 = bounds[X1];
        if (bounds[X4] < x1) {
            x1 = bounds[X4];
        }
        float x2 = bounds[X2];
        if (bounds[X3] > x2) {
            x2 = bounds[X3];
        }
        float y1 = bounds[Y1];
        if (bounds[Y2] < y1) {
            y1 = bounds[Y2];
        }
        float y2 = bounds[Y3];
        if (bounds[Y4] > y2) {
            y2 = bounds[Y4];
        }

        int xdist = (int) ((x2 - x1) + 0.5f);
        int ydist = (int) ((y2 - y1) + 0.5f);
        return (float) Math.sqrt(xdist * xdist + ydist * ydist);
    }

    /**
     * Set the bounds from the specified data, corner 1 is upper left then going clockwise, ending with
     * lower left corner.
     * 
     * @param bounds The bounds values, must contain 8 values at index
     * @param index Index into bounds array where values are
     */
    public void setBounds(float[] bounds, int index) {
        if (this.bounds == null) {
            this.bounds = new float[BOUNDS_LENGTH];
        }
        System.arraycopy(bounds, 0, this.bounds, 0, BOUNDS_LENGTH);
        System.arraycopy(this.bounds, 0, rotatedBounds, 0, BOUNDS_LENGTH);
        updated = true;
        calculateRadius();
    }

    /**
     * Sets the bounds from upper left corner position, width and height
     * 
     * @param x1
     * @param y1
     * @param width
     * @param height
     */
    public void setBounds(float x1, float y1, float width, float height) {
        if (bounds == null) {
            bounds = new float[BOUNDS_LENGTH];
        }
        bounds[X1] = x1;
        bounds[Y1] = y1;
        bounds[X2] = x1 + width;
        bounds[Y2] = y1;
        bounds[X3] = x1 + width;
        bounds[Y3] = y1 + height;
        bounds[X4] = x1;
        bounds[Y4] = y1 + height;
        System.arraycopy(bounds, 0, rotatedBounds, 0, BOUNDS_LENGTH);
        updated = true;
        calculateRadius();
    }


    @Override
    public boolean isPointInside(float[] position, int index) {

        calculateVectors();
        collideVector1[0] = position[index] - resultPositions[0];
        collideVector1[1] = position[index + 1] - resultPositions[1];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, vector1) < 0) {
            return false;
        }
        if (Vector2D.dot2D(collideVector1, vector2D) > 0) {
            return false;
        }
        collideVector1[0] = position[index] - resultPositions[4];
        collideVector1[1] = position[index + 1] - resultPositions[5];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, vector3D) > 0) {
            return false;
        }
        if (Vector2D.dot2D(collideVector1, vector4) < 0) {
            return false;
        }
        return true;
    }


    /**
     * Internal method to calculate the vectors in the rectangular bounds.
     * This needs to be done when the position reference has changed or when the bounds itself has changed.
     */
    protected void calculateVectors() {
        if (!updated) {
            return;
        }
        getPositions(resultPositions, 0);
        //Rotate along z axis to get cross
        vector1[1] = (position[0] + rotatedBounds[2] - resultPositions[0]);
        vector1[0] = -(position[1] + rotatedBounds[3] - resultPositions[1]);
        Vector3D.normalize2D(vector1, 0);

        vector2D[1] = (position[0] + rotatedBounds[6] - resultPositions[0]);
        vector2D[0] = -(position[1] + rotatedBounds[7] - resultPositions[1]);
        Vector3D.normalize2D(vector2D, 0);

        vector3D[1] = (position[0] + rotatedBounds[2] - resultPositions[4]);
        vector3D[0] = -(position[1] + rotatedBounds[3] - resultPositions[5]);
        Vector3D.normalize2D(vector3D, 0);

        vector4[1] = (position[0] + rotatedBounds[6] - resultPositions[4]);
        vector4[0] = -(position[1] + rotatedBounds[7] - resultPositions[5]);
        Vector3D.normalize2D(vector4, 0);
        updated = false;
    }

    @Override
    public boolean isCircularInside(CircularBounds bounds) {
        calculateVectors();
        float[] position = bounds.position;

        float radius = bounds.bounds[CircularBounds.RADIUS_INDEX];
        collideVector1[0] = (position[0] + radius  * vector1[0]) - resultPositions[0];
        collideVector1[1] = (position[1] + radius * vector1[1]) - resultPositions[1];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, vector1) < 0) {
            return false;
        }
        collideVector1[0] = (position[0] - radius * vector2D[0]) - resultPositions[0];
        collideVector1[1] = (position[1] - radius * vector2D[1]) - resultPositions[1];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, vector2D) > 0) {
            return false;
        }
        collideVector1[0] = (position[0] - radius * vector3D[0]) - resultPositions[4];
        collideVector1[1] = (position[1] - radius * vector3D[1]) - resultPositions[5];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, vector3D) > 0) {
            return false;
        }
        collideVector1[0] = (position[0] + radius  * vector4[0]) - resultPositions[4];
        collideVector1[1] = (position[1] + radius * vector4[1]) - resultPositions[5];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, vector4) < 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRectangleInside(RectangularBounds bounds) {
        calculateVectors();
        int index = 0;
        while (index < BOUNDS_LENGTH) {
            if (bounds.isPointInside(resultPositions, index)) {
                return true;
            }
            index += 2;
        }
        index = 0;
        bounds.getPositions(tempPositons, 0);
        while (index < BOUNDS_LENGTH) {
            if (isPointInside(tempPositons, index)) {
                return true;
            }
            index += 2;
        }

        return false;
    }

    @Override
    public void rotate(int axis, float angle) {
        switch (axis) {
        case X:
        case Y:
        break;
        case Z:
            Vector2D.rotateZAxis(bounds, 0, rotatedBounds, 0, angle);
            Vector2D.rotateZAxis(bounds, 2, rotatedBounds, 2, angle);
            Vector2D.rotateZAxis(bounds, 4, rotatedBounds, 4, angle);
            Vector2D.rotateZAxis(bounds, 6, rotatedBounds, 6, angle);
            updated = true;
        break;
        }

    }

    /**
     * Fetches the 4 positions of the corners for the rotated boundingbox, this is the position of this object plus
     * the rotated boundingbox.
     * @param destination The 4 corners are stored here, rotated rectangle plus bound position.
     * @param index Index into array where data is stored, must be able to store 4 x,y values (8)
     */
    protected void getPositions(float[] destination, int index) {
        destination[index++] = position[0] + rotatedBounds[X1];
        destination[index++] = position[1] + rotatedBounds[Y1];
        destination[index++] = position[0] + rotatedBounds[X2];
        destination[index++] = position[1] + rotatedBounds[Y2];
        destination[index++] = position[0] + rotatedBounds[X3];
        destination[index++] = position[1] + rotatedBounds[Y3];
        destination[index++] = position[0] + rotatedBounds[X4];
        destination[index++] = position[1] + rotatedBounds[Y4];
    }

    /**
     * Creates one rectangular bounds to cover the bounds in the list.
     * 
     * @param source List with Bounds that the result bounds shall cover.
     * Currently only supports Circular bounds in the list.
     * @return
     */
    public static RectangularBounds createBounds(LinkedList<Bounds> source) {

        float x1 = 1000;
        float y1 = 1000;
        float x2 = -1000;
        float y2 = -1000;

        float[] pos = null;
        while (source.size() > 0) {
            Bounds b = source.remove();
            pos = b.position;
            switch (b.type) {

            case RECTANGULAR:
                    //Don't use rotated bounds.
                    RectangularBounds r = (RectangularBounds) b;
                if (pos[X] - r.bounds[RectangularBounds.X1] < x1) {
                    x1 = pos[X] - r.bounds[RectangularBounds.X1];
                    }
                if (pos[Y] - r.bounds[RectangularBounds.Y1] < y1) {
                    y1 = pos[Z] - r.bounds[RectangularBounds.Y1];
                    }
                if (pos[X] + r.bounds[RectangularBounds.X2] > x2) {
                    x2 = pos[X] + r.bounds[RectangularBounds.X2];
                    }
                if (pos[Y] + r.bounds[RectangularBounds.Y3] > y2) {
                    y2 = pos[Y] + r.bounds[RectangularBounds.Y3];
                    }

                break;
            case CIRCULAR:
                    CircularBounds bounds = (CircularBounds) b;
                if (pos[X] - bounds.bounds[CircularBounds.RADIUS_INDEX] < x1) {
                    x1 = pos[X] - bounds.bounds[CircularBounds.RADIUS_INDEX];
                    }
                if (pos[Y] - bounds.bounds[CircularBounds.RADIUS_INDEX] < y1) {
                    y1 = pos[Y] - bounds.bounds[CircularBounds.RADIUS_INDEX];
                    }
                if (pos[X] + bounds.bounds[CircularBounds.RADIUS_INDEX] > x2) {
                    x2 = pos[X] + bounds.bounds[CircularBounds.RADIUS_INDEX];
                    }
                if (pos[Y] + bounds.bounds[CircularBounds.RADIUS_INDEX] > y2) {
                    y2 = pos[Y] + bounds.bounds[CircularBounds.RADIUS_INDEX];
                    }
                break;
            }
        }

        return new RectangularBounds(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public void transform(float[] matrix, int index) {
        Matrix.transformVec2(matrix, index, bounds, rotatedBounds, 4);
        updated = true;
    }

}
