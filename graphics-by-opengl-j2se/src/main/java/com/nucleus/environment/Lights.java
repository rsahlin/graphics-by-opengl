package com.nucleus.environment;

import com.nucleus.light.DirectionalLight;
import com.nucleus.light.Light;

/**
 * Singleton class to handle global illumination
 * 
 */
public class Lights {

    private Light light = new DirectionalLight(new float[] { 1, 1, 1, 1, 5000, 5000, 10000, 0, 0.2f, 0.2f, 0.7f, 0 });

    private static Lights globalLight = new Lights();

    /**
     * Returns the singleton instance of the global light
     * 
     * @return
     */
    public static Lights getInstance() {
        return globalLight;
    }

    /**
     * Returns a reference to the global light
     * 
     * @return
     */
    public Light getLight() {
        return light;
    }

}
