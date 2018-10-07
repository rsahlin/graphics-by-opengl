package com.nucleus.scene.gltf;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.BufferUtils;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

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
public class BufferView extends GLTFNamedValue implements RuntimeResolver {

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
    private int bufferIndex = -1;
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
    private int targetValue = -1;

    transient private Target target;
    transient private Buffer buffer;

    /**
     * Creates a BufferView based on the specified Buffer
     * 
     * @param buffer
     * @param byteOffset
     * @param byteStride
     * @param target
     */
    public BufferView(Buffer buffer, int byteOffset, int byteStride, Target target) {
        this.buffer = buffer;
        this.byteOffset = byteOffset;
        this.byteLength = buffer.getByteLength();
        this.byteStride = byteStride;
        this.target = target;
    }

    public int getBufferIndex() {
        return bufferIndex;
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

    public Target getTarget() {
        return target;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        this.buffer = asset.getBuffers()[bufferIndex];
        if (targetValue >= 0) {
            target = Target.getTarget(targetValue);

        }
    }

    /**
     * Returns the contents of toString() + the contents of byteBuffer limited to {@link Buffer#MAX_BUFFER_PRINT}
     * 
     * @param byteBuffer
     * @return
     */
    public String toString(ByteBuffer byteBuffer) {
        return toString() + "\n" + BufferUtils.getContentAsString(byteOffset,
                byteLength < Buffer.MAX_BUFFER_PRINT ? byteLength : Buffer.MAX_BUFFER_PRINT, byteBuffer);
    }

    /**
     * Returns the contents of toString() + the contents of shortBuffer limited to {@link Buffer#MAX_BUFFER_PRINT}
     * 
     * @param shortBuffer
     * @return
     */
    public String toString(ShortBuffer shortBuffer) {
        return toString() + "\n" + BufferUtils.getContentAsString(byteOffset >>> 1,
                byteLength >>> 1 < Buffer.MAX_BUFFER_PRINT ? byteLength >>> 1 : Buffer.MAX_BUFFER_PRINT, shortBuffer);
    }

    /**
     * Returns the contents of toString() + the contents of shortBuffer limited to {@link Buffer#MAX_BUFFER_PRINT}
     * 
     * @param floatBuffer
     * @return
     */
    public String toString(FloatBuffer floatBuffer) {
        return toString() + "\n" + BufferUtils.getContentAsString(byteOffset >>> 2,
                byteLength >>> 2 < Buffer.MAX_BUFFER_PRINT ? byteLength >>> 2 : Buffer.MAX_BUFFER_PRINT, floatBuffer);
    }

    @Override
    public String toString() {
        return "Bufferindex: " + bufferIndex + ", byteoffset: " + byteOffset + ", byteLength: " + byteLength
                + ", byteStride: " + byteStride + ", name: " + getName();
    }

}
