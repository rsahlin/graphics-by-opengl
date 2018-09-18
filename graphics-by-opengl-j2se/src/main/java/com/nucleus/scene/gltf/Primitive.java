package com.nucleus.scene.gltf;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

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
public class Primitive {

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
        ROTATE(),
        SCALE(),
        TRANSLATE(),
        FRAME(),
        ALBEDO(),
        EMISSIVE(),
        BOUNDS();
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

    public HashMap<Attributes, Integer> getAttributes() {
        return attributes;
    }

    /**
     * Returns the index of the accessor that contains the indices.
     * @return
     */
    public int getIndices() {
        return indices;
    }

    public void setIndices(int indices) {
        this.indices = indices;
    }

    /**
     * Returns the index of the material to apply when rendering this primitive
     * @return
     */
    public int getMaterialIndex() {
        return material;
    }

    public void setMaterialIndex(int material) {
        this.material = material;
    }

    public Mode getMode() {
        if (mode == null) {
            mode = Mode.getMode(modeValue);
        }
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

}
