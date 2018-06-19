package com.nucleus.shader;

import java.util.ArrayList;

import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.Mesh.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * Connecting variables in shader programs with offsets so that data can be written into buffer that stores the
 * attributes. This can be seen as a type of layout where the purpose is to connect the fields defined in a shader
 * with the usage.
 * Case be set to {@link ShaderProgram} by calling {@link ShaderProgram#setIndexer(VariableIndexer)} to set attribute
 * offsets.
 *
 */
public class VariableIndexer {

    /**
     * Returns the index of the named variable, or -1 if not defined
     * 
     * @param name
     * @return
     */
    public int getIndexByName(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].contentEquals(name)) {
                return i;
            }
        }
        return -1;
    }

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

    /**
     * Returns the size (in floats) per vertex that the buffer shall have - may be larger than actual usage.
     * 
     * @param index
     * @return
     */
    public int getSizePerVertex(int index) {
        return sizePerVertex[index];
    }

    /**
     * Sort the variables belonging to the specified buffer index, putting them in the
     * 
     * @param variables
     * @param index
     * @return
     */
    public ShaderVariable[] sortByBuffer(ShaderVariable[] activeVariables, int index) {
        ArrayList<ShaderVariable> result = new ArrayList<>();
        for (ShaderVariable v : activeVariables) {
            if (getBufferIndex(getIndexByName(v.getName())).index == index) {
                result.add(v);
            }
        }
        ShaderVariable[] array = new ShaderVariable[result.size()];
        return result.toArray(array);
    }

    protected String[] names;
    protected int[] offsets;
    protected VariableType[] types;
    protected BufferIndex[] bufferIndexes;
    protected int[] sizePerVertex;

    /**
     * 
     * @param names
     * @param offsets
     * @param types
     * @param bufferIndexes
     * @param sizePerVertex Size to allocate for each vertex, in floats, for each attribute buffer. If only one buffer
     * is used this array shall only have one entry.
     */
    public VariableIndexer(String[] names, int[] offsets, VariableType[] types, BufferIndex[] bufferIndexes,
            int[] sizePerVertex) {
        if (names == null || offsets == null || types == null || bufferIndexes == null || sizePerVertex == null) {
            throw new IllegalArgumentException(
                    "Null argument in constructor " + names + ", " + offsets + ", " + types + ", " + bufferIndexes
                            + ", " + sizePerVertex);
        }
        int len = names.length;
        if (offsets.length != len || types.length != len || bufferIndexes.length != len) {
            throw new IllegalArgumentException("Length of arrays do not match " + len + ", " + offsets.length + ", "
                    + types.length + ", " + bufferIndexes.length);
        }
        this.names = names;
        this.offsets = offsets;
        this.types = types;
        this.bufferIndexes = bufferIndexes;
        this.sizePerVertex = sizePerVertex;
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

}
