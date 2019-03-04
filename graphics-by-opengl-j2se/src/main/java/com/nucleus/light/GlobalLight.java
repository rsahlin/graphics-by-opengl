package com.nucleus.light;

/**
 * Singleton class to handle global illumination
 * 
 */
public class GlobalLight {

    private Light light = new DirectionalLight(new float[] { 1, 1, 1, 1 }, new float[] { 0, 100000, 50000 });

    private static GlobalLight globalLight = new GlobalLight();

    /**
     * Returns the singleton instance of the global light
     * 
     * @return
     */
    public static GlobalLight getInstance() {
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
