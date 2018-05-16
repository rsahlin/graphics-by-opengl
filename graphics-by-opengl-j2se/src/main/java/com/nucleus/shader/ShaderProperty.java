package com.nucleus.shader;

import com.nucleus.geometry.Mesh.BufferIndex;

/**
 * Connecting variables in shader programs with offsets so that data can be written.
 *
 */
public class ShaderProperty {

    /**
     * Enum of properties that may be declared in shaders, this is to allow fetching known properties without
     * knowing the exact shader source variable name.
     *
     */
    public enum Property {
        TRANSLATE(),
        ROTATE(),
        SCALE(),
        ALBEDO(),
        EMISSIVE(),
        FRAME();
    }

    /**
     * Holds the runtime offset into attribute/property in shader programs.
     * 
     * @author Richard Sahlin
     *
     */
    public static class PropertyMapper {
        public int translateOffset;
        public int rotateOffset;
        public int scaleOffset;
        public int frameOffset;
        public int albedoOffset;
        public int emissiveOffset;
        public int attributesPerVertex;

        /**
         * Creates the attributer index mapping for the properties with a specific shader program.
         * 
         * @param program
         */
        public PropertyMapper(ShaderProgram program) {
            translateOffset = program.getPropertyOffset(Property.TRANSLATE);
            rotateOffset = program.getPropertyOffset(Property.ROTATE);
            scaleOffset = program.getPropertyOffset(Property.SCALE);
            frameOffset = program.getPropertyOffset(Property.FRAME);
            albedoOffset = program.getPropertyOffset(Property.ALBEDO);
            emissiveOffset = program.getPropertyOffset(Property.EMISSIVE);
            attributesPerVertex = program.getAttributesPerVertex(BufferIndex.ATTRIBUTES);
        }

        protected PropertyMapper(PropertyMapper source) {
            translateOffset = source.translateOffset;
            rotateOffset = source.rotateOffset;
            scaleOffset = source.scaleOffset;
            frameOffset = source.frameOffset;
            albedoOffset = source.albedoOffset;
            emissiveOffset = source.emissiveOffset;
            attributesPerVertex = source.attributesPerVertex;

        }

    }

}
