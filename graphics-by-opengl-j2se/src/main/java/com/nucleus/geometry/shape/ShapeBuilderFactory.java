package com.nucleus.geometry.shape;

import com.nucleus.geometry.shape.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.vecmath.Rectangle;
import com.nucleus.vecmath.Shape;

/**
 * Creates shape builders
 *
 */
public class ShapeBuilderFactory {

    /**
     * Generic shapebuilder create method, will create the correct builder instance based on the shape.
     * 
     * @param shape
     * @param count
     * @param startVertex
     * @return
     */
    public static ShapeBuilder createBuilder(Shape shape, int count, int startVertex) {
        switch (shape.getType()) {
            case rect:
                return createBuilder((Rectangle) shape, count, startVertex);
            default:
                throw new IllegalArgumentException("Not implemented for: " + shape.getType());

        }
    }

    public static ShapeBuilder createBuilder(Rectangle shape, int count, int startVertex) {
        RectangleConfiguration config = new RectangleShapeBuilder.RectangleConfiguration(shape,
                RectangleShapeBuilder.DEFAULT_Z, count, 0);
        return new RectangleShapeBuilder(config);
    }

}
