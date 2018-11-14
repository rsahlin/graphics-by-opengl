package com.nucleus.scene.gltf;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.BufferUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.shader.GLTFShaderProgram;
import com.nucleus.vecmath.Vec2;
import com.nucleus.vecmath.Vec3;

/**
 * The Primitive as it is loaded using the glTF format.
 * 
 * primitive
 * Geometry to be rendered with the given material.
 * 
 * Related WebGL functions: drawElements() and drawArrays()
 * 
 * Properties
 * 
 * Type Description Required
 * attributes object A dictionary object, where each key corresponds to mesh attribute semantic and each value is the
 * index of the accessor containing attribute's data. ✅ Yes
 * indices integer The index of the accessor that contains the indices. No
 * material integer The index of the material to apply to this primitive when rendering. No
 * mode integer The type of primitives to render. No, default: 4
 * targets object [1-*] An array of Morph Targets, each Morph Target is a dictionary mapping attributes (only POSITION,
 * NORMAL, and TANGENT supported) to their deviations in the Morph Target. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public class Primitive implements RuntimeResolver {

    public abstract class BufferToArray<T, U> {
        public abstract U copyBuffer(T buffer);
    }

    public class FloatBufferToArray extends BufferToArray<FloatBuffer, float[]> {
        @Override
        public float[] copyBuffer(FloatBuffer buffer) {
            return null;
        }
    }

    public class Triangles {
        float[] verticeArray;
        float[] uvArray;
        float[] normalArray;
        short[] indexArray;

        public void createBuffers(Accessor indices, Accessor position, Accessor uv, Accessor normal) {
            verticeArray = copyFloatBuffer(position);
            uvArray = copyFloatBuffer(uv);
            normalArray = copyFloatBuffer(normal);
            indexArray = copyShortBuffer(indices);
        }

        public float[][] calculateTangentBiTangent(Accessor indices, Accessor position, Accessor uv, Accessor normal) {
            float[][] result = new float[2][verticeArray.length];
            int verticeSize = position.getType().size;
            float[] deltaPos1 = new float[3];
            float[] deltaPos2 = new float[3];
            float[] deltaUv1 = new float[] { 1, 1 };
            float[] deltaUv2 = new float[] { 1, 1 };
            float[] temp1 = new float[3];
            float[] temp2 = new float[3];
            float[] tangent = new float[3];
            float[] biTangent = new float[3];
            int uvSize = 0;

            if (uv != null) {
                uvSize = uv.getType().size;
            }

            for (int i = 0; i < indexArray.length; i += 3) {
                int index0 = indexArray[i];
                int index1 = indexArray[i + 1];
                int index2 = indexArray[i + 2];

                int v0Index = index0 * verticeSize;
                int v1Index = index1 * verticeSize;
                int v2Index = index2 * verticeSize;

                int uv0Index = index0 * uvSize;
                int uv1Index = index1 * uvSize;
                int uv2Index = index2 * uvSize;

                Vec3.toVector(verticeArray, v0Index, verticeArray, v1Index, deltaPos1, 0);
                Vec3.toVector(verticeArray, v0Index, verticeArray, v2Index, deltaPos2, 0);

                float reciprocal = 1f;
                if (uvArray != null) {
                    Vec2.toVector(uvArray, uv0Index, uvArray, uv1Index, deltaUv1, 0);
                    Vec2.toVector(uvArray, uv0Index, uvArray, uv2Index, deltaUv2, 0);
                    reciprocal = 1.0f / (deltaUv1[0] * deltaUv2[1] - deltaUv1[1] * deltaUv2[0]);
                }

                Vec3.mul(deltaPos1, 0, deltaUv2[1] * reciprocal, temp1, 0);
                Vec3.mul(deltaPos2, 0, deltaUv1[1] * reciprocal, temp2, 0);
                Vec3.subtract(temp1, 0, temp2, 0, tangent, 0);

                Vec3.mul(deltaPos2, 0, deltaUv1[0] * reciprocal, temp1, 0);
                Vec3.mul(deltaPos1, 0, deltaUv2[0] * reciprocal, temp2, 0);
                Vec3.subtract(temp1, 0, temp2, 0, biTangent, 0);

                Vec3.add(result[0], index0, tangent, 0, result[0], index0);
                Vec3.add(result[0], index1, tangent, 0, result[0], index1);
                Vec3.add(result[0], index2, tangent, 0, result[0], index2);

                Vec3.add(result[0], index0, biTangent, 0, result[0], index0);
                Vec3.add(result[0], index1, biTangent, 0, result[0], index1);
                Vec3.add(result[0], index2, biTangent, 0, result[0], index2);
            }
            return result;
        }

        private short[] copyShortBuffer(Accessor data) {
            BufferView bv = data.getBufferView();
            ShortBuffer buffer = bv.getBuffer().buffer.asShortBuffer();
            int count = data.getCount();
            short[] result = new short[count * data.getType().size];

            int offset = (data.getByteOffset() + bv.getByteOffset()) / data.getComponentType().size;
            buffer.position(offset);
            if (bv.getByteStride() < 4) {
                // Straight copy of all data
                buffer.get(result);
            } else {
                int size = data.getType().size;
                int stride = bv.getByteStride() / data.getComponentType().size;
                int pos = buffer.position();
                for (int i = 0; i < count; i++) {
                    buffer.get(result, i * size, size);
                    pos += stride;
                    buffer.position(pos);
                }
            }
            return result;

        }

        private float[] copyFloatBuffer(Accessor data) {
            if (data == null) {
                return null;
            }
            BufferView bv = data.getBufferView();
            FloatBuffer buffer = bv.getBuffer().buffer.asFloatBuffer();
            int count = data.getCount();
            float[] result = new float[count * data.getType().size];

            int offset = (data.getByteOffset() + bv.getByteOffset()) / data.getComponentType().size;
            buffer.position(offset);
            if (bv.getByteStride() < 4) {
                // Straight copy of all data
                buffer.get(result);
            } else {
                int size = data.getType().size;
                int stride = bv.getByteStride() / data.getComponentType().size;
                int pos = buffer.position();
                for (int i = 0; i < count; i++) {
                    buffer.get(result, i * size, size);
                    pos += stride;
                    buffer.position(pos);
                }
            }
            return result;
        }
    }

    private static final int DEFAULT_MODE = 4;

    private static final String ATTRIBUTES = "attributes";
    private static final String INDICES = "indices";
    private static final String MATERIAL = "material";
    private static final String MODE = "mode";
    private static final String TARGETS = "targets";

    public enum Attributes {
        POSITION(),
        NORMAL(),
        TANGENT(),
        TEXCOORD_0(),
        TEXCOORD_1(),
        TEXCOORD_2(),
        TEXCOORD_3(),
        COLOR_0(),
        COLOR_1(),
        WEIGHTS_0(),
        WEIGHTS_1(),
        /**
         * Custom Attributes
         */
        _ROTATE(),
        _SCALE(),
        _TRANSLATE(),
        _FRAME(),
        _ALBEDO(),
        _EMISSIVE(),
        _BOUNDS(),
        _PBRDATA(),
        _LIGHT_0();
    }

    public enum Mode {
        POINTS(0, GLES20.GL_POINTS),
        LINES(1, GLES20.GL_LINES),
        LINE_LOOP(2, GLES20.GL_LINE_LOOP),
        LINE_STRIP(3, GLES20.GL_LINE_STRIP),
        TRIANGLES(4, GLES20.GL_TRIANGLES),
        TRIANGLE_STRIP(5, GLES20.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(6, GLES20.GL_TRIANGLE_FAN);

        public final int index;
        /**
         * The OpenGL value
         */
        public final int value;

        private Mode(int index, int value) {
            this.index = index;
            this.value = value;
        }

        public static final Mode getMode(int index) {
            for (Mode m : values()) {
                if (m.index == index) {
                    return m;
                }
            }
            return null;
        }

    }

    @SerializedName(ATTRIBUTES)
    private HashMap<Attributes, Integer> attributes;
    @SerializedName(INDICES)
    private int indicesIndex = -1;
    @SerializedName(MATERIAL)
    private int material = -1;
    /**
     * Allowed values:
     * 0 POINTS
     * 1 LINES
     * 2 LINE_LOOP
     * 3 LINE_STRIP
     * 4 TRIANGLES
     * 5 TRIANGLE_STRIP
     * 6 TRIANGLE_FAN
     */
    @SerializedName(MODE)
    private int modeIndex = DEFAULT_MODE;

    transient private ArrayList<Accessor> accessorList;
    transient private ArrayList<Attributes> attributeList;
    transient private ArrayList<Buffer> bufferList;
    transient private Material materialRef;
    /**
     * Program to use when rendering this primitive
     */
    @Deprecated
    transient private GLTFShaderProgram program;
    transient private Accessor indices;
    transient private Mode mode;

    /**
     * Returns the dictionary (HashMap) with Attributes
     * 
     * @return
     */
    public HashMap<Attributes, Integer> getAttributes() {
        return attributes;
    }

    public ArrayList<Accessor> getAccessorArray() {
        return accessorList;
    }

    public ArrayList<Attributes> getAttributesArray() {
        return attributeList;
    }

    public ArrayList<Buffer> getBufferArray() {
        return bufferList;
    }

    /**
     * Returns the Accessor for the attribute, or null if not defined.
     * 
     * @param glTF
     * @param attribute
     * @return
     */
    @Deprecated
    public Accessor getAccessor(GLTF glTF, Attributes attribute) {
        Integer index = attributes.get(attribute);
        if (index != null) {
            return glTF.getAccessor(index);
        }
        return null;
    }

    /**
     * Returns the Accessor for the attribute if defined, otherwise null
     * 
     * @param attribute
     * @return
     */
    public Accessor getAccessor(Attributes attribute) {
        if (attributeList != null) {
            for (int i = 0; i < attributeList.size(); i++) {
                if (attributeList.get(i) == attribute) {
                    return accessorList.get(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the index of the accessor that contains the indices.
     * 
     * @return Index of indices or -1 if no indices.
     */
    public int getIndicesIndex() {
        return indicesIndex;
    }

    /**
     * Returns the accessor containing the indices, if this primitive is not indexed null is returned.
     * 
     * @return The indices, or null if drawArrays should be used.
     */
    public Accessor getIndices() {
        return indices;
    }

    /**
     * Sets the index of the accessor that contains the indices
     * 
     * @param indices
     */
    public void setIndicesIndex(int indices) {
        this.indicesIndex = indices;
    }

    /**
     * Returns the index of the material to apply when rendering this primitive
     * 
     * @return
     */
    public int getMaterialIndex() {
        return material;
    }

    public Material getMaterial() {
        return materialRef;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
        mode = Mode.getMode(modeIndex);
        createAttributeList(asset);
        if (material >= 0) {
            this.materialRef = asset.getMaterials()[material];
        }
        indices = asset.getAccessor(indicesIndex);
    }

    private void createAttributeList(GLTF asset) {
        if (attributes != null && attributes.size() > 0) {
            Set<Buffer> bufferSet = new HashSet<>();
            accessorList = new ArrayList<>();
            attributeList = new ArrayList<>();
            for (Attributes a : attributes.keySet()) {
                attributeList.add(a);
                Accessor accessor = asset.getAccessor(attributes.get(a));
                accessorList.add(accessor);
                bufferSet.add(asset.getBuffer(accessor));
            }
            bufferList = new ArrayList<>();
            for (Buffer b : bufferSet) {
                bufferList.add(b);
            }
        }
    }

    /**
     * Builds the Tangent/Binormal buffers
     * Must be called after buffers are loaded
     */
    public void calculateTBN() {
        Accessor position = getAccessor(Attributes.POSITION);
        Accessor normal = getAccessor(Attributes.NORMAL);
        Accessor uv = getAccessor(Attributes.TEXCOORD_0);
        if (indices == null) {
            throw new IllegalArgumentException("Arrayed mode not supported");
        }
        FloatBuffer tangentBuffer = BufferUtils.createFloatBuffer(normal.getCount());
        FloatBuffer bitangentBuffer = BufferUtils.createFloatBuffer(normal.getCount());
        buildTBNBuffers(mode, indices, position, uv, normal, tangentBuffer, bitangentBuffer);
    }

    private void buildTBNBuffers(Mode mode, Accessor indices, Accessor position, Accessor uv, Accessor normal,
            FloatBuffer tangentBuffer, FloatBuffer bitangentBuffer) {

        switch (mode) {
            case TRIANGLES:
                buildTBNTriangles(indices, position, uv, normal, tangentBuffer, bitangentBuffer);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + mode);
        }

    }

    /**
     * Builds the tangent and binormal buffers for this primitive using TRIANGLES mode.
     * 
     * @param indices
     * @param position
     * @param uv
     * @param normal
     * @param tangentBuffer
     * @param bitangentBuffer
     */
    private void buildTBNTriangles(Accessor indices, Accessor position, Accessor uv, Accessor normal,
            FloatBuffer tangentBuffer, FloatBuffer bitangentBuffer) {

        BufferView indexView = indices.getBufferView();
        ShortBuffer sb = indexView.getBuffer().getBuffer().asShortBuffer();
        sb.position((indexView.getByteOffset() + indices.getByteOffset()) / 2);

        Triangles triangles = new Triangles();
        triangles.createBuffers(indices, position, uv, normal);
        float[][] TB = triangles.calculateTangentBiTangent(indices, position, uv, normal);

    }

    /**
     * @deprecated Primitive should not have a reference to ShaderProgram, use index instead and fetch from
     * AssetManager.
     * @param program
     */
    @Deprecated
    public void setProgram(GLTFShaderProgram program) {
        this.program = program;
    }

    /**
     * Returns the program to be used to render this primitive
     * 
     * @return
     */
    @Deprecated
    public GLTFShaderProgram getProgram() {
        return program;
    }

}
