package com.nucleus.geometry.shape;

import java.nio.ShortBuffer;

import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.opengl.GLESWrapper;
import com.nucleus.opengl.GLESWrapper.Mode;
import com.nucleus.texturing.Texture2D;

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
    public void build(AttributeBuffer attributes, Texture2D texture, ElementBuffer indices, GLESWrapper.Mode mode) {
        if (indices != null) {
            if (indices.type != Type.SHORT) {
                throw new IllegalArgumentException("Invalid type " + indices.type);
            }
            buildElements(indices.indices.asShortBuffer(), mode, configuration.vertexCount >>> 1, configuration.startVertex);
        }
    }

    @Override
    public void buildElements(ShortBuffer buffer, GLESWrapper.Mode mode, int count, int startVertex) {
        // Check if indicebuffer shall be built
        switch (mode) {
            case LINES:
                buildLinesBuffer(buffer, count, startVertex);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + mode);
        }
    }

}
