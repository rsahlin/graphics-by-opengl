package com.nucleus.shader;

/**
 * The resources needed for a programmable stage of the pipeline
 *
 */
public interface Shader {

    /**
     * Different type of shadings that needs to be supported in shaders
     *
     */
    public enum Shading {
    flat(),
    textured(),
    pbr(),
    shadow1(),
    shadow2();
    }

}
