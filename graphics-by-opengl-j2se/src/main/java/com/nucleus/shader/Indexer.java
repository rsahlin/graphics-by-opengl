package com.nucleus.shader;

import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.ShaderProgram;

/**
 * Holds the runtime offset into attribute/uniform in shader programs.
 * These are generalised names for common usecases, offset is fetched from program.
 * Subclasses can override constructor and fetch offset by using name of attribute.
 * 
 */
@Deprecated
public class Indexer {
    public final int vertex;
    public final int uv;
    public final int translate;
    public final int rotate;
    public final int scale;
    public final int frame;
    public final int albedo;
    public final int emissive;
    public final int attributesPerVertex;

    /**
     * Enum of properties that may be declared in shaders, this is to allow fetching known properties without
     * knowing the exact shader source variable name.
     *
     */
    public enum Property {
        VERTEX("aVertex"),
        UV("aTexCoord"),
        TRANSLATE("aTranslate"),
        ROTATE("aRotate"),
        SCALE("aScale"),
        ALBEDO("aColor"),
        EMISSIVE("aColor"),
        FRAME("aFrameData");

        public final String name;

        private Property(String name) {
            this.name = name;
        }

    }

    /**
     * Creates the attributer index mapping for the properties with a specific shader program.
     * These indexes are generalised variable names, some may not be included in shader and will be -1
     * To write to attribute data without using indexer, fetch attribute offset by variable name - OR
     * create a VariableIndexer and pass to program when created.
     * 
     * @param program
     */
    public Indexer(ShaderProgram program) {
        vertex = program.getAttributeOffset(Property.VERTEX.name);
        uv = program.getAttributeOffset(Property.UV.name);
        translate = program.getAttributeOffset(Property.TRANSLATE.name);
        rotate = program.getAttributeOffset(Property.ROTATE.name);
        scale = program.getAttributeOffset(Property.SCALE.name);
        frame = program.getAttributeOffset(Property.FRAME.name);
        albedo = program.getAttributeOffset(Property.ALBEDO.name);
        emissive = program.getAttributeOffset(Property.EMISSIVE.name);
        attributesPerVertex = program.getAttributesPerVertex(BufferIndex.ATTRIBUTES);
    }

    protected Indexer(Indexer source) {
        vertex = source.vertex;
        uv = source.uv;
        translate = source.translate;
        rotate = source.rotate;
        scale = source.scale;
        frame = source.frame;
        albedo = source.albedo;
        emissive = source.emissive;
        attributesPerVertex = source.attributesPerVertex;
    }

    /**
     * Internal contstructor
     * 
     * @param values
     */
    protected Indexer(int[] values) {
        vertex = values[0];
        uv = values[1];
        translate = values[2];
        rotate = values[3];
        scale = values[4];
        frame = values[5];
        albedo = values[6];
        emissive = values[7];
        attributesPerVertex = values[8];

    }

}
