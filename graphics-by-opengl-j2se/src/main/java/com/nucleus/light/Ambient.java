package com.nucleus.light;

/**
 * Ambient lightsource, this provides ambient illumination for childnodes.
 * The ambient light is unattenuated.
 */
public class Ambient extends Light {

    /**
     * Creates a new ambient lightsource
     * 
     * @param type
     */
    public Ambient(Type type) {
        super(type);
    }

}
