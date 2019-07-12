package com.nucleus.opengl.shader;

import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;
import com.nucleus.shader.VariableIndexer;

public class NamedVariableIndexer extends VariableIndexer {

    protected String[] names;

    public NamedVariableIndexer() {

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
    public NamedVariableIndexer(String[] names, int[] offsets, VariableType[] types, BufferIndex[] bufferIndexes,
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
     * Creates indexer for one, or more, buffer storage
     * 
     * @param property
     * @param offsets
     * @param type
     * @param sizePerVertex
     */
    protected void createArrays(NamedVariableEnum[] property, int[] offsets, VariableType[] types, int[] sizePerVertex,
            BufferIndex[] bufferIndex) {
        int count = Property.values().length;
        this.names = new String[count];
        this.offsets = new int[count];
        this.types = new VariableType[count];
        this.bufferIndexes = new BufferIndex[count];
        this.sizePerVertex = new int[sizePerVertex.length];
        System.arraycopy(sizePerVertex, 0, this.sizePerVertex, 0, sizePerVertex.length);
        for (int i = 0; i < property.length; i++) {
            NamedVariableEnum p = property[i];
            this.names[p.getLocation()] = p.getName();
            this.offsets[p.getLocation()] = offsets[i];
            this.types[p.getLocation()] = types[i];
            this.bufferIndexes[p.getLocation()] = bufferIndex[i];
        }

    }

    /**
     * Returns the index of the named variable, or -1 if not defined
     * Deprecated - use method taking Property enum instead.
     * 
     * @param name
     * @return The location, or -1 if no matching name
     */
    @Deprecated
    public int getIndexByName(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null && names[i].contentEquals(name)) {
                return i;
            }
        }
        return -1;
    }

}
