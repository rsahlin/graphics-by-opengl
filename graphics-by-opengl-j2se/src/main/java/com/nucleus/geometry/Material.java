package com.nucleus.geometry;

import com.google.gson.annotations.SerializedName;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.shader.ShaderProgram;

/**
 * The material properties for a renderable object.
 * 
 * @author Richard Sahlin
 *
 */
public class Material {

    public static final String MATERIAL = "material";
    
    public static final String BLEND_EQUATION = "blendEquation";
    public static final String BLEND_FUNC = "blendFunc";
    public static final String AMBIENT = "ambient";
    public static final String DIFFUSE = "diffuse";

    public enum BlendEquation {
        DISABLED(-1),
        GL_FUNC_ADD(GLES20.GL_FUNC_ADD),
        GL_FUNC_SUBTRACT(GLES20.GL_FUNC_SUBTRACT),
        GL_FUNC_REVERSE_SUBTRACT(GLES20.GL_FUNC_REVERSE_SUBTRACT);

        public final int value;

        private BlendEquation(int value) {
            this.value = value;
        }

        /**
         * Returns the blendequation for the value, or null if not found.
         * 
         * @param value
         * @return
         */
        public static BlendEquation get(int value) {
            for (BlendEquation be : values()) {
                if (be.value == value) {
                    return be;
                }
            }
            return null;
        }

    }

    public enum BlendFunc {
        GL_ZERO(GLES20.GL_ZERO),
        GL_ONE(GLES20.GL_ONE),
        GL_SRC_COLOR(GLES20.GL_SRC_COLOR),
        GL_ONE_MINUS_SRC_COLOR(GLES20.GL_ONE_MINUS_SRC_COLOR),
        GL_DST_COLOR(GLES20.GL_DST_COLOR),
        GL_ONE_MINUS_DST_COLOR(GLES20.GL_ONE_MINUS_DST_COLOR),
        GL_SRC_ALPHA(GLES20.GL_SRC_ALPHA),
        GL_ONE_MINUS_SRC_ALPHA(GLES20.GL_ONE_MINUS_SRC_ALPHA),
        GL_DST_ALPHA(GLES20.GL_DST_ALPHA),
        GL_ONE_MINUS_DST_ALPHA(GLES20.GL_ONE_MINUS_DST_ALPHA),
        GL_CONSTANT_COLOR(GLES20.GL_CONSTANT_COLOR),
        GL_ONE_MINUS_CONSTANT_COLOR(GLES20.GL_ONE_MINUS_CONSTANT_COLOR),
        GL_CONSTANT_ALPHA(GLES20.GL_CONSTANT_ALPHA),
        GL_ONE_MINUS_CONSTANT_ALPHA(GLES20.GL_ONE_MINUS_CONSTANT_ALPHA),
        GL_SRC_ALPHA_SATURATE(GLES20.GL_SRC_ALPHA_SATURATE);
        
        public final int value;

        private BlendFunc(int value) {
            this.value = value;
        }
    }

    public final static String NULL_PROGRAM_STRING = "Program is null";

    /**
     * Index for 4 ambient color properties.
     */
    public final static int AMBIENT_INDEX = 0;
    /**
     * Index for 4 diffuse color properties.
     */
    public final static int DIFFUSE_INDEX = 4;
    /**
     * Index for shininess
     */
    public final static int SHININESS_INDEX = 12;

    /**
     * Index for 4 specular color properties.
     */
    public final static int SPECULAR_INDEX = 8;

    public final static int COLOR_DATA_SIZE = 13;
    /**
     * The shader program to use when rendering
     */
    transient ShaderProgram program;

    @SerializedName(BLEND_EQUATION)
    private BlendEquation[] blendEquation = new BlendEquation[] { BlendEquation.GL_FUNC_ADD,
            BlendEquation.GL_FUNC_ADD };
    @SerializedName(BLEND_FUNC)
    private BlendFunc[] blendFunction = new BlendFunc[] { BlendFunc.GL_SRC_ALPHA, BlendFunc.GL_ONE_MINUS_SRC_ALPHA,
            BlendFunc.GL_SRC_ALPHA, BlendFunc.GL_DST_ALPHA };

    @SerializedName(AMBIENT)
    private float[] ambient = new float[] { 1, 1, 1, 1 };
    @SerializedName(DIFFUSE)
    private float[] diffuse;

