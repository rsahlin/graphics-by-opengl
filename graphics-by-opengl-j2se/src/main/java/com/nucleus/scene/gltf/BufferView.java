package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * The BufferView as it is loaded using the glTF format.
 * 
 * bufferView
 * A view into a buffer generally representing a subset of the buffer.
 * 
 * Properties
 * 
 * Type Description Required
 * buffer integer The index of the buffer. ✅ Yes
 * byteOffset integer The offset into the buffer in bytes. No, default: 0
 * byteLength integer The length of the bufferView in bytes. ✅ Yes
 * byteStride integer The stride, in bytes. No
 * target integer The target that the GPU buffer should be bound to. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 * This class can be serialized using gson
 */
public class BufferView {

    public enum Target {
        ARRAY_BUFFER(34962),
        ELEMENT_ARRAY_BUFFER(34963);

        public int value;

        private Target(int value) {
            this.value = value;
        }

        /**
         * Returns the Target for the specified target value, or null if not matching any Target
         * 
         * @param value
         * @return
         */
        public static Target getTarget(int value) {
            for (Target t : values()) {
                if (t.value == value) {
                    return t;
                }
            }
            return null;
        }

    }

    public static final int DEFAULT_BYTE_OFFSET = 0;
    public static final int DEFAULT_BYTE_STRIDE = 0;
    private static final String BUFFER = "buffer";
    private static final String BYTE_OFFSET = "byteOffset";
    private static final String BYTE_LENGTH = "byteLength";
    private static final String BYTE_STRIDE = "byteStride";
    private static final String TARGET = "target";
    private static final String NAME = "name";

    @SerializedName(BUFFER)
    private int buffer = -1;
    @SerializedName(BYTE_OFFSET)
    private int byteOffset = DEFAULT_BYTE_OFFSET;
    @SerializedName(BYTE_LENGTH)
    private int byteLength = -1;
    @SerializedName(BYTE_STRIDE)
    private int byteStride = DEFAULT_BYTE_STRIDE;
    /**
     * Allowed values
     * 34962 ARRAY_BUFFER
     * 34963 ELEMENT_ARRAY_BUFFER
     */
    @SerializedName(TARGET)
    private int target = -1;
    @SerializedName(NAME)
    private String name;

    public int getBufferIndex() {
        return buffer;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getByteLength() {
        return byteLength;
    }

    public int getByteStride() {
        return byteStride;
    }

    public int getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }

}
