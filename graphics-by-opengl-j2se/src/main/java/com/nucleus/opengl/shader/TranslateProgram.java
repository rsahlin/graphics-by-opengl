package com.nucleus.opengl.shader;

import java.nio.FloatBuffer;

import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.ShaderVariable.VariableType;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.shader.Indexer;
import com.nucleus.shader.Shader.Shading;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;

/**
 * Program for translated vertices, shader calculates vertex position with position offset
 * Can be used to draw objects that cannot be independently rotated or scaled, for instance a quad, but it can
 * be positioned using the translate variable.
 */
public class TranslateProgram extends GLShaderProgram {

    public static class TranslateProgramIndexer extends VariableIndexer {
        protected final static String[] NAMES = new String[] { Indexer.Property.VERTEX.name,
                Indexer.Property.UV.name, Indexer.Property.TRANSLATE.name,
                Indexer.Property.ALBEDO.name };
        protected final static int[] OFFSETS = new int[] { 0, 3, 0, 6 };
        protected final static VariableType[] TYPES = new VariableType[] { VariableType.ATTRIBUTE,
                VariableType.ATTRIBUTE, VariableType.ATTRIBUTE, VariableType.ATTRIBUTE };
        protected final static BufferIndex[] BUFFERINDEXES = new BufferIndex[] { BufferIndex.ATTRIBUTES_STATIC,
                BufferIndex.ATTRIBUTES_STATIC, BufferIndex.ATTRIBUTES, BufferIndex.ATTRIBUTES };
        protected final static int[] SIZEPERVERTEX = new int[] { 7, 5 };

        public TranslateProgramIndexer() {
            super(NAMES, OFFSETS, TYPES, BUFFERINDEXES, SIZEPERVERTEX);
        }
    }

    public TranslateProgram(Texture2D texture) {
        super(new SharedfragmentCategorizer(null,
                (texture == null || texture.textureType == TextureType.Untextured) ? Shading.flat
                        : Shading.textured,
                "translate"), GLShaderProgram.ProgramType.VERTEX_FRAGMENT);
        setIndexer(new TranslateProgramIndexer());
    }

    public TranslateProgram(Shading shading) {
        super(new SharedfragmentCategorizer(null, shading, "translate"), GLShaderProgram.ProgramType.VERTEX_FRAGMENT);
        setIndexer(new TranslateProgramIndexer());
    }

    @Override
    public GLShaderProgram getProgram(NucleusRenderer renderer, Pass pass, Shading shading) {
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
