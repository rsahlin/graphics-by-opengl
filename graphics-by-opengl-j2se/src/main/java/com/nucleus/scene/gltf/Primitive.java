package com.nucleus.scene.gltf;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.BufferUtils;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;
import com.nucleus.shader.GLTFShaderProgram;

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

    transient private Accessor[] accessorList;
    transient private Attributes[] attributeList;
    transient private Buffer[] bufferList;
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

    public Accessor[] getAccessorArray() {
        return accessorList;
    }

    public Attributes[] getAttributesArray() {
        return attributeList;
    }

    public Buffer[] getBufferArray() {
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
            for (int i = 0; i < attributeList.length; i++) {
                if (attributeList[i] == attribute) {
                    return accessorList[i];
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
        if (attributes != null && attributes.size() > 0) {
            Set<Buffer> bufferSet = new HashSet<>();
            accessorList = new Accessor[attributes.size()];
            attributeList = new Attributes[attributes.size()];
            int index = 0;
            for (Attributes a : attributes.keySet()) {
                attributeList[index] = a;
                Accessor accessor = asset.getAccessor(attributes.get(a));
                accessorList[index] = accessor;
                bufferSet.add(asset.getBuffer(accessor));
                index++;
            }
            bufferList = new Buffer[bufferSet.size()];
            index = 0;
            for (Buffer b : bufferSet) {
                bufferList[index++] = b;
            }
        }
        if (material >= 0) {
            this.materialRef = asset.getMaterials()[material];
        }
        indices = asset.getAccessor(indicesIndex);
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
        int count = indices.getCount();

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

    private void buildTBNTriangles(Accessor indices, Accessor position, Accessor uv, Accessor normal,
            FloatBuffer tangentBuffer, FloatBuffer bitangentBuffer) {

        BufferView indexView = indices.getBufferView();
        ShortBuffer sb = indexView.getBuffer().getBuffer().asShortBuffer();
        sb.position((indexView.getByteOffset() + indices.getByteOffset()) / 2);

        int triangles = indices.getCount();
        int triangle = 0;

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
