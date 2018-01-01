package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES30;

/**
 * Info for the texture parameters, GL MIN and MAG filter, S and T wrap modes.
 * Helper class to make it easier to map from String names to GL values, for instance when serializing texture setup.
 * This class can be serialized using GSON
 * 
 * @author Richard Sahlin
 *
 */
public class TextureParameter {

    public final static String VALUES = "values";
    public final static String PARAMETERS = "parameters";

    public final static int MIN_FILTER_INDEX = 0;
    public final static int MAG_FILTER_INDEX = 1;
    public final static int WRAP_S_INDEX = 2;
    public final static int WRAP_T_INDEX = 3;

    public static final Parameter[] DEFAULT_TEXTURE_PARAMETERS = new Parameter[] { Parameter.NEAREST,
            Parameter.NEAREST, Parameter.CLAMP,
            Parameter.CLAMP };

    public enum Parameter {
        NEAREST(GLES20.GL_NEAREST),
        LINEAR(GLES20.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GLES20.GL_NEAREST_MIPMAP_NEAREST),
        LINEAR_MIPMAP_NEAREST(GLES20.GL_LINEAR_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GLES20.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_LINEAR(GLES20.GL_LINEAR_MIPMAP_LINEAR),
        CLAMP(GLES20.GL_CLAMP_TO_EDGE),
        REPEAT(GLES20.GL_REPEAT),
        MIRRORED_REPEAT(GLES20.GL_MIRRORED_REPEAT);

        public final int value;

        private Parameter(int value) {
            this.value = value;
        }

        /**
         * Allowed min texture filters, use this to get allowed values for texture minification filter
         */
        public static final Parameter[] MIN_FILTERS = new Parameter[] { NEAREST, LINEAR, NEAREST_MIPMAP_LINEAR,
                NEAREST_MIPMAP_NEAREST, LINEAR_MIPMAP_NEAREST, LINEAR_MIPMAP_LINEAR };
        /**
         * Allowed mag texture filters, use this to get allowed values for texture magnification filter
         */
        public static final Parameter[] MAG_FILTERS = new Parameter[] { LINEAR_MIPMAP_LINEAR };
        /**
         * Allowed texture wrap modes
         */
        public static final Parameter[] WRAPMODES = new Parameter[] { CLAMP, REPEAT, MIRRORED_REPEAT };

        /**
         * Returns the GL value for the texture parameter
         * 
         * @return
         */
        public int getValue() {
            return value;
        }

        /**
         * Returns an array with the texture UV wrap modes.
         * 
         * @return
         */
        public static Parameter[] getUVWrapModes() {
            return new Parameter[] { CLAMP, REPEAT, MIRRORED_REPEAT };
        }

        /**
         * Checks if value is valid texture minification filter
         * 
         * @param value
         * @return True if value is valid
         */
        public static boolean validateMinFilter(Parameter value) {
            return validateValue(value, MIN_FILTERS);
        }

        /**
         * Checks if value is valid texture magnification filter
         * 
         * @param value
         * @return True if value is valid
         */
        public static boolean validateMagFilter(Parameter value) {
            return validateValue(value, MAG_FILTERS);
        }

        /**
         * Checks if value is valid texture wrap mode value
         * 
         * @param value
         * @return True if value is valid
         */
        public static boolean validateWrapMode(Parameter value) {
            return validateValue(value, WRAPMODES);
        }

        private static boolean validateValue(Parameter value, Parameter[] values) {
            for (Parameter tp : values) {
                if (tp == value) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Valid targets, including 3.0 and 3.1 targets
     * TODO - Should this class be split into one for gles 2.x and one for 3.x?
     *
     */
    public enum Target {
        TEXTURE_2D(GLES20.GL_TEXTURE_2D),
        /**
         * GLES 3 target
         */
        TEXTURE_3D(GLES30.GL_TEXTURE_3D),
        /**
         * GLES 3 target
         */
        TEXTURE_2D_ARRAY(GLES30.GL_TEXTURE_2D_ARRAY),
        /**
         * GLES 3 target
         */
        TEXTURE_CUBE_MAP(GLES30.GL_TEXTURE_CUBE_MAP);

        public final int target;

        private Target(int target) {
            this.target = target;
        }

    }

    /**
     * The pname variable in OpenGL
     */
    public enum Name {
        MIN_FILTER(GLES20.GL_TEXTURE_MIN_FILTER),
        MAG_FILTER(GLES20.GL_TEXTURE_MAG_FILTER),
        WRAP_S(GLES20.GL_TEXTURE_WRAP_S),
        WRAP_T(GLES20.GL_TEXTURE_WRAP_T),
        TEXTURE_COMPARE_MODE(GLES30.GL_TEXTURE_COMPARE_MODE),
        TEXTURE_COMPARE_FUNC(GLES30.GL_TEXTURE_COMPARE_FUNC);

        public final int name;

        private Name(int name) {
            this.name = name;
        }
    }

    public enum Param {
        NEAREST(GLES20.GL_NEAREST),
        LINEAR(GLES20.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GLES20.GL_NEAREST_MIPMAP_NEAREST),
        LINEAR_MIPMAP_NEAREST(GLES20.GL_LINEAR_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GLES20.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_LINEAR(GLES20.GL_LINEAR_MIPMAP_LINEAR),
        CLAMP(GLES20.GL_CLAMP_TO_EDGE),
        REPEAT(GLES20.GL_REPEAT),
        MIRRORED_REPEAT(GLES20.GL_MIRRORED_REPEAT),
        LESS(GLES20.GL_LESS),
        GREATER(GLES20.GL_GREATER),
        COMPARE_REF_TO_TEXTURE(GLES30.GL_COMPARE_REF_TO_TEXTURE);

        public final int param;

        private Param(int param) {
            this.param = param;
        }

    }

    /**
     * Texture parameter values, MUST contain 4 values in the following order:
     * MIN_FILTER
     * MAG_FILTER
     * WRAP_S
     * WRAP_T
     * This is used for minimum texture parameters, if more parameters are needed use target,name,param
     * 
     */
    @SerializedName(VALUES)
    protected Parameter[] values;

    /**
     * Optional texture parameters - mainly used for parameters other than min/mag-filter, wrap s/t
     */
    @SerializedName(PARAMETERS)
    protected ParameterData[] parameters;

    /**
     * Default constructor
     */
    public TextureParameter() {
        super();
    }

    /**
     * Creates new texture parameters, must contain 4 values.
     * 
     * @param values 4 texture parameters, one for each {@link Name}
     * @throws IllegalArgumentException If params does not contain 4 values
     */
    public TextureParameter(Parameter[] values) {
        if (values == null || values.length < 4 || values.length > 4) {
            throw new IllegalArgumentException("Invalid parameters, length of array is not 4");
        }
        setValues(values);
    }

    public TextureParameter(Parameter min, Parameter mag, Parameter wrapS, Parameter wrapT) {
        setValues(values);
    }

    /**
     * Creates a new texture parameter from the source
     * 
     * @param source
     */
    public TextureParameter(TextureParameter source) {
        setValues(source);
    }

    /**
     * Copy values from the source texture parameters
     * 
     * @param source
     * @throws NullPointerException If values is null in source
     */
    public void setValues(TextureParameter source) {
        if (source.values == null) {
            throw new IllegalArgumentException("No texture parameters in source.");
        }
        values = new Parameter[source.values.length];
        int index = 0;
        for (Parameter p : source.values) {
            if (p == null) {
                throw new IllegalArgumentException("Texture parameter value is null, invalid name in source?");
            }
            values[index++] = p;
        }
    }

    /**
     * Clears the current values and sets the specified texture parameter values.
     * 
     * @param values The values to set, shall contain min filter, mag filter, wrap s and wrap t
     * @throws IllegalArgumentException if values is null or does not contain 4 values, or contains invalid values
     */
    public void setValues(Parameter[] values) {
        if (values == null || values.length < 4) {
            throw new IllegalArgumentException("Invalid values:" + values);
        }
        if (this.values == null) {
            this.values = new Parameter[values.length];
        }
        int index = 0;
        for (Parameter p : values) {
            this.values[index++] = p;
        }
        validateValues();
    }

    /**
     * Replaces the current values with the specified min, mag, wrapS, wrapT values
     * 
     * @param min
     * @param mag
     * @param wrapS
     * @param wrapT
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public void setValues(Parameter min, Parameter mag, Parameter wrapS, Parameter wrapT) {
        if (values == null) {
            values = new Parameter[Name.values().length];
        }
        values[MIN_FILTER_INDEX] = min;
        values[MAG_FILTER_INDEX] = mag;
        values[WRAP_S_INDEX] = wrapS;
        values[WRAP_T_INDEX] = wrapT;
        validateValues();
    }

    protected void validateValues() {
        if (!Parameter.validateMinFilter(values[MIN_FILTER_INDEX])
                || Parameter.validateMagFilter(values[MAG_FILTER_INDEX])
                || !Parameter.validateWrapMode(values[WRAP_S_INDEX])
                || !Parameter.validateWrapMode(values[WRAP_T_INDEX])) {
            throw new IllegalArgumentException(
                    "Invalid texture mode:" + values[0] + ", " + values[1] + ", " + values[2] + ", " + values[3]);
        }
    }

    /**
     * Checks if the min filter in the texture parameter is one of the mipmap filters.
     * 
     * @return True if the texture parameters use mipmaps
     */
    public boolean isMipMapFilter() {
        switch (values[MIN_FILTER_INDEX]) {
            case LINEAR_MIPMAP_LINEAR:
            case NEAREST_MIPMAP_LINEAR:
            case LINEAR_MIPMAP_NEAREST:
            case NEAREST_MIPMAP_NEAREST:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns a reference to the array with texture values.
     * 
     * @return
     */
    public Parameter[] getParameters() {
        return values;
    }

    /**
     * Returns the optional texture parameters, this is usually texture parameters other than the normal
     * min / mag-filter and wrap s / t
     * 
     * @return
     */
    public ParameterData[] getParameterData() {
        return parameters;
    }

}
