package com.nucleus.geometry;

import com.nucleus.shader.ShaderProgram;

/**
 * The material properties for a renderable object.
 * 
 * @author Richard Sahlin
 *
 */
public class Material {

    public final static String NULL_PROGRAM_STRING = "Program is null";

    /**
     * The shader program to use when rendering
     */
    ShaderProgram program;

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
     * Color properties, must be supported by the program used.
     */
    protected final float[] colorProperties = new float[13];

    /**
     * 
     * @param program The program to use
     * @param color Color property source array, ambient, diffuse, specular
     * @param sourceoffset Offset into source array where color properties are read.
     * @param destoffset Offset into this class where color properties are written.
     * @param length Number of color property values to write, 3 for R,G,B
     * @throws IllegalArgumentException If program is null
     * @throws ArrayIndexOutOfBoundsException If length of color < sourceoffset + count or if destoffset + count >
     * COLOR_DATA_SIZE
     * @throws NullPointerException If color is null
     */
    public Material(ShaderProgram program, float[] color, int sourceoffset, int destoffset, int length) {
        setProgram(program);
        System.arraycopy(color, sourceoffset, colorProperties, destoffset, length);
    }

    /**
     * @param program The program to use
     * @throws IllegalArgumentException If program is null
     */
    public Material(ShaderProgram program) {
        setProgram(program);
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
     * Returns the program to use for this material.
     * 
     * @return
     */
    public ShaderProgram getProgram() {
        return program;
    }

}
