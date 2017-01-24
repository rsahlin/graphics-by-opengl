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
     * Returns the GL value for the texture parameter
     * 
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns an array with the allowed texture minification filters
     * 
     * @return
     */
    public static TexParameter[] getMinFilters() {
        return new TexParameter[] { NEAREST, LINEAR, NEAREST_MIPMAP_LINEAR, NEAREST_MIPMAP_NEAREST,
                LINEAR_MIPMAP_NEAREST, LINEAR_MIPMAP_LINEAR };
    }

    /**
     * Returns an array with the texture magnification filters
     * 
     * @return
     */
    public static TexParameter[] getMagFilters() {
        return new TexParameter[] { NEAREST, LINEAR };
    }

    /**
     * Returns an array with the texture UV wrap modes.
     * 
     * @return
     */
    public static TexParameter[] getUVWrapModes() {
        return new TexParameter[] { CLAMP, REPEAT, MIRRORED_REPEAT };
    }
}