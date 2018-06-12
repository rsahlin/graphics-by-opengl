package com.nucleus.scene.gltf;

import com.google.gson.annotations.SerializedName;

/**
 * accessor
 * A typed view into a bufferView. A bufferView contains raw binary data. An accessor provides a typed view into a
 * bufferView or a subset of a bufferView similar to how WebGL's vertexAttribPointer() defines an attribute in a buffer.
 * Properties
 * bufferView integer The index of the bufferView. No
 * byteOffset integer The offset relative to the start of the bufferView in bytes. No, default: 0
 * componentType integer The datatype of components in the attribute. ✅ Yes
 * normalized boolean Specifies whether integer data values should be normalized. No, default: false
 * count integer The number of attributes referenced by this accessor. ✅ Yes
 * type string Specifies if the attribute is a scalar, vector, or matrix. ✅ Yes
 * max number [1-16] Maximum value of each component in this attribute. No
 * min number [1-16] Minimum value of each component in this attribute. No
 * sparse object Sparse storage of attributes that deviate from their initialization value. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 * This class can be serialized using gson
 * 
 */

public class Accessor {

    private static final String BUFFER_VIEW = "bufferView";
    private static final String BYTE_OFFSET = "byteOffset";
    private static final String COMPONENT_TYPE = "componentType";
    private static final String NORMALIZED = "normalized";
    private static final String COUNT = "count";
    private static final String TYPE = "type";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String SPARSE = "sparse";
    private static final String NAME = "name";

    public enum Type {
        SCALAR(),
        VEC2(),
        VEC3(),
        VEC4(),
        MAT2(),
        MAT3(),
        MAT4();
    }

    public static final int DEFAULT_BYTE_OFFSET = 0;
    public static final boolean DEFAULT_NORMALIZED = false;

    @SerializedName(BUFFER_VIEW)
    private int bufferView = -1;
    /**
     * Offset into buffer relative the bufferView byte offset.
     * This is the offset for the Primitive Attribute index accessing the value in the bufferView
     */
    @SerializedName(BYTE_OFFSET)
    private int byteOffset = DEFAULT_BYTE_OFFSET;
    /**
     * Allowed values
     * 5120 BYTE
     * 5121 UNSIGNED_BYTE
     * 5122 SHORT
     * 5123 UNSIGNED_SHORT
     * 5125 UNSIGNED_INT
     * 5126 FLOAT
     */
    @SerializedName(COMPONENT_TYPE)
    private int componentType = -1;
    @SerializedName(NORMALIZED)
    private boolean normalized = DEFAULT_NORMALIZED;
    @SerializedName(COUNT)
    private int count = -1;
    @SerializedName(TYPE)
    private String type;
    @SerializedName(MAX)
    private float[] max;
    @SerializedName(MIN)
    private float[] min;
    // private Object sparse;
    @SerializedName(NAME)
    private String name;

    transient Type accessorType;

    public int getBufferView() {
        return bufferView;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getComponentType() {
        return componentType;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public int getCount() {
        return count;
    }

    public float[] getMax() {
        return max;
    }

    public float[] getMin() {
        return min;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        if (accessorType == null) {
            accessorType = Type.valueOf(type);
        }
        return accessorType;
    }

}
