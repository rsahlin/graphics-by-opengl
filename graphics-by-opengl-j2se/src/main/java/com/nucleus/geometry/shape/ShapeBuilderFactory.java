package com.nucleus.geometry.shape;

import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.shape.RectangleShapeBuilder.RectangleConfiguration;
import com.nucleus.vecmath.Grid;
import com.nucleus.vecmath.Rectangle;
import com.nucleus.vecmath.Shape;

/**
 * Creates shape builders
 *
 */
public class ShapeBuilderFactory {

    private static final ShapeBuilderFactory factory = new ShapeBuilderFactory();

    public static ShapeBuilderFactory getInstance() {
        return factory;
    }

    /**
     * Generic shapebuilder create method, will create the correct builder instance based on the shape.
     * 
     * @param shape
     * @param count
     * @param startVertex
     * @return
     */
    public ShapeBuilder<Mesh> createBuilder(Shape shape, int count, int startVertex) {
        switch (shape.getType()) {
            case rect:
                return createBuilder((Rectangle) shape, count, startVertex);
            case grid:
                return createBuilder((Grid) shape, count, startVertex);
            default:
                throw new IllegalArgumentException("Not implemented for: " + shape.getType());

        }
    }

    private ShapeBuilder<Mesh> createBuilder(Rectangle shape, int count, int startVertex) {
        RectangleConfiguration config = new RectangleShapeBuilder.RectangleConfiguration(shape,
                RectangleShapeBuilder.DEFAULT_Z, count, 0);
        return new RectangleShapeBuilder(config);
    }

    private static ShapeBuilder<Mesh> createBuilder(Grid shape, int count, int startVertex) {
        return new LineShapeBuilder(count * shape.getXSegments() * shape.getYSegments(), startVertex);
    }

}
