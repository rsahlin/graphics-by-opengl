package com.nucleus.opengl.shader;

import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * Program for rendering lines and similar.
 *
 */
public class LineProgram extends GLShaderProgram {

    public static class LineProgramIndexer extends NamedVariableIndexer {

        protected final static Property[] PROPERTY = new Property[] { Property.VERTEX,
                Property.EMISSIVE };
        protected final static int[] OFFSETS = new int[] { 0, 3 };
        protected final static VariableType[] TYPES = new VariableType[] { VariableType.ATTRIBUTE,
                VariableType.ATTRIBUTE };
        protected final static BufferIndex[] BUFFERINDEXES = new BufferIndex[] { BufferIndex.ATTRIBUTES,
                BufferIndex.ATTRIBUTES };

        public LineProgramIndexer() {
            super();
            createArrays(PROPERTY, OFFSETS, TYPES, new int[] { 7 }, BUFFERINDEXES);
        }

    }

    public static final String CATEGORY = "line";

    public LineProgram(Shading shading) {
        super(null, shading, CATEGORY, GLShaderProgram.ProgramType.VERTEX_FRAGMENT);
        setIndexer(new LineProgramIndexer());
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
    }

}