    /**
     * Creates a default material
     */
    public Material() {
    }

    /**
     * Creates a copy of the source material
     * 
     * @param source
     */
    public Material(Material source) {
        copy(source);
    }

    /**
     * Sets the program to use when rendering.
     * 
     * @param program
     * @throws IllegalArgumentException If program is null
     */
    public void setProgram(ShaderProgram program) {
        if (program == null) {
            throw new IllegalArgumentException(NULL_PROGRAM_STRING);
        }
        this.program = program;
    }

    /**
     * Copies the source material
     * 
     * @param source
     */
    public void copy(Material source) {
        program = source.program;
        setBlendFunc(source.blendFunction);
        setBlendEquation(source.blendEquation);
        setAmbient(source);

    }

    /**
     * Returns the program to use for this material.
     * 
     * @return
     */
    public ShaderProgram getProgram() {
        return program;
    }


    /**
     * Returns the blend equation(s), used when setting the blendEquation or blendEquationSeparate
     * Set the first value to null to disable alpha blend
     * 
     * @return BlendEquation values
     */
    public BlendEquation[] getBlendEquation() {
        return blendEquation;
    }

    /**
     * Returns the blend function values, used when setting the blendFunc or blendFuncSeparate values.
     * 
     * @return
     */
    public BlendFunc[] getBlendFunc() {
        return blendFunction;
    }

    /**
     * Sets the blend equation to use
     * 
     * @param blendEquation
     */
    public void setBlendEquation(BlendEquation[] blendEquation) {
        if (this.blendEquation == null || this.blendEquation.length < blendEquation.length) {
            this.blendEquation = new BlendEquation[blendEquation.length];
        }
        System.arraycopy(blendEquation, 0, this.blendEquation, 0, blendEquation.length);
    }

    /**
     * Sets the blend function to use, this will copy the values
     * 
     * @param blendFunction
     */
    public void setBlendFunc(BlendFunc[] blendFunction) {
        if (this.blendFunction == null || this.blendFunction.length < blendFunction.length) {
            this.blendFunction = new BlendFunc[blendFunction.length];
        }
        System.arraycopy(blendFunction, 0, this.blendFunction, 0, blendFunction.length);
    }

    /**
     * Sets the separate blend equation/function for this material to gl
     * TODO Move this method from this class to renderer or utility
     * 
     * @param gles
     */
    public void setBlendModeSeparate(GLES20Wrapper gles) {
        if (blendEquation == null || blendEquation[0] == BlendEquation.DISABLED) {
            gles.glDisable(GLES20.GL_BLEND);
        } else {
            gles.glEnable(GLES20.GL_BLEND);
            gles.glBlendEquationSeparate(blendEquation[0].value, blendEquation[1].value);
            gles.glBlendFuncSeparate(blendFunction[0].value, blendFunction[1].value, blendFunction[2].value,
                    blendFunction[3].value);
        }
    }

    /**
     * Returns the diffuse color, or null if not set.
     * 
     * @return
     */
    public float[] getDiffuse() {
        return diffuse;
    }

    /**
     * Returns the ambient color, or null of not set.
     * 
     * @return
     */
    public float[] getAmbient() {
        return ambient;
    }

    /**
     * Copies the ambient value from the source, if source is null or does not have ambient, nothing is done.
     * 
     * @param source
     */
    public void setAmbient(Material source) {
        if (source != null && source.getAmbient() != null) {
            setAmbient(source.getAmbient(), 0);
        }
    }

    /**
     * Sets the ambient RGBA values from source array
     * 
     * @param ambient
     * @param index
     * @throws ArrayIndexOutOfBoundsException If there is not room to read 4 values at index
     */
    public void setAmbient(float[] ambient, int index) {
        if (this.ambient == null || this.ambient.length < 4) {
            this.ambient = new float[4];
        }
        this.ambient[0] = ambient[index++];
        this.ambient[1] = ambient[index++];
        this.ambient[2] = ambient[index++];
        this.ambient[3] = ambient[index++];
    }

    @Override
    public String toString() {
        return "RGB:" + blendEquation[0] + ", ALPHA:" + blendEquation[1] + " program:"
                + (program != null ? program.getClass().getSimpleName() : "null");
    }

}
