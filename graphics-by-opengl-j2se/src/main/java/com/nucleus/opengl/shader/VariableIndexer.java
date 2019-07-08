package com.nucleus.opengl.shader;

import java.util.ArrayList;

import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * Connecting variables in shader programs with offsets so that data can be written into buffer that stores the
 * attributes. This can be seen as a type of layout where the purpose is to connect the fields defined in a shader
 * with the usage.
 * Can be set to {@link GLShaderProgram} by calling {@link GLShaderProgram#setIndexer(VariableIndexer)} to set
 * attribute offsets.
 *
 */
public class VariableIndexer {

    protected String[] names;
    protected int[] offsets;
    protected VariableType[] types;
    protected BufferIndex[] bufferIndexes;
    protected int[] sizePerVertex;

    /**
     * Enum of properties that may be declared in shaders, this is to allow fetching known properties without
     * knowing the exact shader source variable name.
     *
     */
    public enum Property {
        VERTEX("aVertex", 0),
        UV("aTexCoord", 1),
        TRANSLATE("aTranslate", 2),
        ROTATE("aRotate", 3),
        SCALE("aScale", 4),
        ALBEDO("aColor", 5),
        EMISSIVE("aColor", 6),
        FRAME("aFrameData", 7);

        public final String name;
        public final int location;

        private Property(String name, int location) {
            this.name = name;
            this.location = location;
        }

    }

    protected VariableIndexer() {
    }

    /**
     * Creates indexer for one buffer storage
     * 
     * @param property
     * @param offsets
     * @param type
     * @param sizePerVertex
     */
    protected void createArrays(Property[] property, int[] offsets, VariableType[] type, int sizePerVertex,
            BufferIndex bufferIndex) {

        int count = Property.values().length;
        this.names = new String[count];
        this.offsets = new int[count];
        this.types = new VariableType[count];
        this.bufferIndexes = new BufferIndex[count];
        this.sizePerVertex = new int[] { sizePerVertex };
        for (int i = 0; i < property.length; i++) {
            Property p = property[i];
            this.names[p.location] = p.name;
            this.offsets[p.location] = offsets[i];
            this.types[p.location] = type[i];
            this.bufferIndexes[p.location] = bufferIndex;
        }

    }

    /**
     * Returns the location of the named variable, or -1 if not defined
     * Deprecated - use method taking Property enum instead.
     * 
     * @param name
     * @return The location, or -1 if no matching name
     */
    @Deprecated
    public int getLocationByName(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null && names[i].contentEquals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the offset for data to the variable at the location
     * 
     * @param location
     * @return
     */
    public int getOffset(int location) {
        return offsets[location];
    }

    /**
     * Returns the type of variable at the location
     * 
     * @return Type of variable
     */
    public VariableType getType(int location) {
        return types[location];
    }

    /**
     * Returns the buffer index of the variable at location
     * This value can be used to call {@link AttributeUpdater#getAttributeBuffer(BufferIndex)}
     * 
     * @param BufferIndex Index to buffer holding variables, or null if index is not valid
     */
    public BufferIndex getBufferIndex(int location) {
        return location >= 0 && location < bufferIndexes.length ? bufferIndexes[location] : null;
    }

    /**
     * Returns the size (in floats) per vertex that the buffer shall have - may be larger than actual usage.
     * 
     * @param bufferIndex
     * @return
     */
    public int getSizePerVertex(int bufferIndex) {
        return sizePerVertex[bufferIndex];
    }

    /**
     * Sort the variables belonging to the specified buffer index. Returning an array with the variables.
     * 
     * @param activeVariables
     * @param index Index of the buffer
     * @return
     */
    public NamedShaderVariable[] sortByBuffer(NamedShaderVariable[] activeVariables, int index) {
        ArrayList<NamedShaderVariable> result = new ArrayList<>();
        for (NamedShaderVariable v : activeVariables) {
            BufferIndex bi = getBufferIndex(getLocationByName(v.getName()));
            if (bi != null && bi.index == index) {
                result.add(v);
            }
        }
        NamedShaderVariable[] array = new NamedShaderVariable[result.size()];
        return result.toArray(array);
    }

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

}
