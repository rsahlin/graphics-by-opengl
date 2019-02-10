package com.nucleus.geometry.shape;

import com.nucleus.geometry.Mesh;

/**
 * Builder for line shapes - this builder will not set vertice data since lines are primitives.
 * Element buffer will be built, lines are created by setting vertice positions.
 *
 */
public class LineShapeBuilder extends ShapeBuilder<Mesh> {

    public LineShapeBuilder(int verticeCount, int startVertex) {
        configuration = new Configuration(verticeCount, startVertex);
    }

    private Configuration configuration;

    @Override
    public void build(Mesh mesh) {
    }

}
