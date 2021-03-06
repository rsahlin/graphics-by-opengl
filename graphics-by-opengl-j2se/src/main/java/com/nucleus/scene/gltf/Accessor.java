package com.nucleus.scene.gltf;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.google.gson.annotations.SerializedName;
import com.nucleus.SimpleLogger;
import com.nucleus.scene.gltf.GLTF.GLTFException;

/**
 * The Accessor as it is loaded using the glTF format.
 * 
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

public class Accessor extends GLTFNamedValue implements GLTF.RuntimeResolver {

    private static final String BUFFER_VIEW = "bufferView";
    private static final String BYTE_OFFSET = "byteOffset";
    private static final String COMPONENT_TYPE = "componentType";
    private static final String NORMALIZED = "normalized";
    private static final String COUNT = "count";
    private static final String TYPE = "type";
    private static final String MAX = "max";
    private static final String MIN = "min";

    public enum ComponentType {
        BYTE(5120, 1),
        UNSIGNED_BYTE(5121, 1),
        SHORT(5122, 2),
        UNSIGNED_SHORT(5123, 2),
        UNSIGNED_INT(5125, 4),
        FLOAT(5126, 4);

        /**
         * The gl value
         */
        public final int value;
        /**
         * Size in bytes
         */
        public final int size;

        private ComponentType(int value, int size) {
            this.value = value;
            this.size = size;
        }

        public static ComponentType get(int value) {
            for (ComponentType cp : values()) {
                if (cp.value == value) {
                    return cp;
                }
            }
            return null;
        }

    }

    public enum Type {
        SCALAR(1),
        VEC2(2),
        VEC3(3),
        VEC4(4),
        MAT2(2),
        MAT3(3),
        MAT4(4);

        public final int size;
        public final int sizeInBytes;

        private Type(int size) {
            this.size = size;
            sizeInBytes = size * 4;

        }
    }

    public static final int DEFAULT_BYTE_OFFSET = 0;
    public static final boolean DEFAULT_NORMALIZED = false;

    @SerializedName(BUFFER_VIEW)
    private int bufferViewIndex = -1;
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
    private int componentTypeValue;
    @SerializedName(NORMALIZED)
    private boolean normalized = DEFAULT_NORMALIZED;
    @SerializedName(COUNT)
    private int count = -1;
    @SerializedName(TYPE)
    private Type type;
    @SerializedName(MAX)
    private float[] max;
    @SerializedName(MIN)
    private float[] min;

    transient ComponentType componentType;
    transient BufferView bufferViewRef;

    public Accessor(BufferView bufferView, int byteOffset, ComponentType componentType, int count, Type type) {
        this.bufferViewRef = bufferView;
        this.byteOffset = byteOffset;
        this.componentType = componentType;
        this.count = count;
        this.type = type;
        this.bufferViewIndex = bufferView.getBufferIndex();
        if (bufferViewIndex == -1) {
            throw new IllegalArgumentException("Invalid index for BufferView");
        }
    }

    public int getBufferViewIndex() {
        return bufferViewIndex;
    }

    public BufferView getBufferView() {
        return bufferViewRef;
    }

    /**
     * The offset relative to the start of the bufferView in bytes.
     * 
     * @return
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * The datatype of components in the attribute
     * 
     * @return
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * Returns true if integer data should be normalized
     * 
     * @return
     */
    public boolean isNormalized() {
        return normalized;
    }

    /**
     * Returns the number of attributes referenced by this accessor
     * 
     * @return
     */
    public int getCount() {
        return count;
    }

    public float[] getMax() {
        return max;
    }

    public float[] getMin() {
        return min;
    }

    /**
     * Specifies if the attribute is a scalar, vector, or matrix
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the ByteBuffer positioned according to this accessor and bufferview
     * 
     * @return
     */
    public ByteBuffer getBuffer() {
        ByteBuffer buffer = bufferViewRef.getBuffer().buffer;
        buffer.position(byteOffset + bufferViewRef.getByteOffset());
        return buffer;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        if (bufferViewRef != null) {
            throw new GLTFException("Already resolved Accessor with name " + getName());
        }
        bufferViewRef = asset.getBufferViews()[bufferViewIndex];
        componentType = ComponentType.get(componentTypeValue);
    }

    @Override
    public String toString() {
        String str = "Bufferviewindex: " + bufferViewIndex + ", count: " + count + ", offset: " + byteOffset
                + ", component: " + componentType
                + ", type: " + type + (getName() != null ? (", name: " + getName()) : "") + "\n";
        bufferViewRef.getBuffer().getBuffer().position(byteOffset);
        switch (componentType) {
            case BYTE:
            case UNSIGNED_BYTE:
                str += bufferViewRef.toString(bufferViewRef.getBuffer().getBuffer());
                break;
            case SHORT:
            case UNSIGNED_SHORT:
                str += bufferViewRef.toString(bufferViewRef.getBuffer().getBuffer().asShortBuffer());
                break;
            case FLOAT:
                str += bufferViewRef.toString(bufferViewRef.getBuffer().getBuffer().asFloatBuffer());
                break;
            default:
                str += "Not implemented toString() for " + componentType;
        }

        return str;
    }

    /**
     * Copies all data in this accessor to short buffer
     * If componentType in accessor is float then nothing is done.
     * If componentType is int then values are truncated to short.
     * No conversion is made.
     * 
     * @param dest
     * @param index Index into dest where data is copied
     */
    public void copy(int[] dest, int index) {
        switch (componentType) {
            case BYTE:
            case UNSIGNED_BYTE:
                ByteBuffer byteBuffer = getBuffer();

                for (int i = 0; i < count; i++) {
                    dest[index++] = byteBuffer.get();
                }
                break;
            case UNSIGNED_INT:
                IntBuffer intBuffer = getBuffer().asIntBuffer();
                intBuffer.get(dest, index, count);
                break;

            case SHORT:
            case UNSIGNED_SHORT:
                ShortBuffer shortBuffer = getBuffer().asShortBuffer();
                short[] ushort = new short[count];
                shortBuffer.get(ushort, index, count);
                for (int i = 0; i < count; i++) {
                    dest[index++] = (ushort[i] & 0x0ffff);
                }
                break;
            default:
                SimpleLogger.d(getClass(), "Wrong component type, cannot copy " + componentType + " to dest buffer");
        }
    }

    /**
     * Copies all data in this accessor to float buffer, component type must be FLOAT otherwise nothing is done.
     * 
     * @param dest
     * @param index
     */
    public void copy(float[] dest, int index) {
        switch (componentType) {
            case FLOAT:
                copy(dest, index, getBuffer().asFloatBuffer());
                break;
            default:
                SimpleLogger.d(getClass(), "Wrong component type, cannot copy " + componentType + " to float buffer");
        }
    }

    /**
     * 
     * @param dest
     * @param index
     * @param buffer The source data
     */
    private void copy(float[] dest, int index, FloatBuffer buffer) {
        BufferView bv = getBufferView();
        if (bv.getByteStride() <= type.size) {
            // Straight copy of all data
            buffer.get(dest, index, count * type.size);
        } else {
            final int size = getType().size;
            int stride = bv.getByteStride() / getComponentType().size;
            int pos = buffer.position();
            for (int i = 0; i < count; i++) {
                buffer.get(dest, index + i * size, size);
                pos += stride;
                buffer.position(pos);
            }
        }
    }

}
