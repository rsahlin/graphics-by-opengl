package com.nucleus.opengl.shader;

import com.nucleus.assets.Assets;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.shader.GenericShaderProgram;
import com.nucleus.shader.Shader;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureType;

/**
 * Program for translated vertices, shader calculates vertex position with position offset
 * Can be used to draw objects that cannot be independently rotated or scaled, for instance a quad, but it can
 * be positioned using the translate variable.
 */
public class TranslateProgram extends GenericShaderProgram {

    public static class TranslateProgramIndexer extends NamedVariableIndexer {
        protected final static Property[] PROPERTY = new Property[] { Property.VERTEX,
                Property.UV, Property.TRANSLATE,
                Property.ALBEDO };
        protected final static int[] OFFSETS = new int[] { 0, 3, 0, 6 };
        protected final static VariableType[] TYPES = new VariableType[] { VariableType.ATTRIBUTE,
                VariableType.ATTRIBUTE, VariableType.ATTRIBUTE, VariableType.ATTRIBUTE };
        protected final static BufferIndex[] BUFFERINDEXES = new BufferIndex[] { BufferIndex.ATTRIBUTES_STATIC,
                BufferIndex.ATTRIBUTES_STATIC, BufferIndex.ATTRIBUTES, BufferIndex.ATTRIBUTES };

        private TranslateProgramIndexer() {
            super();
            createArrays(PROPERTY, OFFSETS, TYPES, new int[] { 7, 5 }, BUFFERINDEXES);
        }
    }

    /**
     * Constructor for shader program - the program will be empty - no attached shader source, no shader
     * compile or linked.
     * Shall only be used to create a placeholder program that can be used to create a pipeline:
     * call {@link Assets#getGraphicsPipeline(NucleusRenderer, com.nucleus.shader.Shader)}
     * 
     * @param texture
     */
    public TranslateProgram(Texture2D texture) {
        init(new SharedfragmentCategorizer(null,
                (texture == null ||
                        texture.textureType == TextureType.Untextured) ? Shading.flat : Shading.textured,
                "translate"), Shader.ProgramType.VERTEX_FRAGMENT);
        setIndexer(new TranslateProgramIndexer());
    }

    /**
     * Constructor for shader program - the program will be empty - no attached shader source, no shader
     * compile or linked.
     * Shall only be used to create a placeholder program that can be used to create a pipeline:
     * call {@link Assets#getGraphicsPipeline(NucleusRenderer, com.nucleus.shader.Shader)}
     * 
     * @param shading
     */
    public TranslateProgram(Shading shading) {
        init(new SharedfragmentCategorizer(null, shading, "translate"), Shader.ProgramType.VERTEX_FRAGMENT);
        setIndexer(new TranslateProgramIndexer());
    }

    @Override
    public void updateUniformData() {
    }

    @Override
    public void initUniformData() {
    }

}
