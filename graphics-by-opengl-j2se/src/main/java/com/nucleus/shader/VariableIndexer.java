package com.nucleus.shader;

import com.nucleus.geometry.AttributeUpdater;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * Connecting variables in shader programs with offsets so that data can be written into buffer that stores the
 * attributes. This can be seen as a type of layout where the purpose is to connect the fields defined in a shader
 * with the usage.
 * Can be set to {@link Shader} by calling {@link Shader#setIndexer(VariableIndexer)} to set location mapping
 *
 */
public class VariableIndexer {

    public interface VariableEnum {
        /**
         * Returns the location of this enum
         * 
         * @return
         */
        public int getLocation();

        /**
         * Returns all defined enum values
         * 
         * @return
         */
        public VariableEnum[] getValues();
    }

    public interface NamedVariableEnum extends VariableEnum {
        /**
         * Returns the name of the variable
         * 
         * @return
         */
        public String getName();
    }

    /**
     * Enum of properties that may be declared in shaders, this is to allow fetching known properties without
     * knowing the exact shader source variable name.
     *
     */
    public enum Property implements NamedVariableEnum {
        VERTEX("aVertex", 0),
        UV("aTexCoord", 1),
        TRANSLATE("aTranslate", 2),
        ROTATE("aRotate", 3),
        SCALE("aScale", 4),
        ALBEDO("aColor", 5),
        EMISSIVE("aColor", 6),
        FRAME("aFrameData", 7);

        private final String name;
        private final int location;

        private Property(String name, int location) {
            this.name = name;
            this.location = location;
        }

        @Override
        public int getLocation() {
            return location;
        }

        @Override
        public VariableEnum[] getValues() {
            return Property.values();
        }

        @Override
        public String getName() {
            return name;
        }

    }

    protected int[] offsets;
    protected VariableType[] types;
    protected BufferIndex[] bufferIndexes;
    protected int[] sizePerVertex;

    protected VariableIndexer() {
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

    public int[] getSizesPerVertex() {
        return sizePerVertex;
    }

}
