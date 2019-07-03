package com.nucleus.opengl.shader;

import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.ShaderVariable.VariableType;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.Indexer;

/**
 * Program for rendering lines and similar.
 *
 */
public class LineProgram extends GLShaderProgram {

    public static class LineProgramIndexer extends VariableIndexer {

        protected final static String[] NAMES = new String[] { Indexer.Property.VERTEX.name,
                Indexer.Property.ALBEDO.name };
        protected final static int[] OFFSETS = new int[] { 0, 3 };
        protected final static VariableType[] TYPES = new VariableType[] { VariableType.ATTRIBUTE,
                VariableType.ATTRIBUTE };
        protected final static BufferIndex[] BUFFERINDEXES = new BufferIndex[] { BufferIndex.ATTRIBUTES,
                BufferIndex.ATTRIBUTES };
        protected final static int[] SIZEPERVERTEX = new int[] { 7 };

        public LineProgramIndexer() {
            super(NAMES, OFFSETS, TYPES, BUFFERINDEXES, SIZEPERVERTEX);
        }

    }

    public static final String CATEGORY = "line";

    public LineProgram(GLShaderProgram.Shading shading) {
        super(null, shading, CATEGORY, GLShaderProgram.ProgramType.VERTEX_FRAGMENT);
        setIndexer(new LineProgramIndexer());
    }

    @Override
    public GLShaderProgram getProgram(NucleusRenderer renderer, Pass pass, GLShaderProgram.Shading shading) {
        switch (pass) {
            case UNDEFINED:
            case ALL:
            case MAIN:
                return this;
            default:
                throw new IllegalArgumentException("Invalid pass " + pass);
        }
    }

    @Override
    public void updateUniformData(FloatBuffer destinationUniform) {
    }

    @Override
    public void initUniformData(FloatBuffer destinationUniforms) {
    }

}
