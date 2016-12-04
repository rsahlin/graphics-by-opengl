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

    public enum BlendEquation {
        GL_FUNC_ADD(GLES20.GL_FUNC_ADD),
        GL_FUNC_SUBTRACT(GLES20.GL_FUNC_SUBTRACT),
        GL_FUNC_REVERSE_SUBTRACT(GLES20.GL_FUNC_REVERSE_SUBTRACT);

        public final int value;

        private BlendEquation(int value) {
            this.value = value;
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
     * Index for 4 specular color properties.
     */
    public final static int SPECULAR_INDEX = 8;
    /**
     * Index for shininess
     */
    public final static int SHININESS_INDEX = 12;

    public final static int COLOR_DATA_SIZE = 13;
    /**
     * The shader program to use when rendering
     */
    transient ShaderProgram program;

    @SerializedName("blendEquation")
    private BlendEquation[] blendEquation = new BlendEquation[] { BlendEquation.GL_FUNC_ADD,
            BlendEquation.GL_FUNC_ADD };
    @SerializedName("blendFunc")
    private BlendFunc[] blendFunction = new BlendFunc[] { BlendFunc.GL_SRC_ALPHA, BlendFunc.GL_ONE_MINUS_SRC_ALPHA,
            BlendFunc.GL_SRC_ALPHA, BlendFunc.GL_DST_ALPHA };

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
        setBlendFunc(source.blendFunction);
        setBlendEquation(source.blendEquation);
        program = source.program;
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
     * Sets the separate blend equation/function for this material, this will copy the values.
     * 
     * @param gles
     */
    public void setBlendModeSeparate(GLES20Wrapper gles) {
        if (blendEquation[0] == null) {
            gles.glDisable(GLES20.GL_BLEND);
        } else {
            gles.glEnable(GLES20.GL_BLEND);
            gles.glBlendEquationSeparate(blendEquation[0].value, blendEquation[1].value);
            gles.glBlendFuncSeparate(blendFunction[0].value, blendFunction[1].value, blendFunction[2].value,
                    blendFunction[3].value);
        }
    }
}
