package com.nucleus.opengl.shader;

import java.util.ArrayList;

import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.opengl.shader.ShaderVariable.VariableType;

/**
 * Connecting variables in shader programs with offsets so that data can be written into buffer that stores the
 * attributes. This can be seen as a type of layout where the purpose is to connect the fields defined in a shader
 * with the usage.
 * Case be set to {@link GLShaderProgram} by calling {@link GLShaderProgram#setIndexer(VariableIndexer)} to set attribute
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
     * Returns the buffer index in the attribute updater.
     * This value can be used to call {@link AttributeUpdater#getAttributeBuffer(BufferIndex)}
     * 
     * @param BufferIndex Index to buffer holding variables, or null if index is not valid
     */
    public BufferIndex getBufferIndex(int index) {
        return index >= 0 && index < bufferIndexes.length ? bufferIndexes[index] : null;
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
            BufferIndex bi = getBufferIndex(getIndexByName(v.getName()));
            if (bi != null && bi.index == index) {
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

}
