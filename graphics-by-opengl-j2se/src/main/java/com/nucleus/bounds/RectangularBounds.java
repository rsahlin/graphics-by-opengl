package com.nucleus.bounds;

import static com.nucleus.vecmath.VecMath.X;
import static com.nucleus.vecmath.VecMath.Y;
import static com.nucleus.vecmath.VecMath.Z;

import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Rectangle;
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

    /**
     * TODO Move to one array
     */
    transient protected float[] top = new float[2];
    transient protected float[] left = new float[2];
    transient protected float[] right = new float[2];
    transient protected float[] bottom = new float[2];

    transient protected float[] tempPositons = new float[8];
    transient protected float[] rotatedBounds = new float[8];

    /**
     * Creates a new bounds from an array of values.
     * If the array contains 4 values, bounds will be created from upper left corner (x1,y1) and width + height
     * If not, the array must contain 8 values, X+Y for each corner in a clockwise manner from upper left.
     * 
     * @param values The bounds source values, either 4 values for corner + width, height or 8 values.
     * May be null to flag that values should be calculated later
     * @throws NullPointerException If values is null
     * @throws ArrayIndexOutOfBoundsException If values does not contain index + 4 or 8 values as needed
     */
    RectangularBounds(float[] values) {
        create(values);
    }

    private void create(float[] values) {
        type = Type.RECTANGULAR;
        if (values != null) {
            setBounds(values);
        }
    }

    /**
     * Creates rectangulare bounds from a rectangle
     * 
     * @param rect
     */
    public RectangularBounds(Rectangle rect) {
        create(rect.getValues());
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
    private void copyBounds(float[] bounds, int index) {
        createBounds();
        System.arraycopy(bounds, 0, this.bounds, 0, BOUNDS_LENGTH);
        System.arraycopy(this.bounds, 0, rotatedBounds, 0, BOUNDS_LENGTH);
        updated = true;
        calculateRadius();
    }

    @Override
    public void setBounds(float[] values) {
        if (values.length == 4) {
            setFromRectangle(values);
        } else {
            copyBounds(values, 0);
        }
    }

    @Override
    public void setBounds(Rectangle rectangle) {
        setFromRectangle(rectangle.getValues());
    }

    private void setFromRectangle(float[] rectangle) {
        createBounds();
        float x = rectangle[Rectangle.X];
        float y = rectangle[Rectangle.Y];
        float width = rectangle[Rectangle.WIDTH];
        float height = rectangle[Rectangle.HEIGHT];
        bounds[X1] = x;
        bounds[Y1] = y;
        bounds[X2] = x + width;
        bounds[Y2] = y;
        bounds[X3] = x + width;
        bounds[Y3] = y - height;
        bounds[X4] = x;
        bounds[Y4] = y - height;
        System.arraycopy(bounds, 0, rotatedBounds, 0, BOUNDS_LENGTH);
        updated = true;
        calculateRadius();
    }

    /**
     * Creates the bounds array if null
     */
    private void createBounds() {
        if (this.bounds == null) {
            this.bounds = new float[BOUNDS_LENGTH];
        }

    }

    @Override
    public boolean isPointInside(float[] position, int index) {

        calculateVectors();
        collideVector1[0] = position[index] - rotatedBounds[0];
        collideVector1[1] = position[index + 1] - rotatedBounds[1];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, top) < 0) {
            return false;
        }
        if (Vector2D.dot2D(collideVector1, left) < 0) {
            return false;
        }
        collideVector1[0] = position[index] - rotatedBounds[4];
        collideVector1[1] = position[index + 1] - rotatedBounds[5];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, right) < 0) {
            return false;
        }
        if (Vector2D.dot2D(collideVector1, bottom) < 0) {
            return false;
        }
        return true;
    }


    /**
     * Internal method to calculate the vectors in the rectangular bounds.
     * This needs to be done when the position reference has changed or when the bounds itself has
     * changed/rotated/transformed
     */
    protected void calculateVectors() {
        if (!updated) {
            return;
        }
        // getPositions(resultPositions, 0);
        // TOP
        top[0] = (rotatedBounds[2] - rotatedBounds[0]);
        top[1] = (rotatedBounds[3] - rotatedBounds[1]);
        Vector3D.normalize2D(top, 0);

        // LEFT
        left[0] = (rotatedBounds[6] - rotatedBounds[0]);
        left[1] = (rotatedBounds[7] - rotatedBounds[1]);
        Vector3D.normalize2D(left, 0);

        // RIGHT
        right[0] = (rotatedBounds[2] - rotatedBounds[4]);
        right[1] = (rotatedBounds[3] - rotatedBounds[5]);
        Vector3D.normalize2D(right, 0);

        // BOTTOM
        bottom[0] = (rotatedBounds[6] - rotatedBounds[4]);
        bottom[1] = (rotatedBounds[7] - rotatedBounds[5]);
        Vector3D.normalize2D(bottom, 0);
        updated = false;
    }

    @Override
    public boolean isCircularInside(CircularBounds bounds) {
        calculateVectors();

        float radius = bounds.bounds[CircularBounds.RADIUS_INDEX];
        collideVector1[0] = (radius * top[0]) - rotatedBounds[0];
        collideVector1[1] = (radius * top[1]) - rotatedBounds[1];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, top) < 0) {
            return false;
        }
        collideVector1[0] = (radius * left[0]) - rotatedBounds[0];
        collideVector1[1] = (radius * left[1]) - rotatedBounds[1];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, left) > 0) {
            return false;
        }
        collideVector1[0] = (radius * right[0]) - rotatedBounds[4];
        collideVector1[1] = (radius * right[1]) - rotatedBounds[5];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, right) > 0) {
            return false;
        }
        collideVector1[0] = (radius * bottom[0]) - rotatedBounds[4];
        collideVector1[1] = (radius * bottom[1]) - rotatedBounds[5];
        Vector3D.normalize2D(collideVector1, 0);
        if (Vector2D.dot2D(collideVector1, bottom) < 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRectangleInside(RectangularBounds bounds) {
        calculateVectors();
        int index = 0;
        while (index < BOUNDS_LENGTH) {
            if (bounds.isPointInside(rotatedBounds, index)) {
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
        destination[index++] = rotatedBounds[X1];
        destination[index++] = rotatedBounds[Y1];
        destination[index++] = rotatedBounds[X2];
        destination[index++] = rotatedBounds[Y2];
        destination[index++] = rotatedBounds[X3];
        destination[index++] = rotatedBounds[Y3];
        destination[index++] = rotatedBounds[X4];
        destination[index++] = rotatedBounds[Y4];
    }

    /**
     * Creates one rectangular bounds to cover the bounds in the list.
     * 
     * @param source List with Bounds that the result bounds shall cover.
     * Currently only supports Circular bounds in the list.
     * @return
     */
    /*
     * public static RectangularBounds createBounds(LinkedList<Bounds> source) {
     * 
     * float x1 = 1000;
     * float y1 = 1000;
     * float x2 = -1000;
     * float y2 = -1000;
     * 
     * float[] pos = null;
     * while (source.size() > 0) {
     * Bounds b = source.remove();
     * pos = b.position;
     * switch (b.type) {
     * 
     * case RECTANGULAR:
     * //Don't use rotated bounds.
     * RectangularBounds r = (RectangularBounds) b;
     * if (pos[X] - r.bounds[RectangularBounds.X1] < x1) {
     * x1 = pos[X] - r.bounds[RectangularBounds.X1];
     * }
     * if (pos[Y] - r.bounds[RectangularBounds.Y1] < y1) {
     * y1 = pos[Z] - r.bounds[RectangularBounds.Y1];
     * }
     * if (pos[X] + r.bounds[RectangularBounds.X2] > x2) {
     * x2 = pos[X] + r.bounds[RectangularBounds.X2];
     * }
     * if (pos[Y] + r.bounds[RectangularBounds.Y3] > y2) {
     * y2 = pos[Y] + r.bounds[RectangularBounds.Y3];
     * }
     * 
     * break;
     * case CIRCULAR:
     * CircularBounds bounds = (CircularBounds) b;
     * if (pos[X] - bounds.bounds[CircularBounds.RADIUS_INDEX] < x1) {
     * x1 = pos[X] - bounds.bounds[CircularBounds.RADIUS_INDEX];
     * }
     * if (pos[Y] - bounds.bounds[CircularBounds.RADIUS_INDEX] < y1) {
     * y1 = pos[Y] - bounds.bounds[CircularBounds.RADIUS_INDEX];
     * }
     * if (pos[X] + bounds.bounds[CircularBounds.RADIUS_INDEX] > x2) {
     * x2 = pos[X] + bounds.bounds[CircularBounds.RADIUS_INDEX];
     * }
     * if (pos[Y] + bounds.bounds[CircularBounds.RADIUS_INDEX] > y2) {
     * y2 = pos[Y] + bounds.bounds[CircularBounds.RADIUS_INDEX];
     * }
     * break;
     * }
     * }
     * 
     * return new RectangularBounds(x1, y1, x2 - x1, y2 - y1);
     * }
     */
    @Override
    public void transform(float[] matrix, int index) {
        Matrix.transformVec2(matrix, index, bounds, rotatedBounds, 4);
        updated = true;
    }



}
