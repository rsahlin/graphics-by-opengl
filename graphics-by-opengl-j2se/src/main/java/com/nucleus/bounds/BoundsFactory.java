package com.nucleus.bounds;

import com.nucleus.ErrorMessage;
import com.nucleus.bounds.Bounds.Type;

public class BoundsFactory {

    /**
     * Creates the specified bounds, this is used when deserializing.
     * 
     * @param type the type of bounds to create
     * @param bounds The bounds values
     * @return The implementation of the bounds
     * @throws IllegalArgumentException If bounds or type is null
     */
    public static Bounds create(Type type, float[] bounds) {
        if (type == null || bounds == null) {
            throw new IllegalAccessError("Parameter is null " + type + ", " + bounds);
        }
        switch (type) {
            case CIRCULAR:
            return new CircularBounds(bounds);
        case RECTANGULAR:
            return new RectangularBounds(bounds, 0);
        default:
            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + type);
        }
    }

}
