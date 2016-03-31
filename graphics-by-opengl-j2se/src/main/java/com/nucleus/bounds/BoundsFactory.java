package com.nucleus.bounds;

import com.nucleus.Error;
import com.nucleus.bounds.Bounds.Type;

public class BoundsFactory {

    /**
     * Creates the specified bounds, this is used when deserializing.
     * 
     * @param type the type of bounds to create
     * @param bounds The bounds values
     * @return The implementation of the bounds
     */
    public static Bounds create(Type type, float[] bounds) {
        switch (type) {
            case CIRCULAR:
            return new CircularBounds(bounds);
        case RECTANGULAR:
            return new RectangularBounds(bounds, 0);
        default:
            throw new IllegalArgumentException(Error.NOT_IMPLEMENTED.message + type);
        }
    }

}
