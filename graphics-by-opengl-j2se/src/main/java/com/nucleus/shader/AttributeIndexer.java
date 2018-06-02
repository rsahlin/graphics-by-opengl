package com.nucleus.shader;

import com.nucleus.geometry.Mesh.BufferIndex;

/**
 * Connecting variables in shader programs with offsets so that data can be written into buffer that stores the
 * attributes.
 * This only works using one attribute buffer.
 *
 */
public class AttributeIndexer {

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
     * Holds the runtime offset into attribute/uniform in shader programs.
     * These are generalised names for common usecases, offset is fetched from program.
     * Subclasses can override constructor and fetch offset by using name of attribute.
     * 
     * TODO How to get the datatype from shader?
     */
    public static class Indexer {
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
         * Creates the attributer index mapping for the properties with a specific shader program.
         * 
         * @param program
         */
        public Indexer(ShaderProgram program) {
            vertex = program.getVariableOffset(Property.VERTEX);
            uv = program.getVariableOffset(Property.UV);
            translate = program.getVariableOffset(Property.TRANSLATE);
            rotate = program.getVariableOffset(Property.ROTATE);
            scale = program.getVariableOffset(Property.SCALE);
            frame = program.getVariableOffset(Property.FRAME);
            albedo = program.getVariableOffset(Property.ALBEDO);
            emissive = program.getVariableOffset(Property.EMISSIVE);
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

    }

}
