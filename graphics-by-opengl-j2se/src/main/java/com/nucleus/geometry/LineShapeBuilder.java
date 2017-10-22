package com.nucleus.geometry;

/**
 * Builder for line shapes - this builder will not set vertice data since lines are primitives.
 * Element buffer will be built, lines are created by setting vertice positions.
 *
 */
public class LineShapeBuilder extends ElementBuilder {

    public LineShapeBuilder(int lineCount, int startVertex) {
        configuration = new Configuration(lineCount * 2, startVertex);
    }

    private Configuration configuration;

    @Override
    public void build(Mesh mesh) {
        buildElements(mesh, configuration.vertexCount >>> 1, configuration.startVertex);
    }

    /**
     * Builds the element buffer if present in the mesh
     * 
     * @param mesh
     * @param count Number of lines to build element indices for
     * @param startVertex First vertex to index
     */
    @Override
    public void buildElements(Mesh mesh, int count, int startVertex) {
        // Check if indicebuffer shall be built
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices != null) {
            switch (mesh.getMode()) {
                case LINES:
                    buildLinesBuffer(indices, count, startVertex);
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented for " + mesh.getMode());
            }
        }
    }

}
