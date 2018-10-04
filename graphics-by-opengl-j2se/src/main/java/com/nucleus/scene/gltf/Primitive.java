package com.nucleus.scene.gltf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.nucleus.scene.gltf.GLTF.GLTFException;
import com.nucleus.scene.gltf.GLTF.RuntimeResolver;

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
        _LIGHT_0();
    }

    public enum Mode {
        POINTS(0),
        LINES(1),
        LINE_LOOP(2),
        LINE_STRIP(3),
        TRIANGLES(4),
        TRIANGLE_STRIP(5),
        TRIANGLE_FAN(6);

        public final int value;

        private Mode(int mode) {
            this.value = mode;
        }

        /**
         * Returns the mode for the mode value, or null if no matching mode
         * 
         * @param mode
         * @return
         */
        public static Mode getMode(int mode) {
            for (Mode m : values()) {
                if (m.value == mode) {
                    return m;
                }
            }
            return null;
        }

    }

    @SerializedName(ATTRIBUTES)
    private HashMap<Attributes, Integer> attributes;
    @SerializedName(INDICES)
    private int indices = -1;
    @SerializedName(MATERIAL)
    private int material;
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
    private int modeValue = DEFAULT_MODE;

    transient private Mode mode;
    transient private Accessor[] accessorList;
    transient private Attributes[] attributeList;
    transient private Buffer[] bufferList;
    transient private Material materialRef;

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
    public Accessor getAccessor(GLTF glTF, Attributes attribute) {
        Integer index = attributes.get(attribute);
        if (index != null) {
            return glTF.getAccessor(index);
        }
        return null;
    }

    /**
     * Returns the index of the accessor that contains the indices.
     * 
     * @return Index of indices or -1 if no indices.
     */
    public int getIndicesIndex() {
        return indices;
    }

    /**
     * Sets the index of the accessor that contains the indices
     * 
     * @param indices
     */
    public void setIndicesIndex(int indices) {
        this.indices = indices;
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

    /**
     * Allowed values:
     * 0 POINTS
     * 1 LINES
     * 2 LINE_LOOP
     * 3 LINE_STRIP
     * 4 TRIANGLES
     * 5 TRIANGLE_STRIP
     * 6 TRIANGLE_FAN
     * 
     * @param mode
     * @throws IllegalArgumentException If mode is not one of the allowed values
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        this.modeValue = mode.value;
    }

    @Override
    public void resolve(GLTF asset) throws GLTFException {
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
        mode = Mode.getMode(modeValue);
        this.materialRef = asset.getMaterials()[material];
    }

}
