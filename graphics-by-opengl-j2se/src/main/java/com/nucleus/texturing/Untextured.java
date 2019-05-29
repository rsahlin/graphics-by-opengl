package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.opengl.shader.GLShaderProgram;

/**
 * For untextured objects, can define a shading which corresponds to the untextured program to use.
 * 
 * @author Richard Sahlin
 *
 */
public class Untextured extends Texture2D {

    @SerializedName("shading")
    private GLShaderProgram.Shading shading;

    protected Untextured() {
        super();
    }

    protected Untextured(Untextured source) {
        set(source);
    }

    /**
     * Copies the data from the source into this
     * 
     * @param source
     */
    protected void set(Untextured source) {
        super.set(source);
        this.shading = source.shading;
    }

    /**
     * Returns the (fragment) shader to use for the untextured object
     * 
     * @return
     */
    public GLShaderProgram.Shading getShading() {
        return shading;
    }

}
