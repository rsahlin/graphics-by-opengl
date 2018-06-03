package com.nucleus.shader;

import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * Connecting variables in shader programs with offsets so that data can be written into buffer that stores the
 * attributes. This can be seen as a type of layout where the purpose is to connect the fields defined in a shader
 * with the usage.
 * 
 *
 */
public class VariableIndexer {

    /**
     * Returns the offset, within the buffer specified by {@link #getBufferIndex()}, for data to the variable.
     * 
     * @return
     */
    public int getOffset(int index) {
        return offsets[index];
    }

    /**
     * Returns the type of variable
     * 
     * @return Type of variable
     */
    public VariableType getType(int index) {
        return types[index];
    }

    /**
     * Returns the buffer index in the mesh.
     * This value can be used to call {@link Mesh#getAttributeBuffer(BufferIndex)}
     * 
     * @param BufferIndex Index to buffer holding variables
     */
    public BufferIndex getBufferIndex(int index) {
        return bufferIndexes[index];
    }

    /**
     * Returns the name of the variable - this is the name as defined it shall be defined in shader program.
     * 
     * @return The name of the variable at index
     */
    public String getName(int index) {
        return names[index];
    }

    protected String[] names;
    protected int[] offsets;
    protected VariableType[] types;
    protected BufferIndex[] bufferIndexes;

    public VariableIndexer(String[] names, int[] offsets, VariableType[] types, BufferIndex[] bufferIndexes) {
        if (names == null || offsets == null || types == null || bufferIndexes == null) {
            throw new IllegalArgumentException(
                    "Null argument in constructor " + names + ", " + offsets + ", " + types + ", " + bufferIndexes);
        }
        this.names = names;
        this.offsets = offsets;
        this.types = types;
        this.bufferIndexes = bufferIndexes;
    }

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

    }

}
