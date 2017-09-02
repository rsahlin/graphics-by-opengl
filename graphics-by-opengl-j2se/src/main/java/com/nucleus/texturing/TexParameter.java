package com.nucleus.texturing;

import com.nucleus.opengl.GLESWrapper.GLES20;

public enum TexParameter {
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

    private TexParameter(int value) {
        this.value = value;
    }

    /**
     * Allowed min texture filters, use this to get allowed values for texture minification filter
     */
    public static final TexParameter[] MIN_FILTERS = new TexParameter[] { NEAREST, LINEAR, NEAREST_MIPMAP_LINEAR,
            NEAREST_MIPMAP_NEAREST, LINEAR_MIPMAP_NEAREST, LINEAR_MIPMAP_LINEAR };
    /**
     * Allowed mag texture filters, use this to get allowed values for texture magnification filter
     */
    public static final TexParameter[] MAG_FILTERS = new TexParameter[] { LINEAR_MIPMAP_LINEAR };
    /**
     * Allowed texture wrap modes
     */
    public static final TexParameter[] WRAPMODES = new TexParameter[] { CLAMP, REPEAT, MIRRORED_REPEAT };


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
    public static TexParameter[] getUVWrapModes() {
        return new TexParameter[] { CLAMP, REPEAT, MIRRORED_REPEAT };
    }

    /**
     * Checks if value is valid texture minification filter
     * 
     * @param value
     * @return True if value is valid
     */
    public static boolean validateMinFilter(TexParameter value) {
        return validateValue(value, MIN_FILTERS);
    }

    /**
     * Checks if value is valid texture magnification filter
     * 
     * @param value
     * @return True if value is valid
     */
    public static boolean validateMagFilter(TexParameter value) {
        return validateValue(value, MAG_FILTERS);
    }

    /**
     * Checks if value is valid texture wrap mode value
     * 
     * @param value
     * @return True if value is valid
     */
    public static boolean validateWrapMode(TexParameter value) {
        return validateValue(value, WRAPMODES);
    }

    private static boolean validateValue(TexParameter value, TexParameter[] values) {
        for (TexParameter tp : values) {
            if (tp == value) {
                return true;
            }
        }
        return false;
    }


}